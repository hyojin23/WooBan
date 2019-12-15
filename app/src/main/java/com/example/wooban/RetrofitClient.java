package com.example.wooban;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://54.180.104.190/android/";
    private static RetrofitClient mInstance;
//    private static Retrofit retrofit = null;
    private Retrofit retrofit;

    // 모든 요청의 헤더에 토큰 추가
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            // CustomInterceptor 익명 객체 인자로 받음
            .addInterceptor(new CustomInterceptor()) // This is used to add ApplicationInterceptor.
            .build();

    // 레트로핏 디버깅 위한 HttpLoggingInterceptor
    public static OkHttpClient getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        return client;
    }



    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getClient())
//                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 실제로 생성되는 객체는 단 하나. 생성자가 여러 번 호출되어도 최초 생성한 객체를 반환함
    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }

}
