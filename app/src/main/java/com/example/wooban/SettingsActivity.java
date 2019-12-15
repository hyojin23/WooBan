package com.example.wooban;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.gson.JsonObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivityaaa";
    private static String access_token;
    private static boolean validToken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Intent intent = getIntent();
        intent.getStringExtra("profile_image_url");
        Log.d(TAG, "onCreate: intent.getStringExtra(\"profile_image_url\"): " + intent.getStringExtra("profile_image_url"));

        Log.d(TAG, "onCreate: 토큰을 얻어옴");
        access_token = getSharedToken();
        Log.d(TAG, "onCreate: 토큰 유효성 체크");
        SharedPreferenceUtil sharedPreference = new SharedPreferenceUtil(SettingsActivity.this);
        validToken = sharedPreference.getSharedBoolean("validToken");
        Log.d(TAG, "onCreate: access_token: " + access_token);
        Log.d(TAG, "onCreate: 토큰이 유효한가: " +validToken);


        // 프레그먼트 실행
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            Log.d(TAG, "onCreatePreferences: SettingsFragment 실행");

            // 로그인 상태이면 설정에서 프로필 편집, 비밀번호 변경, 버튼을 보이게 함
            if (access_token != null && validToken) {
                Log.d(TAG, "onCreatePreferences: access_token != null, 로그인 상태");

                // 프로필 편집
                Preference profileChange = findPreference("profile_change");
                // 비밀번호 변경
                Preference passwordChange = findPreference("password_change");
                // 푸시 알람 받기
                Preference pushAlarm = findPreference("push_alarm");


                // 프로필 편집 버튼 보이게 함
                profileChange.setVisible(true);

                // 프로필 편집 버튼 클릭시
                profileChange.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "onPreferenceClick: 프로필 편집 버튼 클릭");

                        // 프로필 편집화면으로 이동
                        Intent intent = new Intent(getActivity(), ProfileChangeActivity.class);
                        intent.putExtra("profile_image_url", intent.getStringExtra("profile_image_url"));
                        startActivity(intent);
                        return false;
                    }
                });


                // 비밀번호 변경 버튼 보이게 함
                passwordChange.setVisible(true);

                // 비밀번호 변경 버튼 클릭시
                passwordChange.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "onPreferenceClick: 비밀번호 변경 버튼 클릭");

                        return false;
                    }
                });


                // 푸시 알람 버튼을 보이게 함
                pushAlarm.setVisible(true);
                // 푸시 알람 버튼 클릭시
                pushAlarm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "onPreferenceClick: 푸시 알람 버튼 클릭");
                        return false;
                    }
                });


            }

        }


    }

    // 레트로핏. 지금 사용 X
    private void checkToken() {

        // 레트로핏 객체
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .checkToken(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                Log.d(TAG, "onResponse: 응답 성공");
                JsonObject data = response.body();
                // 정상적으로 로그인된 경우
                if (data != null) {
                    Log.d(TAG, "onResponse: 유효한 토큰");
                    Log.d(TAG, "onResponse: response.body(): " + response.body());
                    // 토큰이 유효하면 validToken = true
                    String getString = data.get("valid_token").getAsString();
                    Log.d(TAG, "onResponse: getString: " + getString);
                    validToken = Boolean.valueOf(getString);
                    Log.d(TAG, "onResponse: validToken: " + validToken);

                    // 토큰이 만료된 경우
                } else {
                    Log.d(TAG, "onResponse: 만료된 토큰");
                    Log.d(TAG, "onResponse: response.body(): " + response.body());
                    validToken = false;
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 응답 실패");

            }
        });
    }


    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null을 반환
        String access_token = sharedPreferences.getString("access_token", null);
        return access_token;
    }


}