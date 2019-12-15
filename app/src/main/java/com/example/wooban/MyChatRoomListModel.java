package com.example.wooban;


import com.google.gson.annotations.SerializedName;



public class MyChatRoomListModel {
    @SerializedName("chat_room_idx")
    String chatRoomIdx;
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


    public MyChatRoomListModel(String idx, String title, String tag, String writerId, String writerName, String writerProfileImage, String peopleNumber, String descriptionImage) {
        this.chatRoomIdx = idx;
        this.title = title;
        this.tag = tag;
        this.writerId = writerId;
        this.writerName = writerName;
        this.writerProfileImage = writerProfileImage;
        this.peopleNumber = peopleNumber;
        this.descriptionImage = descriptionImage;
    }

    // @SerializedName은 파싱할때 사용될 key 이름이다.


    public String getIdx() {
        return chatRoomIdx;
    }

    public void setIdx(String idx) {
        this.chatRoomIdx = idx;
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
