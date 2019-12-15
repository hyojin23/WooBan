package com.example.wooban;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/* 로그인에 성공하면 서버에서 토큰을 발급받는다. 받은 토큰을 Shared에 저장하고 액티비티를 종료한다.*/

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = " LoginActivity";
    EditText editId, editPassword;
    SQLiteDatabase tokenDB = null;
    // SQLite 테이블 이름
    String table_name = "refresh_token_info";
    // db 이름
    String db_name = "token_db";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // 아이디와 비밀번호 입력란
        editId = findViewById(R.id.login_id);
        editPassword = findViewById(R.id.login_password);


        // 클릭 리스너
        findViewById(R.id.do_sign_up).setOnClickListener(this);
        findViewById(R.id.login_button).setOnClickListener(this);
        findViewById(R.id.login_layout).setOnClickListener(this);

    }

//    private void userSignUp() {
//        String email = editTextId.getText().toString().trim();
//        String password = editTextPassword.getText().toString().trim();
//
//
//        if (email.isEmpty()) {
//            editTextId.setError("Email is required");
//            editTextId.requestFocus();
//            return;
//        }
//
//
//    }


    private void login() {

        Log.d(TAG, "login: login() 실행");
        // 입력된 아이디
        final String id = editId.getText().toString().trim();
        // 입력된 비밀번호
        String password = editPassword.getText().toString().trim();

        // 해쉬맵 객체 생성
        Map<String, String> params = new HashMap<>();

        // 해쉬맵에 key-value 형태로 데이터를 넣는다.
        params.put("id", id);
        params.put("password", password);


        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                // 사용자가 입력한 아이디와 패스워드
                .login(params);


        Log.d(TAG, "login: 레트로핏 콜");
        call.enqueue(new Callback<JsonObject>() {

            // 응답 성공시
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 응답 성공");
                // 서버에서 받아온 데이터
                JsonObject data = response.body();
                Log.d(TAG, "onResponse: 서버에서 받은 data: " + data);

                // 아이디와 비밀번호를 맞게 입력하면 데이터가 null이 아니고, 틀리면 null이 된다.
                if (data != null) {
                    // access_token
                    String access_token = data.get("access_token").getAsString();
                    // refresh_token
                    String refresh_token = data.get("refresh_token").getAsString();
                    // id
                    String id = data.get("id").getAsString();
                    // access_toekn 만료시간 (second를 millisecond로 바꾸기 위해 1000을 곱함. millisecond를 넣어야 제대로 된 날짜로 변경됨)
                    long access_token_expire_time = data.get("access_token_expire_time").getAsLong() * 1000;
                    // refresh_token 만료시간
                    long refresh_token_expire_time = data.get("refresh_token_expire_time").getAsLong() * 1000;
                    // access_toekn 발급시간
                    long access_token_issued_time = data.get("access_token_issued_time").getAsLong() * 1000;


                    // 디버깅용 토스트
//                Toast.makeText(LoginActivity.this, access_token, Toast.LENGTH_SHORT).show();
//                Toast.makeText(LoginActivity.this, id, Toast.LENGTH_SHORT).show();
//                Toast.makeText(LoginActivity.this, access_token_expire_time, Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "access_token: " + access_token);
                    Log.d(TAG, "refresh_token: " + refresh_token);
                    Log.d(TAG, "id: " + id);
                    Log.d(TAG, "access_token_expire_time: " + access_token_expire_time);
                    Log.d(TAG, "access_token_issued_time: " + access_token_issued_time);
                    // 토큰 만료시간 long 타입
                    Date issue_date = new Date(access_token_issued_time);
                    Date expire_date = new Date(access_token_expire_time);
                    Date refresh_expire_date = new Date(refresh_token_expire_time);
                    SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss", Locale.KOREA);
                    String issued_time = format.format(issue_date);
                    String expired_time = format.format(expire_date);
                    String refresh_expired_time = format.format(refresh_expire_date);

                    Log.d(TAG, "onResponse: access_token 발급 시간: " + issued_time);
                    Log.d(TAG, "onResponse: access_token 만료날짜: " + expired_time);
                    Log.d(TAG, "onResponse: refresh_token 만료날짜: " + refresh_expired_time);
                    Log.d(TAG, "onResponse: 현재시간: " +System.currentTimeMillis());


                    // 쉐어드 객체
                    SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
                    //저장을 하기위해 editor 객체 생성
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    // key, value를 이용하여 에디터에 추가

                    // access_tokne 저장
                    editor.putString("access_token", access_token);
                    // access_token 만료시간 저장
                    editor.putLong("access_token_expire_time" , access_token_expire_time);
                    // access_token 발급시간 저장
                    editor.putLong("access_token_issued_time", access_token_issued_time);
                    // refresh_token 만료시간 저장
                    editor.putLong("refresh_token_expire_time", refresh_token_expire_time);
                    // 유저 id 저장
                    editor.putString("id", id);
                    editor.apply();

                    // refresh token 저장을 위한 SQLite db 생성
                    createDB();
                    // refresh token 저장
                    insertDB(refresh_token);



                    // 로그인 액티비티를 종료하고 메인화면으로 이동
                    Toast.makeText(LoginActivity.this, "로그인 되었습니다", Toast.LENGTH_SHORT).show();
                    // 메인 화면으로 이동
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    // 로그인 액티비티 종료
                    finish();

                    // 아이디는 맞는데 비밀번호가 틀린 경우
                } else {
                    Log.d(TAG, "onResponse: 아이디와 비밀번호가 일치하지 않아 로그인 실패");
                    Toast.makeText(LoginActivity.this, "로그인 실패: \n아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
                }


//
                //            finish();
//                String token = getToken();

//                Toast.makeText(LoginActivity.this, token, Toast.LENGTH_SHORT).show();
//                Log.d(TAG, token);

                // 데이터로 받아온 string을 boolean 값으로 변환
//                    boolean check = Boolean.valueOf(data);

//                    if (check) {
//                        Log.d(TAG, "로그인 성공");
//                        Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else {
//                        Log.d(TAG, "로그인 실패");
////                        Toast.makeText(LoginActivity.this, "로그인 실패: \n아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
//
//
//                        // 로그인 실패 토스트(텍스트가 가운데로 정렬된 토스트)
//                        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
//                        if( v != null) v.setGravity(Gravity.CENTER);
//                        toast.show();
//
//
//                    }
//            }


            }

            // 응답 실패시 (없는 아이디 입력시)
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 응답 실패");
                Log.d(TAG, "onResponse: 없는 아이디라 로그인 실패");
                Toast.makeText(LoginActivity.this, "로그인 실패: \n아이디 또는 비밀번호를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 화면 빈공간을 누를 경우
            case R.id.login_layout:
                // 키보드 아래로 내림
                keyBoradDown();
                break;

            // 아직 회원이 아니신가요? 버튼을 누를 경우 회원가입 페이지로 이동
            case R.id.do_sign_up:
                Intent intent = new Intent(this, SignUpActivity.class);
                startActivity(intent);
                break;

            // 로그인 버튼 누를 경우
            case R.id.login_button:
                // 로그인
                login();
//                Intent intent = new Intent(this, SignUpActivity.class);
//                startActivity(intent);
                break;

        }
    }

    // 키보드 내리기
    public void keyBoradDown() {
        // 키보드 내리기
        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        immhide.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    // SQLite db 생성
    private void createDB() {

        try {

            tokenDB = this.openOrCreateDatabase(db_name, MODE_PRIVATE, null);
            // 테이블이 존재하는 경우 기존 데이터를 지우기 위해서 사용합니다.
//            tokenDB.execSQL("DELETE FROM " + table_name  );
            //테이블이 존재하지 않으면 새로 생성합니다.
            Log.d(TAG, "createDB: table_name: " + table_name);
            tokenDB.execSQL("CREATE TABLE IF NOT EXISTS " + table_name
                    + " (refresh_token TEXT);");

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

    // refresh_token SQLite db에 저장
    private void insertDB (String refresh_token) {
        tokenDB.execSQL( "INSERT INTO " + table_name
                + " (refresh_token)  " +
                "Values ('" + refresh_token + "'); " );
    }



}

