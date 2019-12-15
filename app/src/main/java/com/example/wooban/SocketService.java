package com.example.wooban;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketService extends Service {

    private static final String TAG = "SocketService";
    // 소켓 생성에 필요한 IP
    private static final String SERVER_IP = "54.180.104.190";
    // 소켓 생성에 필요한 포트 번호
    private static final int SERVER_PORT = 5000;
    Socket socket;
    PrintWriter pw;
    IBinder mBinder = new SocketBinder();
    String received_data;
    Handler handler = null;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    // SocketService 객체를 리턴
    class SocketBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: 실행");
        return mBinder;
    }

    // 서비스가 실행되는 최초 1번만 실행됨
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 실행");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 실행");
        // SocketConnect 스레드 시작
        SocketConnect connect = new SocketConnect();
        connect.start();

        /* join()을 호출하여 SocketConnect가 종료되길 기다렸다가 onBind가 실행되고, onBind 실행 후
        chattingActivity의 onServiceConnected()가 실행되게 하여 chattingActivity에서 Request 스레드가 먼저 실행되지 않게 함 */
        try {
            Log.d(TAG, "onStartCommand: connect.join() 실행");
            connect.join();
        } catch (InterruptedException e) {
            Log.d(TAG, "onStartCommand: InterruptedException 발생");
            e.printStackTrace();
        }

        // 서버에서 데이터를 받는 스레드 시작
        DataReceiveThread receiveThread = new DataReceiveThread();
        receiveThread.start();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 실행");
    }


    // 소켓 생성 및 연결 스레드
    class SocketConnect extends Thread {

        public void run() {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
                pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                Log.d(TAG, "run: 소켓 실행" + socket.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // 메세지를 받는 스레드
    class DataReceiveThread extends Thread {

        // 스레드 동작
        public void run() {
            try {
                // 소켓에서 받은 데이터
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                while (true) {
                    Log.d(TAG, "run: while문 실행");
//                    if (Thread.interrupted()) {
//                        Log.d(TAG, "run: Thread.interrupted(): " + Thread.interrupted());
//                        break;
//                    }
                    Log.d(TAG, "run: 스레드 실행 중...");
                    // 서버에서 온 메세지 데이터 수신
                    received_data = reader.readLine();
                    Log.d(TAG, "run: 서버에서 받은 메세지: " + received_data);

                    // message 객체에 String 데이터를 넣음
                    Bundle data = new Bundle();
                    data.putString("received_data", received_data);
                    Message msg = new Message();
                    msg.setData(data);
                    // 핸들러로 메세지 전달
                    handler.sendMessage(msg);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}


