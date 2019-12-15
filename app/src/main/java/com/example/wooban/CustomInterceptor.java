package com.example.wooban;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CustomInterceptor implements Interceptor {

    private String token;

    @Override
    public Response intercept(Chain chain) throws IOException {

        // 헤더에 추가될 내용
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer "+token)
                .add("Content-Type", "application/json")
                .build();


        /*
        chain.request() returns original request that you can work with(modify, rewrite)
        */
        // 작동할 요청
        Request request = chain.request().newBuilder().headers(headers).build();
        // Here you can rewrite the request



        /*
        chain.proceed(request) is the call which will initiate the HTTP work. This call invokes the
        request and returns the response as per the request.
        */
        // 요청을 넣어 실행
        Response response = chain.proceed(request);
        //Here you can rewrite/modify the response


        // 결과 리턴
        return response;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

