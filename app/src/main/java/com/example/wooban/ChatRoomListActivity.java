package com.example.wooban;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomListActivity extends AppCompatActivity {

    private Animation fabShow, fabHide, fabLeanShow, fabLeanHide;
    private static final String TAG = "ChatRoomListActivity";
    private ArrayList<ChatRoomListModel> chatRoomData = new ArrayList<>() ;
    private RecyclerView recyclerView;
    private JsonArray chatRoomInfoArray;
    private ChatRoomListAdapter adapter;
    private String chat_room_idx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room_list);
        Log.d(TAG, "onCreate: 실행");

        // 툴바
        Toolbar mToolbar = findViewById(R.id.chatting_room_list_toolbar);
        setSupportActionBar(mToolbar);
        // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 툴바 제목
        getSupportActionBar().setTitle("오픈채팅 방 목록");

        // 메인 액티비티에서 받은 번들 객체(내 아이디, 이름, 프로필 이미지)
        Intent intent = getIntent();
        final Bundle bundle = intent.getBundleExtra("bundle");


        // 채팅방 만들기 버튼
        Button createRoomButton = findViewById(R.id.chatting_room_list_create_room_button);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 채팅방 만들기 클릭됨");
                Intent intent = new Intent(ChatRoomListActivity.this, CreateChattingRoomActivity.class);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }
        });


        /** 탭 레이아웃(나중에 탭 레이아웃 사용시 사용) */
//        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs) ;
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            // 탭이 선택된 경우
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                Log.d(TAG, "onTabUnselected: 탭 선택됨");
//                int position = tab.getPosition() ;
//                changeView(position) ;
//            }
//
//            // 탭이 선택되지 않은 경우
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                Log.d(TAG, "onTabUnselected: 탭 선택되지 않음");
//
//            }
//
//            // 선택된 탭을 다시 클릭할 경우
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//                Log.d(TAG, "onTabReselected: 탭 다시 클릭됨");
//
//            }
//
//            /// ... 코드 생략
//        }) ;



        /* 리사이클러뷰 */

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.chatting_room_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)) ;

        // 리사이클러뷰에 어댑터 객체 지정.
        adapter = new ChatRoomListAdapter(ChatRoomListActivity.this, getApplicationContext(), bundle, chatRoomData) ;
        recyclerView.setAdapter(adapter) ;

        // 채팅방 정보를 불러옴
        fetchChatRoomInfo();



        /** 플로팅 버튼(나중에 사용할것) */

//        // 플러스 버튼이 나타나는 애니메이션
//        fabShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_button);
//        fabHide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_button);
//        // 버튼이 기울어지는 애니메이션
//        fabLeanShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_layout);
//        fabLeanHide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_layout);
//        // 플러스 플로팅 버튼
//        final FloatingActionButton plusButton = findViewById(R.id.chatting_room_list_plus_button);
//        // 채팅 아이콘 플로팅 버튼
//        final FloatingActionButton addButton = findViewById(R.id.chatting_room_list_add_button);
//        // 채팅 아이콘 옆 텍스트
//        final TextView addText = findViewById(R.id.chatting_room_list_add_text_view);
//        // 플러스 버튼 클릭시 채팅 추가 버튼이 나타나게 함
//        plusButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: 플러스 버튼 클릭됨");
//                Log.d(TAG, "onClick: plusButton.isSelected(): "+plusButton.isSelected());
//                // 버튼이 처음 눌렸을 때(플러스 버튼이 나옴)
//                if(!v.isSelected()) {
//                    Log.d(TAG, "onClick: 플러스 버튼 클릭 상태");
//                    plusButton.startAnimation(fabLeanShow);
//                    addText.startAnimation(fabShow);
//                    addButton.startAnimation(fabShow);
//                    addButton.setClickable(true);
//                    v.setSelected(true);
//                } else {
//                    Log.d(TAG, "onClick: 플러스 버튼 클릭되지 않은 상태");
//                    plusButton.startAnimation(fabLeanHide);
//                    addText.startAnimation(fabHide);
//                    addButton.startAnimation(fabHide);
//                    addButton.setClickable(false);
//                    v.setSelected(false);
//                }
//
//            }
//        });


    }

    // 메뉴 리소스 XML 파일(main_menu)을 툴바에 표시
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.chatting_room_list_menu, menu);
//        return true;
//    }

    // 툴바의 버튼들을 클릭했을때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 뒤로가기 버튼 눌렀을 때
            case android.R.id.home: {
                finish();
                break;
            }
            // 오른쪽 메뉴에서 오픈채팅을 눌렀을 때
