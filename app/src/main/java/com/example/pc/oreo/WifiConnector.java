package com.example.pc.oreo;

import android.util.Log;

import com.example.pc.oreo.WifiCommand.WifiCommandCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.DatagramChannel;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;

public class WifiConnector {
    private DatagramSocket datagramSocket;
    private DatagramSocket videoSocket;
    private InetAddress serverAddress;
    private int port;
    private TelloWifiDataChangeListener wifiDataChangeListener;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public WifiConnector(String ad, int serverPort) {
        try {
            serverAddress = InetAddress.getByName(ad);
            port = serverPort;
            datagramSocket = new DatagramSocket();
            videoSocket = new DatagramSocket();
        } catch (SocketException e) {
            datagramSocket = null;
            videoSocket = null;
        } catch (UnknownHostException e) {
            serverAddress = null;
        }
    }

    private byte[] formRequestPacket(int port) {
        byte[] connectPacket = new byte[64];
        connectPacket[0] = WifiCommand.WifiCommandCode.REQUEST_CONNECTION.getLength();
        connectPacket[1] = WifiCommand.WifiCommandCode.REQUEST_CONNECTION.getValue();
        return connectPacket;
    }

    public void connect() {
        compositeDisposable.add(
                Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) {
                        if (emitter.isDisposed()) {
                            return;
                        }
                        try {
                            DatagramChannel channel = DatagramChannel.open();
                            datagramSocket = channel.socket();
                            datagramSocket.connect(serverAddress, port);
                            byte[] connectPacket = formRequestPacket(videoSocket.getLocalPort());
                            sendWithException(connectPacket);
                            byte[] buffer = new byte[2048];
                            DatagramPacket pk = new DatagramPacket(buffer, buffer.length);
                            datagramSocket.setSoTimeout(100);
                            datagramSocket.receive(pk);
                            int replyCode = pk.getData()[1];
                            if (replyCode == WifiCommandCode.APPROVE_CONNECTION.getValue()) {
                                emitter.onNext(replyCode);
                                emitter.onComplete();
                            } else {
                                emitter.onError(new Throwable());
                            }
                        } catch (IOException e) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<Integer>() {
                            @Override
                            public void onNext(Integer replyCode) {
                                Log.e("connection", replyCode.toString());
                                wifiDataChangeListener.onWifiCommandReceived(new WifiCommand(replyCode));
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("connectionError", "AAAAAAAAAA");
                            }

                            @Override
                            public void onComplete() {
                                Log.e("connectionComplete", "BBBBBBBB");
                                streamData();
                                dispose();
                            }
                        })
        );
    }

    public void send(final byte[] message) {
        compositeDisposable.add(
                Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> emitter) {
                        if (emitter.isDisposed()) {
                            return;
                        }
                        try {
                            DatagramPacket datagramPacket = new DatagramPacket(message, message.length, serverAddress, port);
                            datagramSocket.send(datagramPacket);
                            emitter.onNext(true);
                            emitter.onComplete();
                        } catch (IOException e) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                        }
                    }
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retry()
                        .subscribeWith(new DisposableObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean sent) {
                                Log.e("sending", "");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("sendingError", "AAAAAAAAAA");
                            }

                            @Override
                            public void onComplete() {
                                Log.e("sendingComplete", "BBBBBBBB");
                                dispose();
                            }
                        })
        );
    }

    private void streamData() {
        compositeDisposable.add(
                Flowable.create(new FlowableOnSubscribe<DatagramPacket>() {
                    @Override
                    public void subscribe(FlowableEmitter<DatagramPacket> emitter) throws Exception {
                        byte[] buffer = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        datagramSocket.setSoTimeout(150);
                        while (true) {
                            if (emitter.isCancelled()) {
                                break;
                            }
                            try {
                                if (!datagramSocket.isClosed()) {
                                    datagramSocket.receive(packet);
                                    emitter.onNext(packet);
                                } else {
                                    Log.e("socket closed", "");
                                    emitter.onError(new Throwable());
                                    break;
                                }
                            } catch (IOException e) {
                                Log.e("UDP packet error", "");
                                if (!emitter.isCancelled()) {
                                    emitter.onError(e);
                                }
                                break;
                            }
                        }
                    }
                }, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.trampoline())
                        .retry()
                        .subscribeWith(new DisposableSubscriber<DatagramPacket>() {
                            @Override
                            public void onNext(DatagramPacket datagramPacket) {
                                Log.e("receive ", datagramPacket.getData().length + "");
                                WifiCommand command = new WifiCommand(datagramPacket.getData());
                                wifiDataChangeListener.onWifiCommandReceived(command);
                                Log.e("receiveNext", command.getType() + "");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("receiveError", "AAAAAAAAAA");
                                dispose();
                            }

                            @Override
                            public void onComplete() {
                                Log.e("receiveComplete", "BBBBBBBB");
                                dispose();
                            }
                        })
        );
    }

    private void startStreaming() {
        compositeDisposable.add(
                Flowable.create(new FlowableOnSubscribe<DatagramPacket>() {
                    @Override
                    public void subscribe(FlowableEmitter<DatagramPacket> emitter) throws Exception {
                        byte[] buffer = new byte[2048];
                        videoSocket.setSoTimeout(100);
                        DatagramPacket packet;
                        while (true) {
                            if (emitter.isCancelled()) {
                                break;
                            }
                            try {
                                if (!videoSocket.isClosed()) {
                                    packet = new DatagramPacket(buffer, buffer.length);
                                    videoSocket.receive(packet);
                                    emitter.onNext(packet);
                                } else {
                                    Log.e("video socket closed", "");
                                    emitter.onError(new Throwable());
                                    break;
                                }
                            } catch (SocketTimeoutException e) {
                                continue;
                            } catch (IOException e) {
                                Log.e("video packet error", "");
                                if (!emitter.isCancelled()) {
                                    emitter.onError(e);
                                }
                            }
                        }
                    }
                }, BackpressureStrategy.BUFFER)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.trampoline())
                        .subscribeWith(new DisposableSubscriber<DatagramPacket>() {
                            @Override
                            public void onNext(DatagramPacket packet) {
                                Log.e("streamNext", packet.getLength() + "");
                                wifiDataChangeListener.onVideoDataReceived(packet.getData(), packet.getLength());
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("streamError", "AAAAAAAAAA");
                            }

                            @Override
                            public void onComplete() {
                                Log.e("streamComplete", "BBBBBBBB");
                            }
                        })
        );
    }

    private void sendWithException(byte[] message) throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(message, message.length, serverAddress, port);
        datagramSocket.send(datagramPacket);
    }

    public void disconnect() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
        }
        if (videoSocket != null) {
            videoSocket.close();
        }
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public DatagramSocket getVideoSocket() {
        return videoSocket;
    }

    public void setWifiDataChangeListener(TelloWifiDataChangeListener wifiDataChangeListener) {
        this.wifiDataChangeListener = wifiDataChangeListener;
    }

    public interface TelloWifiDataChangeListener {
        void onWifiCommandReceived(WifiCommand command);

        void onVideoDataReceived(byte[] videoBuffer, int size);
    }
}
