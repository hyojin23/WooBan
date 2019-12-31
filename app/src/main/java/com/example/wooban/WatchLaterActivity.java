package com.example.wooban;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchLaterActivity extends AppCompatActivity {

    ArrayList<WatchLaterVideoModel> videoInfoList = new ArrayList<>();
    private final static String TAG = "WatchLaterActivity";
    private JsonArray watchLaterVideoArray;
    ImageView profileImageView;
    TextView nameTextView;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_later);

        // 툴바
        Toolbar mToolbar = (Toolbar) findViewById(R.id.watch_later_toolbar);
        setSupportActionBar(mToolbar);

        // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 툴바 제목
        getSupportActionBar().setTitle("나중에 볼 동영상");

        // 뷰 참조
        profileImageView = findViewById(R.id.watch_later_profile_image);
        nameTextView = findViewById(R.id.watch_later_my_name);

        // 인텐트로 받은 데이터
        Intent intent = getIntent();
        bundle = intent.getBundleExtra("bundle");
        // 프로필 이미지 url
        String profile_image = bundle.getString("my_profile_image_url");
        // 이름
        String name = bundle.getString("my_name");

        /* 뷰에 나타냄 */
        // 프로필 이미지
        Glide.with(this).load(profile_image).apply(RequestOptions.circleCropTransform()).into(profileImageView);
        // 이름
        nameTextView.setText(name);

        getWatchLaterVideoInfo();

    }

    // 나중에 볼 동영상 정보를 불러옴
    private void getWatchLaterVideoInfo() {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .getWatchLaterVideoInfo(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 나중에 볼 영상 정보 불러오기 성공");
                JsonObject data = response.body();
                // 가져온 JsonArray
                if (data != null) {
                    watchLaterVideoArray = data.getAsJsonArray("watch_later_video_info");
                }
                Log.d(TAG, "onResponse: watchLaterVideoArray: "+watchLaterVideoArray);

                // JsonArray 안에 있는 JsonObject를 꺼내 파싱
                for (JsonElement object : watchLaterVideoArray) {
                    WatchLaterVideoModel videoInfo = new Gson().fromJson(object, WatchLaterVideoModel.class);

                    // 리스트에 모델 추가
                    videoInfoList.add(videoInfo);
                    Log.d(TAG, "onResponse: 리스트에 모델 추가");

                }

                // 리사이클러뷰에 LinearLayoutManager 객체 지정.
                RecyclerView recyclerView = findViewById(R.id.watch_later_recycler_view) ;
                recyclerView.setLayoutManager(new LinearLayoutManager(WatchLaterActivity.this)) ;

                // 리사이클러뷰에 WatchLaterAdapter 객체 지정.
                WatchLaterAdapter adapter = new WatchLaterAdapter(videoInfoList, bundle, getApplicationContext(), WatchLaterActivity.this) ;
                recyclerView.setAdapter(adapter) ;
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 나중에 볼 영상 정보 불러오기 실패");

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

    // 메뉴 선택시
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 툴바의 back키 눌렀을 때 동작
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
