package com.example.wooban;

import android.os.Parcel;
import android.os.Parcelable;

public class ChattingModel implements Parcelable{
    // 채팅 타입, 채팅 메세지, 채팅 작성 시간, id, 이름, 프로필 이미지 url
    private String type, message, time, id, name, profile_image_url;


    public ChattingModel(String type, String message, String time, String id, String name, String profile_image_url) {
        this.type = type;
        this.message = message;
        this.time = time;
        this.id = id;
        this.name = name;
        this.profile_image_url = profile_image_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}




