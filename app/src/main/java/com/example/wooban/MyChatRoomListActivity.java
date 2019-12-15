package com.example.wooban;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyChatRoomListActivity extends AppCompatActivity {
    private static final String TAG = "MyChatRoomListActivity";
    private Bundle bundle;
    private JsonArray chatRoomInfoArray;
    private ArrayList<MyChatRoomListModel> chatRoomData = new ArrayList<>() ;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_chat_room_list);

        // 툴바
        Toolbar mToolbar = findViewById(R.id.my_chatting_room_list_toolbar);
        setSupportActionBar(mToolbar);
        // 툴바 제목
        getSupportActionBar().setTitle("내 채팅방 목록");

        // 메인 액티비티에서 받은 내 프로필 이미지와 이름이 담긴 번들
        Intent intent = getIntent();
        bundle = intent.getBundleExtra("bundle");

        /* 리사이클러뷰 */

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.chatting_room_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)) ;

        // 리사이클러뷰에 어댑터 객체 지정.
        adapter = new MyChatRoomListAdapter(MyChatRoomListActivity.this, getApplicationContext(), bundle, chatRoomData) ;
        recyclerView.setAdapter(adapter) ;


        // 내가 들어가 있는 채팅방 정보를 불러옴
        fetchMyChatRoomInfo();


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: 실행");
        fetchMyChatRoomInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 실행");
    }

    // 메뉴 리소스 XML 파일(main_menu)을 툴바에 표시
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatting_room_list_menu, menu);
        return true;
    }

    // 툴바의 버튼들을 클릭했을때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 뒤로가기 버튼 눌렀을 때
            case android.R.id.home: {
                break;
            }
            // 오른쪽 메뉴에서 오픈채팅을 눌렀을 때
            case R.id.chatting_room_list_open_chatting: {
                Log.d(TAG, "onOptionsItemSelected: 채팅방 목록 툴바에서 오픈채팅을 누름");

                // 오픈채팅방 목록 액티비티로 이동
                Intent intent = new Intent(MyChatRoomListActivity.this, ChatRoomListActivity.class);
                intent.putExtra("bundle", bundle);
                startActivity(intent);

            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 내가 들어가 있는 채팅방 정보를 불러오는 메소드
    private void fetchMyChatRoomInfo() {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchMyChatRoomInfo(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 내가 들어가 있는 채팅방 정보 불러오기 응답 성공");
                JsonObject data = response.body();
                if (data != null) {
                    chatRoomInfoArray = data.getAsJsonArray("my_chat_room_info");
                }
                Log.d(TAG, "onResponse: chatRoomInfoArray: "+chatRoomInfoArray);

                // 리스트 초기화
                chatRoomData.clear();
                // JsonArray 안에 있는 JsonObject를 꺼내 파싱
                for (JsonElement object : chatRoomInfoArray) {
                    MyChatRoomListModel chatInfo = new Gson().fromJson(object, MyChatRoomListModel.class);
                    Log.d(TAG, "onResponse: chatInfo.chat_room_idx: "+chatInfo.chatRoomIdx);
                    Log.d(TAG, "onResponse: chatInfo.title: "+chatInfo.title);
                    Log.d(TAG, "onResponse: chatInfo.tag: "+chatInfo.tag);
                    Log.d(TAG, "onResponse: chatInfo.writerId: "+chatInfo.writerId);
                    Log.d(TAG, "onResponse: chatInfo.writerName: "+chatInfo.writerName);
                    Log.d(TAG, "onResponse: chatInfo.writerProfileImage: "+chatInfo.writerProfileImage);
                    Log.d(TAG, "onResponse: chatInfo.peopleNumber: "+chatInfo.peopleNumber);
                    Log.d(TAG, "onResponse: chatInfo.descriptionImage: "+chatInfo.descriptionImage);

                    // 방 제목, 태그, 작성자 id, 이름, 프로필 이미지, 방 참여인원수, 방 소개 이미지 url을 생성자에 넣은 모델 생성
                    MyChatRoomListModel list = new MyChatRoomListModel(chatInfo.chatRoomIdx, chatInfo.title, chatInfo.tag, chatInfo.writerId, chatInfo.writerName,
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
                Log.d(TAG, "onResponse: 내가 들어가 있는 채팅방 정보 불러오기 응답 실패");

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
