package com.example.wooban;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawer; // 드로어 레이아웃 객체
    private ActionBarDrawerToggle toggle; // 액션바의 토글 객체
    Toolbar toolbar;
    // 네비게이션 뷰의 환영인사(~님 환영합니다!), 인삿말
    TextView textWelcome;
    String name, profile_image_url;
    private RecyclerView recyclerView;
    private JsonObject jsonObject;
    ImageView profileImageView;
    // 내 프로필 이미지와 이름을 저장할 번들
    Bundle bundle = new Bundle();
    private String my_id;
    // SQLite 테이블 이름
    String table_name = "refresh_token_info";
    // db 이름
    String db_name = "token_db";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: 실행");

        // 토큰을 발급받은 경우
        if (getSharedToken() != null) {

            // 네비게이션 뷰 객체 생성
            NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
            // 네비게이션 뷰의 헤더 뷰
            View headerView = navigationView.getHeaderView(0);
            // 네비게이션 뷰 리스너 세팅
            navigationView.setNavigationItemSelectedListener(this);
            // 네비게이션 뷰 안에 있는 텍스트뷰 연결
            textWelcome = headerView.findViewById(R.id.main_navigation_header_welcome);
            // 네비게이션 뷰 안에 있는 프로필 이미지뷰 연결
            profileImageView = headerView.findViewById(R.id.navigation_header_profile_image);

            // 툴바
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            //액션바와 같게 만들어줌
            setSupportActionBar(toolbar);
            // 툴바 제목 제거
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // 툴바 홈버튼 이미지 변경
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

            // DrawerLayout 객체 생성
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

            // access_token이 만료되었을 경우 토큰을 재발급받고 아이디, 이름, 프로필 이미지 데이터를 요청함
            renewAccessToken();

            // 액션바 토글 객체 생성
            // 드로어를 포함하는 액티비티, 액티비티의 액션바와 연동할 드로어 객체, 액션바, '드로어 열기' 에 해당하는 문자열 리소스, '드로어 닫기' 에 해당하는 문자열 리소스
            toggle = new ActionBarDrawerToggle(this, drawer,
                    toolbar, R.string.open_drawer, R.string.close_drawer) {


                // 드로어가 닫혔을 때 이벤트
                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }

                // 드로어가 열렸을 때 이벤트
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

            };
            // 드로어와 액션바 토글 연결
            drawer.addDrawerListener(toggle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 뒤로가기 버튼 활성화(툴바 왼쪽의 메뉴 버튼을 만들기 위한 것)

        } else {
            Log.d(TAG, "onCreate: 토큰이 없어 로그인 액티비티로 이동");
            // 로그인 액티비티로 이동
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


    } // onCreate 종료

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: 실행");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: 실행");
        renewAccessToken();
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

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: 실행");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 실행");
    }

    private void getData() {
        Log.d(TAG, "getData: getData() 실행");

        // 레트로핏 객체
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .getMainData(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {

            // 응답 성공시
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "응답 성공");
                // 서버에서 받아온 데이터
                JsonObject data = response.body();
                Log.d(TAG, "onResponse: data: " + data);
                // 토큰으로 정상적으로 로그인 된 경우
                if (data != null) {
                    // 이름
                    name = data.get("name").getAsString();
                    // id
                    my_id = data.get("id").getAsString();
                    // 프로필 이미지 url
                    profile_image_url = data.get("profile_image").getAsString();

                    /* 프로필 이미지가 default라는 단어를 포함하면 기본 이미지가 나타나고,
                     그렇지 않으면 본인이 선택한 이미지가 나타남 */
                    if (profile_image_url.contains("default")) {
                        Log.d(TAG, "null onResponse: profile_image_url: " + profile_image_url);
                        // 기본 이미지 중 선택한 이미지를 이미지뷰에 넣어 보여준다.
                        DefaultProfileImage defaultImage = new DefaultProfileImage();
                        defaultImage.changeToAnimal(MainActivity.this, profile_image_url, profileImageView);

                    } else {
                        Log.d(TAG, "onResponse: profile_image_url: " + profile_image_url);
                        Glide.with(MainActivity.this).load(profile_image_url).apply(RequestOptions.circleCropTransform()).into(profileImageView);

                    }
                    Log.d(TAG, "onResponse: name: " + name);
                    Log.d(TAG, "onResponse: id: " + my_id);

                    // 내 프로필 이미지와 이름, id를 번들에 저장
                    bundle.putString("my_profile_image_url", profile_image_url);
                    // Log.d(TAG, "onResponse: my_profile_image_url: " + profile_image_url);
                    bundle.putString("my_name", name);
                    // Log.d(TAG, "onResponse: name: " + name);
                    bundle.putString("my_id", my_id);
                    // Log.d(TAG, "onResponse: my_id: " + my_id);

                    // 환영메세지
                    textWelcome.setText(name + "님, \n즐거운 하루 되세요!");
                    // 유효한 토큰임을 쉐어드에 저장
                    Log.d(TAG, "onResponse: 유효한 토큰임을 쉐어드에 저장");
                    SharedPreferenceUtil sharedPreference = new SharedPreferenceUtil(MainActivity.this);
                    sharedPreference.setSharedBoolean("validToken", true);

                    // 비디오 정보를 불러옴
                    fetchVideoInfo();

                }


            }

            // 응답 실패시 (토큰이 없는 상황, 로그아웃 상태)
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "응답 실패");


            }
        });


    }

    // 영상 정보를 불러옴
    private void fetchVideoInfo() {
        Log.d(TAG, "fetchVideoInfo: 실행");

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchVideoInfo(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {


            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: fetchVideoInfo 응답 성공");
                jsonObject = response.body();
                Log.d(TAG, "onResponse: jsonObject: " + jsonObject);

                // 리사이클러뷰 객체 생성 및 레이아웃매니저 지정
                recyclerView = findViewById(R.id.main_recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));


                Log.d(TAG, "onCreate: 어댑터 객체 생성 및 리사이클러뷰에 어댑터 장착");
                // 어댑터 객체 생성 및 리사이클러뷰에 어댑터 장착
                MainAdapter adapter = new MainAdapter(getApplicationContext(), jsonObject, bundle, MainActivity.this);
                Log.d(TAG, "onResponse: 번들 객체: " + bundle);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: fetchVideoInfo 응답 실패");

            }
        });
    }


    // 메뉴 리소스 XML 파일(main_menu)을 툴바에 표시
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }


    // 드로어가 열려있는 상태에서 뒤로가기 버튼을 누르면 드로어가 닫힌다.
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // 네비게이션 드로어 클릭 이벤트
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {

            // 영상 업로드 및 스트리밍 클릭
            case R.id.navigation_menu_vod_upload:
                Log.d(TAG, "영상 업로드 밎 스트리밍 시작 클릭");
                Intent videoUpload = new Intent(MainActivity.this, VideoUploadActivity.class);
                videoUpload.putExtra("profile_image_url", profile_image_url);
                Log.d(TAG, "onNavigationItemSelected: profile_image_url: " + profile_image_url);
                videoUpload.putExtra("name", name);
                Log.d(TAG, "onNavigationItemSelected: name: " + name);
                startActivity(videoUpload);
                break;

            // 나중에 볼 영상 클릭
            case R.id.navigation_menu_watch_later:
                Log.d(TAG, "나중에 볼 영상 클릭");
                Intent watchLaterIntent = new Intent(MainActivity.this, WatchLaterActivity.class);
                watchLaterIntent.putExtra("bundle", bundle);
                startActivity(watchLaterIntent);
                break;

            // 채팅 클릭
            case R.id.navigation_menu_chatting:
                Log.d(TAG, "채팅 클릭");
                // 채팅 액티비티로 이동
                Intent chattingIntent = new Intent(MainActivity.this, MyChatRoomListActivity.class);
                chattingIntent.putExtra("bundle", bundle);
                startActivity(chattingIntent);
                break;

            case R.id.navigation_menu_setting:
                Log.d(TAG, "설정 클릭");
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                // 프로필 이미지 url을 넣어 전송
                settingIntent.putExtra("profile_image_url", profile_image_url);
                startActivity(settingIntent);
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // access_token을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // access_token라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }

    // access_token 발급시간을 가져오는 함수
    private long getAccessTokenIssuedTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // access_token_expire_time라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        return sharedPreferences.getLong("access_token_issued_time", System.currentTimeMillis());
    }

    // access_token 만료시간을 가져오는 함수
    private long getAccessTokenExpireTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // access_token_expire_time라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        return sharedPreferences.getLong("access_token_expire_time", System.currentTimeMillis());
    }

    // refresh_token 만료시간을 가져오는 함수
    private long getRefreshTokenExpireTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // access_token_expire_time라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        return sharedPreferences.getLong("refresh_token_expire_time", System.currentTimeMillis());
    }


    // shared에 저장된 id를 가져오는 함수
    private String getId() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // id라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        return sharedPreferences.getString("id", null);
    }

    // 로그인 정보를 가져오는 함수
    private Boolean getLoginState() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // login 이라는 key에 저장된 값이 있는지 확인. 값이 없으면 기본값인 false를 반환
        boolean login = sharedPreferences.getBoolean("login", false);
        return login;

    }

    private String readDB() {
        String read_refresh_token = null;
        Log.d(TAG, "readDB: 실행");
        try {

            SQLiteDatabase ReadDB = this.openOrCreateDatabase(db_name, MODE_PRIVATE, null);
            // SELECT문을 사용하여 테이블에 있는 데이터를 가져옵니다..
            Cursor c = ReadDB.rawQuery("SELECT * FROM " + table_name, null);

            if (c != null) {

                if (c.moveToFirst()) {
                    do {

                        //테이블에서 컬럼값을 가져와서
                        read_refresh_token = c.getString(c.getColumnIndex("refresh_token"));
                        Log.d(TAG, "readDB: read_refresh_token: " + read_refresh_token);

                    } while (c.moveToNext());
                }
            }

            ReadDB.close();

        } catch (SQLiteException se) {
            Toast.makeText(getApplicationContext(), se.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("", se.getMessage());
        }
        return read_refresh_token;
    }

    // access_token과 refresh_token을 서버에 전송
    private void renewAccessTokenRetro(String refresh_token, String id) {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .renewAccessToken(getSharedToken(), refresh_token, id);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            // 전송한 refresh_token과 서버db에 저장된 refresh_token을 비교하여 같으면 새로운 access_token을 발급
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: refresh_token 전송 응답 성공");
                JsonObject data = response.body();
                Log.d(TAG, "onResponse: 응답 코드: " + response.code());
                Log.d(TAG, "onResponse: 요청이 성공하였는가? " + response.isSuccessful());
                Log.d(TAG, "onResponse: data: " + data);
                if (response.isSuccessful() && data != null) {
                    // 재발급 받은 access_token
                    String access_token = data.get("access_token").getAsString();
                    // 재발급 받은 access_token 발급시간
                    long access_token_issued_time = 1000 * data.get("access_token_issued_time").getAsLong();
                    // 재발급 받은 access_token 만료시간
                    long access_token_expire_time = 1000 * data.get("access_token_expire_time").getAsLong();
                    // refresh_token 만료시간
                    long refresh_token_expire_time = 1000 * data.get("refresh_token_expire_time").getAsLong();
                    // shared에 access_token 저장
                    SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("access_token", access_token);
                    editor.putLong("access_token_expire_time", access_token_expire_time);
                    editor.putLong("access_token_issued_time", access_token_issued_time);
                    editor.putLong("refresh_token_expire_time", refresh_token_expire_time);
                    editor.apply();
                    // 새롭게 발급받은 access_token으로 아이디, 이름, 프로필 이미지 데이터 요청
                    getData();
                } else {
                    Log.d(TAG, "onResponse: refresh_token이 만료되어 요청이 실패한 경우");
                    // 로그인 액티비티로 이동
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: refresh_token 전송 응답 실패");

            }
        });
    }

    // 토큰이 만료되었으면 access_token을 갱신하는 요청
    private void renewAccessToken() {
        // 현재 시간
        long current_time = System.currentTimeMillis();
        // shared에 저장된 access_token 만료시간
        long access_token_expire_time = getAccessTokenExpireTime();
        Log.d(TAG, "onCreate: 현재 시간 밀리세컨드: " + current_time);
        Log.d(TAG, "onCreate: access_token 만료시간 밀리세컨드: " + access_token_expire_time);
        Log.d(TAG, "renewAccessToken: 현재 시간: " + longTypeTimeToDate(current_time));
        Log.d(TAG, "renewAccessToken: access_token 발급시간: " + longTypeTimeToDate(getAccessTokenIssuedTime()));
        Log.d(TAG, "renewAccessToken: access_token 만료시간: " + longTypeTimeToDate(access_token_expire_time));
        Log.d(TAG, "renewAccessToken: refresh_token 만료시간: " + longTypeTimeToDate(getRefreshTokenExpireTime()));

        // 현재 시간이 access_token이 만료되는 시점을 지났을 때
        if (current_time >= access_token_expire_time) {
            Log.d(TAG, "renewAccessToken: 토큰이 만료됨");
            // SQLite에 저장된 refresh_token을 불러옴
            String refresh_token = readDB();
            Log.d(TAG, "onCreate: refresh_token: " + refresh_token);
            // access_token과 refresh_token, id를 서버에 전송
            renewAccessTokenRetro(refresh_token, getId());
            // 토큰이 만료되지 않았을 경우 바로 아이디, 이름, 프로필 이미지 데이터를 불러옴
        } else {
            Log.d(TAG, "renewAccessToken: 토큰이 만료되지 않음");
            getData();
        }
    }

    private String longTypeTimeToDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        return format.format(date);
    }


}
