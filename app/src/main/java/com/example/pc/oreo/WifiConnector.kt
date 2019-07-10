package com.example.pc.oreo

import android.util.Log
import com.example.pc.oreo.WifiCommand.WifiCommandCode
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.DisposableSubscriber
import java.io.IOException
import java.net.*
import java.nio.channels.DatagramChannel
import java.util.concurrent.TimeUnit

class WifiConnector(ad: String, serverPort: Int) {
    var datagramSocket: DatagramSocket? = null
        private set
    private var serverAddress: InetAddress? = null
    private var port: Int = 0
    private var wifiDataChangeListener: OreoWifiDataChangeListener? = null
    private val compositeDisposable = CompositeDisposable()
    lateinit var oreo: OreoCommandListener

    init {
        try {
            serverAddress = InetAddress.getByName(ad)
            port = serverPort
            datagramSocket = DatagramSocket()
        } catch (e: SocketException) {
            datagramSocket = null
        } catch (e: UnknownHostException) {
            serverAddress = null
        }
    }

    private fun formRequestPacket(): ByteArray {
        val connectPacket = ByteArray(64)
        connectPacket[0] = WifiCommand.WifiCommandCode.REQUEST_CONNECTION.length
        connectPacket[1] = WifiCommand.WifiCommandCode.REQUEST_CONNECTION.value
        return connectPacket
    }

    fun connect() {
        compositeDisposable.add(
                Observable.create(ObservableOnSubscribe<Int> { emitter ->
                    if (emitter.isDisposed) {
                        return@ObservableOnSubscribe
                    }
                    try {
                        val channel = DatagramChannel.open()
                        datagramSocket = channel.socket()
                        datagramSocket?.connect(serverAddress, port)
                        val connectPacket = formRequestPacket()
                        sendWithException(connectPacket)
                        val buffer = ByteArray(2048)
                        val pk = DatagramPacket(buffer, buffer.size)
                        datagramSocket?.soTimeout = 100
                        datagramSocket?.receive(pk)
                        val replyCode = pk.data[1].toInt()
                        if (replyCode == WifiCommandCode.APPROVE_CONNECTION.value.toInt()) {
                            emitter.onNext(replyCode)
                            emitter.onComplete()
                        } else {
                            emitter.onError(Throwable())
                        }
                    } catch (e: IOException) {
                        if (!emitter.isDisposed) {
                            emitter.onError(e)
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<Int>() {
                            override fun onNext(replyCode: Int) {
                                Log.e("connection", replyCode.toString())
                                wifiDataChangeListener!!.onWifiCommandReceived(WifiCommand(replyCode))
                            }

                            override fun onError(e: Throwable) {
                                Log.e("connectionError", "AAAAAAAAAA")
                            }

                            override fun onComplete() {
                                Log.e("connectionComplete", "BBBBBBBB")
                                streamData()
                                dispose()
                            }
                        })
        )
    }

    fun streamControls() {
        compositeDisposable.add(
                Flowable.interval(100, TimeUnit.MILLISECONDS)
                        .onBackpressureLatest()
                        .map {
                            val controls = oreo.onNotifyCommandUpdate()
                            val datagramPacket = DatagramPacket(controls, controls.size, serverAddress, port)
                            datagramSocket?.soTimeout = 150
                            datagramSocket?.send(datagramPacket)
                            true
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.trampoline())
                        .doOnError { err -> Log.e("control send error", err.localizedMessage) }
                        .retry()
                        .subscribeWith(object : DisposableSubscriber<Boolean>() {
                            override fun onNext(result: Boolean) {
                                Log.e("control sent ", result.toString())
                            }

                            override fun onError(e: Throwable) {
                                Log.e("sendError", e.localizedMessage)
                                dispose()
                            }

                            override fun onComplete() {
                                Log.e("sendComplete", "BBBBBBBB")
                                dispose()
                            }
                        })
        )
    }

    private fun streamData() {
        compositeDisposable.add(
                Flowable.create(FlowableOnSubscribe<DatagramPacket> { emitter ->
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    datagramSocket!!.soTimeout = 150
                    while (true) {
                        if (emitter.isCancelled) {
                            break
                        }
                        try {
                            if (!datagramSocket!!.isClosed) {
                                datagramSocket!!.receive(packet)
                                emitter.onNext(packet)
                            } else {
                                Log.e("socket closed", "")
                                emitter.onError(Throwable())
                                break
                            }
                        } catch (e: IOException) {
                            Log.e("UDP packet error", "")
                            if (!emitter.isCancelled) {
                                emitter.onError(e)
                            }
                            break
                        }

                    }
                }, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.trampoline())
                        .retry()
                        .subscribeWith(object : DisposableSubscriber<DatagramPacket>() {
                            override fun onNext(datagramPacket: DatagramPacket) {
                                Log.e("receive ", datagramPacket.data.size.toString() + "")
                                val command = WifiCommand(datagramPacket.data)
                                wifiDataChangeListener!!.onWifiCommandReceived(command)
                                Log.e("receiveNext", command.type.toString() + "")
                            }

                            override fun onError(e: Throwable) {
                                Log.e("receiveError", "AAAAAAAAAA")
                                dispose()
                            }

                            override fun onComplete() {
                                Log.e("receiveComplete", "BBBBBBBB")
                                dispose()
                            }
                        })
        )
    }

    @Throws(IOException::class)
    private fun sendWithException(message: ByteArray) {
        val datagramPacket = DatagramPacket(message, message.size, serverAddress, port)
        datagramSocket!!.send(datagramPacket)
    }

    fun disconnect() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        if (datagramSocket != null) {
            datagramSocket!!.close()
        }
    }

    fun setWifiDataChangeListener(wifiDataChangeListener: OreoWifiDataChangeListener) {
        this.wifiDataChangeListener = wifiDataChangeListener
    }

    interface OreoWifiDataChangeListener {
        fun onWifiCommandReceived(command: WifiCommand)

        fun onVideoDataReceived(videoBuffer: ByteArray, size: Int)
    }

    interface OreoCommandListener {
        fun onNotifyCommandUpdate(): ByteArray
    }
}