//            case R.id.chatting_room_list_open_chatting: {
//
//
//            }
        }
        return super.onOptionsItemSelected(item);
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
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: 실행");
        // 리스트를 비움
        chatRoomData.clear();
        // 채팅방 정보를 불러옴
        fetchChatRoomInfo();

    }

    /** 탭 레이아웃에 필요(나중에 탭 레이아웃 사용시 사용) */
    // 탭에 따라 다른 뷰가 나타나게 하는 메소드
//    private void changeView(int index) {
//        TextView textView2 = (TextView) findViewById(R.id.text2) ;
//
//        switch (index) {
//            case 0 :
//                Log.d(TAG, "changeView: 케이스 0");
//                recyclerView.setVisibility(View.VISIBLE) ;
//                textView2.setVisibility(View.INVISIBLE) ;
//                break ;
//            case 1 :
//                Log.d(TAG, "changeView: 케이스 1");
//                recyclerView.setVisibility(View.INVISIBLE) ;
//                textView2.setVisibility(View.VISIBLE) ;
//                break ;
//
//        }
//    }

    // 방 정보를 불러오는 메소드
    private void fetchChatRoomInfo() {
        Log.d(TAG, "fetchChatRoomInfo: 실행");

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchChatRoomInfo(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {


            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 방 정보를 가져오는 요청 응답 성공");
                JsonObject data = response.body();
                // 가져온 JsonArray
                if (data != null) {
                    chatRoomInfoArray = data.getAsJsonArray("chat_room_info");
                }
                Log.d(TAG, "onResponse: chatRoomInfoArray: "+chatRoomInfoArray);

                // JsonArray 안에 있는 JsonObject를 꺼내 파싱
                for (JsonElement object : chatRoomInfoArray) {
                    ChatRoomListModel chatInfo = new Gson().fromJson(object, ChatRoomListModel.class);
                    chat_room_idx = chatInfo.idx;
                    Log.d(TAG, "onResponse: chatInfo.idx: "+chatInfo.idx);
                    Log.d(TAG, "onResponse: chatInfo.title: "+chatInfo.title);
                    Log.d(TAG, "onResponse: chatInfo.tag: "+chatInfo.tag);
                    Log.d(TAG, "onResponse: chatInfo.writerId: "+chatInfo.writerId);
                    Log.d(TAG, "onResponse: chatInfo.writerName: "+chatInfo.writerName);
                    Log.d(TAG, "onResponse: chatInfo.writerProfileImage: "+chatInfo.writerProfileImage);
                    Log.d(TAG, "onResponse: chatInfo.peopleNumber: "+chatInfo.peopleNumber);
                    Log.d(TAG, "onResponse: chatInfo.descriptionImage: "+chatInfo.descriptionImage);

                    // 방 제목, 태그, 작성자 id, 이름, 프로필 이미지, 방 참여인원수, 방 소개 이미지 url을 생성자에 넣은 모델 생성
                    ChatRoomListModel list = new ChatRoomListModel(chatInfo.idx, chatInfo.title, chatInfo.tag, chatInfo.writerId, chatInfo.writerName,
                            chatInfo.writerProfileImage, chatInfo.peopleNumber, chatInfo.descriptionImage);
                    // 리스트에 모델 추가
                    chatRoomData.add(list);
                    Log.d(TAG, "onResponse: 리스트에 모델 추가");

                }
                Log.d(TAG, "onResponse: 어댑터 갱신");
                // 어댑터 갱신
                adapter.notifyDataSetChanged();



            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 방 정보를 가져오는 요청 응답 실패");

            }
        });
    }


    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }


}
