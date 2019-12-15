package com.example.wooban;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChattingActivity extends AppCompatActivity {
    public static final String TAG = "ChattingActivity";
    private RecyclerView recyclerView;
    private ArrayList<ChattingModel> chatArrayList;
    private Activity activity;
    private Socket socket = null;
    private Handler mHandler = new Handler();
    private ChattingAdapter adapter;
    private EditText chatEditText;
    String my_name, my_id, my_profile_image_url;
    public static String idx;
    String chat_type, chat_room_number, chat_message, chat_time, chat_id, chat_name, chat_profile_image_url;
    int from_create_activity;
    SQLiteDatabase chatDB = null;
    private final String db_name = "chat_info";
    private String table_name;
    private PrintWriter pw;
    boolean isQuit, isService = false;
    private int access_count;
    public static SocketService socketService;

    /**
     * 서비스 연결 객체
     **/
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 서비스와 연결되었을때 호출되는 메소드
            SocketService.SocketBinder mb = (SocketService.SocketBinder) service;
            // mb.getService() : SocketService 클래스의 객체 리턴
            socketService = mb.getService();
            isService = true;
            Log.d(TAG, "onServiceConnected: 서비스 소켓: " + socketService.socket);
            Log.d(TAG, "onServiceConnected: 핸들러 객체를 서비스에 넘김");
            socketService.setHandler(handler);
            // 소켓 받기
            getSocket();
            // 인텐트 받음
            Intent intent = getIntent();
            // 내 정보 받기
            getMyInfo(intent);
            // idx 받기
            getChatRoomInfo(intent);

//            // 소켓 연결 스레드 실행
//            Log.d(TAG, "onServiceConnected: request 실행");
//            // create, join, rejoin 등의 요청을 서버에 보냄
//            Request request = new Request();
//            request.start();

            // 서비스로부터 데이터를 받아 분류하고 저장하는 스레드 시작
//            receiveThread = new ChatClientReceiveThread(data);
//            receiveThread.start();
//            Log.d(TAG, "run: receiveThread.getName(): " + receiveThread.getName());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊어졌을 때 호출되는 메소드
            Log.d(TAG, "onServiceDisconnected: 서비스 연결 끊어짐");
            isService = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        // SocketService 실행하여 서비스의 onCreate() 실행시킴
        Log.d(TAG, "onCreate: 처음 채팅방에 접속하면 SocketService 실행");
        Intent serviceIntent = new Intent(ChattingActivity.this, SocketService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        // 툴바
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chatting_toolbar);
        setSupportActionBar(mToolbar);

        // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 툴바 제목
        getSupportActionBar().setTitle("그룹채팅");

        // 리사이클러뷰 객체 생성 및 레이아웃매니저 지정
        recyclerView = findViewById(R.id.chatting_recycler_view);
        // 레이아웃 매니저 지정
        recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(ChattingActivity.this, LinearLayoutManager.VERTICAL, false));
//        recyclerView.setLayoutManager(new LinearLayoutManager(ChattingActivity.this, LinearLayoutManager.VERTICAL, false));
        Log.d(TAG, "onCreate: 채팅 리사이클러뷰에 어댑터 장착");
        chatArrayList = new ArrayList<>();

        Intent intent = getIntent();
