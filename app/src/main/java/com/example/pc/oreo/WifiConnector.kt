package com.example.pc.oreo

import android.util.Log
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
import kotlin.text.Charsets.UTF_8

class WifiConnector(ad: String, serverPort: Int) {
    var datagramSocket: DatagramSocket? = null
        private set
    private var serverAddress: InetAddress? = null
    private var port: Int = 0
    var wifiDataChangeListener: OreoWifiDataChangeListener? = null
    private val compositeDisposable = CompositeDisposable()
    lateinit var oreo: OreoCommandListener
    private val bufferSize = 128

    init {
        try {
            serverAddress = InetAddress.getByName(ad)
            port = serverPort
        } catch (e: UnknownHostException) {
            serverAddress = null
        }
    }

    private fun formRequestPacket(): ByteArray {
        val connectPacket = ByteArray(1)
        connectPacket[0] = WifiCommandCode.REQUEST_CONNECTION.value
        return connectPacket
    }

    fun connect() {
        compositeDisposable.add(
                Observable.create(ObservableOnSubscribe<DatagramPacket> { emitter ->
                    if (emitter.isDisposed) {
                        return@ObservableOnSubscribe
                    }
                    try {
                        datagramSocket = DatagramChannel.open().socket()
                        datagramSocket?.apply {
                            connect(serverAddress,port)
                            val connectPacket = formRequestPacket()
                            val datagramPacket = DatagramPacket(connectPacket, connectPacket.size, serverAddress, port)
                            send(datagramPacket)
                            val buffer = ByteArray(bufferSize)
                            val pk = DatagramPacket(buffer, buffer.size)
                            soTimeout = 100
                            receive(pk)
                            emitter.onNext(pk)
                        }
                    } catch (e: IOException) {
                        if (!emitter.isDisposed) {
                            emitter.onError(e)
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableObserver<DatagramPacket>() {
                            override fun onNext(datagramPacket: DatagramPacket) {
                                Log.e("connection", "")
                                wifiDataChangeListener?.onDriveDataReceived(datagramPacket.data.toString(UTF_8))
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
                    val buffer = ByteArray(bufferSize)
                    val packet = DatagramPacket(buffer, buffer.size)
                    datagramSocket?.soTimeout = 150
                    while (true) {
                        if (emitter.isCancelled) {
                            break
                        }
                        try {
                            if (!datagramSocket?.isClosed!!) {
                                datagramSocket?.receive(packet)
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
                                wifiDataChangeListener?.onDriveDataReceived(datagramPacket.data.toString(UTF_8))
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

    fun disconnect() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        datagramSocket?.close()
    }

    interface OreoWifiDataChangeListener {
        fun onDriveDataReceived(jsonData: String)

        fun onVideoDataReceived(videoBuffer: ByteArray, size: Int)
    }

    interface OreoCommandListener {
        fun onNotifyCommandUpdate(): ByteArray
    }

    enum class WifiCommandCode constructor(val value: Byte) {
        REQUEST_CONNECTION(100),
        APPROVE_CONNECTION(101),
        JOYSTICK_CONTROL(102),
        REQUEST_STREAM(105),
        FRAME_DATA(106),
        DRIVE_DATA(107);
    }
}
