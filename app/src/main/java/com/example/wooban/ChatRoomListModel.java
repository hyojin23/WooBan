package com.example.wooban;


import com.google.gson.annotations.SerializedName;



public class ChatRoomListModel {
    @SerializedName("idx")
    String idx;
    @SerializedName("title")
    String title;
    @SerializedName("tag")
    String tag;
    @SerializedName("writer_id")
    String writerId;
    @SerializedName("writer_name")
    String writerName;
    @SerializedName("writer_profile_image_url")
    String writerProfileImage;
    @SerializedName("people_number")
    String peopleNumber;
    @SerializedName("room_image_url")
    String descriptionImage;


    public ChatRoomListModel(String idx, String title, String tag, String writerId, String writerName, String writerProfileImage, String peopleNumber, String descriptionImage) {
        this.idx = idx;
        this.title = title;
        this.tag = tag;
        this.writerId = writerId;
        this.writerName = writerName;
        this.writerProfileImage = writerProfileImage;
        this.peopleNumber = peopleNumber;
        this.descriptionImage = descriptionImage;
    }

    // @SerializedName은 테이블의 컬럼명이다.


    public String getIdx() {
        return idx;
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getWriterProfileImage() {
        return writerProfileImage;
    }

    public void setWriterProfileImage(String writerProfileImage) {
        this.writerProfileImage = writerProfileImage;
    }

    public String getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(String peopleNumber) {
        this.peopleNumber = peopleNumber;
    }

    public String getDescriptionImage() {
        return descriptionImage;
    }

    public void setDescriptionImage(String descriptionImage) {
        this.descriptionImage = descriptionImage;
    }
}