//        intent.getIntExtra("access_count", 100);
//        Log.d(TAG, "onCreate: access_count: " + access_count);
//
//        // 두번째 채팅방 접속부터는 SQLite에 저장된 채팅 내용을 가져와 리사이클러뷰에 보여준다.
//        if (access_count > 1) {
//            Log.d(TAG, "onCreate: 두번째 이상 채팅방 접속");
//            chatArrayList = showList();
//        }

        // 번들 객체 받음
        Bundle bundle = intent.getBundleExtra("bundle");

        // 어댑터 객체 생성 및 리사이클러뷰에 어댑터 장착
        adapter = new ChattingAdapter(ChattingActivity.this, getApplicationContext(), chatArrayList, bundle);
        recyclerView.setAdapter(adapter);

        final ImageView plusButton, sendButton;

        // 플러스 버튼
        plusButton = findViewById(R.id.chatting_plus_button);
        // 채팅 전송 버튼
        sendButton = findViewById(R.id.chatting_send_button);
        // 채팅 입력을 위한 EditText
        chatEditText = findViewById(R.id.chatting_edit_text);

        // 채팅 입력 감지
        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String chat_text = chatEditText.getText().toString().trim();
                // 채팅 입력창이 공백이면(글을 썼다가 다시 지워서 공백이 될 경우) 전송 버튼을 안 보이게 함
                if (chat_text.isEmpty()) {
                    sendButton.setVisibility(View.GONE);
                }

            }
        });

        // 전송 버튼 클릭시
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 전송 버튼 클릭됨");
                // 입력한 채팅 내용
                String message = chatEditText.getText().toString();
                Log.d(TAG, "onClick: 입력한 채팅 내용: " + message);
                // 입력한 시간
                // 서버에 메세지 전송
                (new SendMessage(message)).start();
                // 입력한 내용 EditText에서 지움
                chatEditText.setText("");

            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: 실행");
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: 실행");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 실행");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 실행");
    }

    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: 실행");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 실행");
        if (isService) {
            // 서비스 종료
            unbindService(connection);
            isService = false;
        }
    }


    // 화면을 돌렸을 때 chatArrayList 저장
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: 실행");
        outState.putParcelableArrayList("chatArrayList", chatArrayList);
        Log.d(TAG, "onSaveInstanceState: chatArrayList: " + chatArrayList);
    }

    // 화면을 돌렸을 때 데이터 복원
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: 실행");
        chatArrayList = savedInstanceState.getParcelableArrayList("chatArrayList");
        Log.d(TAG, "onRestoreInstanceState: chatArrayList: " + chatArrayList);

        // 인텐트 받음
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        Log.d(TAG, "onRestoreInstanceState: bundle: " + bundle);

        // 리사이클러뷰 객체 생성 및 레이아웃매니저 지정
        recyclerView = findViewById(R.id.chatting_recycler_view);
        // 레이아웃 매니저 지정
        recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(ChattingActivity.this, LinearLayoutManager.VERTICAL, false));
//        recyclerView.setLayoutManager(new LinearLayoutManager(ChattingActivity.this, LinearLayoutManager.VERTICAL, false));
        Log.d(TAG, "onCreate: 채팅 리사이클러뷰에 어댑터 장착");
        // 어댑터 객체 생성 및 리사이클러뷰에 어댑터 장착
        adapter = new ChattingAdapter(activity, getApplicationContext(), chatArrayList, bundle);
        recyclerView.setAdapter(adapter);

    }

    // 뒤로가기 버튼 클릭시
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class Disconnect extends Thread {
        public void run() {
            try {
                if (socket != null) {
                    socket.close();
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            Log.d(TAG, "run: 소켓 연결 종료");
//                            setToast("연결이 종료되었습니다.");
                        }
                    });

                }

            } catch (Exception e) {
                Log.d(TAG, "run: 소켓 연결 종료 실패");
                final String fail = "연결을 끊는데 실패했습니다.";
                Log.d("Connect", e.getMessage());
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        setToast(fail);
                    }

                });

            }

        }
    }


    // 서버에 요청을 전당
    private class Request extends Thread {


        public void run() {
            Log.d(TAG, "run: 커넥트 클래스 실행");
            try {
//                Log.d(TAG, "run: 커넥트 실행");
//
//                // 서비스에서 연결된 소켓을 가져옴
//                Log.d(TAG, "run: socketService.socket: " + socketService.socket);
//                socket = socketService.socket;
//                Log.d(TAG, "run: Connect에서 socket: " + socket);
//                // 마찬가지로 서비스에서 만든 PrintWriter 객체
//                pw = socketService.pw;
//                Log.d(TAG, "run: Connect에서 pw: " + pw);

                // 이름, 아이디, 프로필 이미지 url을 서버에 보냄
                // 채팅방을 만들었으면 create 요청, 아니면 join 요청을 하게 함
                if (from_create_activity == 100) {
                    String request = "create&" + idx + "&" + my_name + "&" + my_id + "&" + my_profile_image_url + "\r\n";
                    Log.d(TAG, "run: 서버에 보내는 방 생성 요청: " + request);
                    pw.println(request);
                    // 채팅방에 처음 접속하면 chatting_type을 join 분류하여 환영메세지가 나오게 하고 그 이외에는 rejoin으로 분류하여 환영메세지가 뜨지 않게함
                } else if (access_count == 1) {
                    String request = "join&" + idx + "&" + my_name + "&" + my_id + "&" + my_profile_image_url + "\r\n";
                    Log.d(TAG, "run: 서버에 보내는 join 요청: " + request);
                    pw.println(request);
                } else {
                    String request = "rejoin&" + idx + "&" + my_name + "&" + my_id + "&" + my_profile_image_url + "\r\n";
                    Log.d(TAG, "run: 서버에 보내는 rejoin 요청: " + request);
                    pw.println(request);
                }


            } catch (Exception e) {
                Log.d(TAG, "run: Connect에서 소켓 생성 실패");
                // Exception 내용 출력
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String exceptionAsStrting = sw.toString();

                Log.d(TAG, exceptionAsStrting);
            }

        }

    }

    // 나갈 때 서버에 요청을 보내는 스레드
    private class Quit extends Thread {
        public void run() {
            try {
                // 나가기를 눌렀을 때 서버에 전송되는 요청
                String request = "quit&" + idx + "&" + my_name + "&" + my_id + "&" + my_profile_image_url + "\r\n";
                Log.d(TAG, "run: 서버에 보내는 quit 요청: " + request);
                pw.println(request);
            } catch (Exception e) {
                Log.d(TAG, "run: 소켓 생성 실패");
            }

        }
    }

    // 채팅 액티비티가 종료될 때 요청을 보내는 스레드
    private class Finish extends Thread {
        public void run() {
            try {
                // 나가기를 눌렀을 때 서버에 전송되는 요청
                String request = "finish&" + idx + "&" + my_name + "&" + my_id + "&" + my_profile_image_url + "\r\n";
                Log.d(TAG, "run: 서버에 보내는 finish 요청: " + request);
                pw.println(request);
            } catch (Exception e) {
                Log.d(TAG, "run: 소켓 생성 실패");
            }

        }
    }


    // 메세지를 전송하는 스레드
    private class SendMessage extends Thread {
        String message;

        SendMessage(String message) {
            this.message = message;
        }

        public void run() {
            PrintWriter sendWriter;
            try {
                sendWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                // 입력한 메세지
                String request = "message&" + idx + "&" + message + "\r\n";
                Log.d(TAG, "run: 클라이언트에서 전송한 방 번호: " + idx);
                Log.d(TAG, "run: 클라이언트에서 전송한 메세지: " + message);
                sendWriter.println(request);


//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        editText.setText("");
//                        editText.requestFocus();
//                    }
//                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 메세지를 전송하는 스레드(고정된 값만 보내는 예제)
//    class SendMessage extends Thread {
//
//        public void run () {
//            try {
//                Log.d(TAG, "run: 메세지 전송 성공");
//                byte[] messageByte;
//                Log.d(TAG, "run: 메세지 전송 성공2");
//                messageByte = message.getBytes();
//                Log.d(TAG, "run:  메세지 전송 성공3");
//                writeSocket.write(messageByte);
//                Log.d(TAG, "run:  메세지 전송 성공4");
//
//
//            } catch (Exception e) {
//                Log.d(TAG, "run: 메세지 전송 실패");
//                final String fail = "메시지 전송에 실패하였습니다.";
//                Log.d("Message", e.getMessage());
//                mHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        setToast(fail);
//                    }
//
//                });
//
//            }
//
//        }
//    }

    // 토스트 생성
    void setToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 방장이 방을 생성할 때 생성한 방에 대한 정보를 가져오는 메소드
    private void fetchOneChatRoomInfo() {

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchOneChatRoomInfo(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 생성한 방에 대한 정보를 가져오는 요청 응답 성공");
                JsonObject jsonObject = response.body();
                Log.d(TAG, "onResponse: jsonObject: " + jsonObject);
                JsonElement roomObject = null;
                // 하나의 행에 대한 정보를 가져오므로 get(0) 사용
                if (jsonObject != null) {
                    roomObject = jsonObject.getAsJsonArray("chat_room_info").get(0);
                    // 인덱스 번호를 가져옴
                    idx = roomObject.getAsJsonObject().get("idx").getAsString();
                    Log.d(TAG, "onResponse: 방 인덱스 번호 idx: " + idx);
                    // 채팅방 접속 횟수 (처음 접속일 경우 기본값인 1이 채팅방 접속 횟수가 됨)
                    Intent intent = getIntent();
                    // 채팅방 접속 횟수를 db에서 받고 플러스 1
                    access_count = intent.getIntExtra("access_count", 0) + 1;
                    Log.d(TAG, "onResponse: 채팅방 접속 횟수: " + access_count);
                    // SQLite table 이름
                    table_name = "room" + idx;
                    Log.d(TAG, "onResponse: 방장일때 테이블 이름: " + table_name);
                    // db에 방 접속 횟수 저장
                    setChatRoomAccessCount(idx);
                    Log.d(TAG, "onServiceConnected: request 실행");
                    // create, join, rejoin 등의 요청을 서버에 보냄
                    Request request = new Request();
                    request.start();


                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 생성한 방에 대한 정보를 가져오는 요청 응답 실패");

            }
        });

    }

    // 메뉴 리소스 XML 파일(main_menu)을 툴바에 표시
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatting_menu, menu);

        return true;
    }

    // 툴바의 버튼들을 클릭했을때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 뒤로가기 버튼 눌렀을 때
            case android.R.id.home: {
                // 3초 뒤 실행
                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: onBackPressed()에서 소켓 연결 종료");
                        // 소켓 연결 종료 스레드 실행
                        (new Disconnect()).start();
                    }
                }, 3000);
                finish();
                break;
            }
            // 오른쪽 메뉴에서 나가기 버튼 눌렀을 때
            case R.id.chatting_menu_out: {
                Log.d(TAG, "onOptionsItemSelected: 채팅방에서 나가기를 누름");
                QuitChatRoomDialog();

            }
        }
        return super.onOptionsItemSelected(item);
    }


    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }


    private void createDB(String chat_room_number) {
            table_name = "room" + chat_room_number;
        try {

            chatDB = this.openOrCreateDatabase(db_name, MODE_PRIVATE, null);
            //테이블이 존재하지 않으면 새로 생성합니다.
            Log.d(TAG, "createDB: table_name: " + table_name);
            chatDB.execSQL("CREATE TABLE IF NOT EXISTS " + table_name
                    + " (chat_type VARCHAR(255), chat_message VARCHAR(255), chat_time VARCHAR(255)" +
                    ", chat_id VARCHAR(255), chat_name VARCHAR(255), chat_profile_image_url VARCHAR(255) );");
            //테이블이 존재하는 경우 기존 데이터를 지우기 위해서 사용합니다.
//            chatDB.execSQL("DELETE FROM " + table_name  );

        } catch (final SQLiteException se) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 사용하고자 하는 코드
                    Toast.makeText(getApplicationContext(), se.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, 0);
            Log.e("", se.getMessage());
        }
    }

    protected ArrayList showList() {
        Log.d(TAG, "showList: 실행");
        try {

            SQLiteDatabase ReadDB = this.openOrCreateDatabase(db_name, MODE_PRIVATE, null);


            Log.d(TAG, "showList: 테이블 이름: " + table_name);
            //SELECT문을 사용하여 테이블에 있는 데이터를 가져옵니다..
            Cursor c = ReadDB.rawQuery("SELECT * FROM " + table_name, null);

            if (c != null) {

                if (c.moveToFirst()) {
                    do {

                        //테이블에서 컬럼값을 가져와서
                        String sql_chat_type = c.getString(c.getColumnIndex("chat_type"));
                        String sql_chat_message = c.getString(c.getColumnIndex("chat_message"));
                        String sql_chat_time = c.getString(c.getColumnIndex("chat_time"));
                        String sql_chat_id = c.getString(c.getColumnIndex("chat_id"));
                        String sql_chat_name = c.getString(c.getColumnIndex("chat_name"));
                        String sql_chat_profile_image_url = c.getString(c.getColumnIndex("chat_profile_image_url"));
                        Log.d(TAG, "showList: sql_chat_type: " + sql_chat_type);
                        Log.d(TAG, "showList: sql_chat_message: " + sql_chat_message);
                        Log.d(TAG, "showList: sql_chat_time: " + sql_chat_time);

                        ChattingModel model = new ChattingModel(sql_chat_type, sql_chat_message, sql_chat_time, sql_chat_id, sql_chat_name, sql_chat_profile_image_url);
                        // 채팅 내용 ArrayList에 추가
                        chatArrayList.add(model);

                    } while (c.moveToNext());
                }
            }

            ReadDB.close();


        } catch (SQLiteException se) {
            Toast.makeText(getApplicationContext(), se.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("", se.getMessage());
        }
        return chatArrayList;
    }

    // shared에 저장된 채팅방 접속 횟수를 가져오는 메소드
//    private int getSharedCount() {
//        // 쉐어드 객체 생성
//        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
//        // shared에 저장되어 있는 유저가 connection한 횟수를 불러옴. 기본값 = 0
//        int count = sharedPreferences.getInt(my_id + idx, 0);
//        Log.d(TAG, "getSharedCount: my_id + idx: " + my_id + idx);
//        Log.d(TAG, "getSharedCount: 쉐어드에 저장된 connection 횟수: " + count);
//        return count;
//    }


    // 방에 접속할 때마다 이전 카운트에 1을 더해 shared에 저장하는 메소드
//    private void saveConnectCount() {
//
//        // shared에 저장되어 있는 채팅방 접속 횟수. 처음 접속시 기본값 0이 반환됨
//        tmp_count = getSharedCount();
//        tmp_count++;
//        Log.d(TAG, "run: tmp_count: " + tmp_count);
//        // 쉐어드 객체
//        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
//        //저장을 하기위해 editor 객체 생성
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        // 내 아이디 + idx를 key값으로 소켓이 connection 될 때마다 횟수 저장
//        Log.d(TAG, "saveConnectCount: 방문 횟수 저장 직전 idx: " + idx);
//        editor.putInt(my_id + idx, tmp_count);
//        // 저장
//        editor.apply();
//
//    }

    // 접속 카운트 0을 shared에 저장
    private void initiateCount() {
        // 쉐어드 객체
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        //저장을 하기위해 editor 객체 생성
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 내 아이디 + idx를 key값으로 소켓이 connection 될 때마다 횟수 저장
        Log.d(TAG, "initiateCount: my_id + idx: " + my_id + idx);
        editor.putInt(my_id + idx, 0);
        // 저장
        editor.apply();
    }


    // 나가기 클릭시 나타나는 다이얼로그
    private void QuitChatRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("채팅방 나가기");
        builder.setMessage("채팅방에서 나가시겠습니까? 나가기를 하면 대화내용이 모두 삭제되고 채팅목록에서도 삭제됩니다.");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 채팅방에서 나가기 예를 선택");

                        // 접속 카운트를 0 으로 만들어 다음에 접속할 때 SQLite에 저장된 채팅 내용이 보이지 않고 초기화된 화면이 나타나게 함
                        initiateCount();
                        //
                        isQuit = true;
                        // 나갈 때 서버에 요청을 보내는 스레드 실행
                        (new Quit()).start();

                        // 5초 지연을 준 후 시작
                        Handler delayHandler = new Handler();
                        delayHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: 나가기 버튼을 눌러 소켓 연결 종료");
                                // 소켓 연결 종료
                                (new Disconnect()).start();
                            }
                        }, 5000);
                        Log.d(TAG, "onOptionsItemSelected: 나가기 버튼을 눌러 액티비티 종료");
                        // 액티비티 종료
                        finish();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 채팅방에서 나가기 아니오를 선택");
                    }
                });
        builder.show();
    }

    // 채팅방 접속 횟수를 저장하는 메소드
    private void setChatRoomAccessCount(String room_idx) {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .setChatRoomAccessCount(getSharedToken(), room_idx);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 채팅방 접속 횟수 저장 응답 성공");

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 채팅방 접속 횟수 저장 응답 실패");

            }
        });

    }

    // 서비스에서 보낸 데이터를 전달받는 핸들러
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        // @Override
        public void handleMessage(Message msg) {
            String received_data = msg.getData().getString("received_data");
            Log.d(TAG, "handleMessage: " + received_data);

            // 서버에서 온 메세지 데이터 수신
            // &를 기준으로 메세지를 분류
            String[] dataArray = received_data.split("&");
//                    if (dataArray[0].equals("join")) {
            Log.d(TAG, "run: 채팅 액티비티에서 서비스로부터 받은 데이터: " + received_data);


            chat_type = dataArray[0];
            chat_room_number = dataArray[1];
            chat_message = dataArray[2];
            chat_time = dataArray[3];
            chat_id = dataArray[4];
            chat_name = dataArray[5];
            chat_profile_image_url = dataArray[6];

            // SQLite DB 생성
            createDB(chat_room_number);

            // 본인에게 보이는 입장메세지일 경우
            if (chat_type.equals("join") && chat_id.equals(my_id)) {
                chat_message = chat_name + "님 환영합니다! \n 건전하고 매너있는 채팅 부탁드립니다.";
            }
            Log.d(TAG, "run: 채팅 타입: " + chat_type);
            Log.d(TAG, "run: 채팅 내용: " + chat_message);
            Log.d(TAG, "run: 채팅 작성 시간: " + chat_time);
            Log.d(TAG, "run: 아이디: " + chat_id);
            Log.d(TAG, "run: 이름: " + chat_name);
            Log.d(TAG, "run: 프로필 이미지 url: " + chat_profile_image_url);

            ChattingModel model = new ChattingModel(chat_type, chat_message, chat_time, chat_id, chat_name, chat_profile_image_url);
            // 채팅 내용 ArrayList에 추가
            Log.d(TAG, "run: 추가 전 채팅 목록: " + chatArrayList);

            Log.d(TAG, "handleMessage: 서버에서 메세지와 함께 받은 채팅방 번호(채팅이 도착해야할 방번호): " + chat_room_number);
            Log.d(TAG, "handleMessage: db에서 가져온 채팅방 번호 idx(현재 내가 보고 있는 채팅방의 방번호): " + idx);
            Log.d(TAG, "handleMessage: sqlite 테이블 이름: " + table_name);

            // 내가 보고 있는 채팅방의 방번호와 채팅이 도착해야 할 곳의 방번호가 같으면 채팅을 보여준다.
            if (idx.equals(chat_room_number)) {
                chatArrayList.add(model);
                // 리사이클러뷰에 새롭게 추가된 채팅 내용이 보이게 함
                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemInserted(chatArrayList.size() - 1);
            }

            Log.d(TAG, "run: 추가 후 채팅 목록: " + chatArrayList);
            // 데이터를 SQLite 테이블에 집어넣음
            chatDB.execSQL("INSERT INTO " + table_name
                    + " (chat_type, chat_message, chat_time, chat_id, chat_name, chat_profile_image_url)  " +
                    "Values ('" + chat_type + "', '" + chat_message + "', '" + chat_time + "', '" + chat_id + "', '" + chat_name + "', '" + chat_profile_image_url + "');");

            Log.d(TAG, "run: 어댑터 갱신");

            // 스크롤을 제일 하단으로 내림
            Log.d(TAG, "run: 스크롤을 제일 하단으로 내림");
            Log.d(TAG, "run: 소켓이 연결되어 있는가: " + socket.isConnected());
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);

            // 나가기를 눌러서 isQuit = true일 경우
            if (isQuit) {
                Log.d(TAG, "run: 삭제되는 테이블 이름: " + table_name);
                // SQLite 테이블에 저장된 채팅 데이터 삭제
                chatDB.execSQL("DELETE FROM " + table_name);
            }


        }


    };

    private void getSocket() {
        Log.d(TAG, "run: socketService에서 소켓 가져오기 실행");
        // 서비스에서 연결된 소켓을 가져옴
        Log.d(TAG, "run: socketService.socket: " + socketService.socket);
        socket = socketService.socket;
        Log.d(TAG, "run: Connect에서 socket: " + socket);
        // 마찬가지로 서비스에서 만든 PrintWriter 객체
        pw = socketService.pw;
        Log.d(TAG, "run: Connect에서 pw: " + pw);

    }

    private void getChatRoomInfo(Intent intent) {
        // 완료버튼을 눌러 방을 생성과 동시에 입장한 경우에는 생성된 방에 대한 정보를 가져와 인덱스를 얻고,
        // 그렇지 않은 경우 어댑터에서 클릭한 포지션에 따라 인덱스를 얻는다.
        if (from_create_activity == 100) {
            Log.d(TAG, "onCreate: 방을 생성한 뒤 입장했기 때문에 생성된 방에 대한 정보를 가져와 인덱스 받음");
            // 생성된 방에 대한 정보를 가져옴
            fetchOneChatRoomInfo();
            // 소켓 연결 스레드 실행

        } else {
            Log.d(TAG, "onCreate: 채팅방 목록을 눌러 입장했기 때문에 포지션에 맞는 인덱스 받음 ");
            // 방 인덱스 번호
            idx = intent.getStringExtra("idx");
            Log.d(TAG, "onCreate: 채팅방에 입장한 유저가 넘긴 방 인덱스 번호 idx: " + idx);
            // 채팅방 접속 횟수 (처음 접속일 경우 1이 채팅방 접속 횟수가 됨)
            access_count = intent.getIntExtra("access_count", 0) + 1;
            Log.d(TAG, "onCreate: 채팅방 접속 횟수: " + access_count);
            // 두번째 채팅방 접속부터는 SQLite에 저장된 채팅 내용을 가져와 리사이클러뷰에 보여준다.
            // SQLite table 이름
            table_name = "room" + idx;
            Log.d(TAG, "onCreate: 방에 들어오는 유저일때 테이블 이름: " + table_name);
            if (access_count > 1) {
                Log.d(TAG, "onCreate: 두번째 이상 채팅방 접속");
                chatArrayList = showList();
            }
            // db에 방 접속 횟수 저장
            setChatRoomAccessCount(idx);
            // 소켓 연결 스레드 실행
            Log.d(TAG, "onServiceConnected: request 실행");
            // create, join, rejoin 등의 요청을 서버에 보냄
            Request request = new Request();
            request.start();

        }

    }

    private void getMyInfo(Intent intent) {
        // 내 아이디
        my_id = intent.getBundleExtra("bundle").getString("my_id");
        Log.d(TAG, "onCreate: 내 아이디: " + my_id);
        // 내 프로필 이미지
        my_profile_image_url = intent.getBundleExtra("bundle").getString("my_profile_image_url");
        Log.d(TAG, "onCreate: my_profile_image_url: " + my_profile_image_url);
        // 내 이름
        my_name = intent.getBundleExtra("bundle").getString("my_name");
        // CreateChattingRoomActivity에서 채팅방에 입장한 것인지 아닌지 판별하기 위해 사용(채팅방을 만든 것인지 아닌지 판단)
        from_create_activity = intent.getIntExtra("from", 0);
        Log.d(TAG, "onCreate: from_create_activity: " + from_create_activity);
    }



}

