package com.example.wooban;

import com.google.gson.annotations.SerializedName;

public class ReplyInfoModel {

    @SerializedName("idx")
    private int idx;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("content")
    private String content;

    @SerializedName("profile_image_url")
    private String profileImage;

    @SerializedName("post_time_millis")
    private long postTimeMillis;

    @SerializedName("like_count")
    private int likeCount;

    @SerializedName("dislike_count")
    private int dislikeCount;

    @SerializedName("is_paging")
    private int isPaging;

    public ReplyInfoModel(int idx, String id, String name, String content, String profileImage, long postTimeMillis, int likeCount, int dislikeCount) {
        this.idx = idx;
        this.id = id;
        this.name = name;
        this.content = content;
        this.profileImage = profileImage;
        this.postTimeMillis = postTimeMillis;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
    }

    public ReplyInfoModel(String id, String name, String content, String profileImage, long postTimeMillis) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.profileImage = profileImage;
        this.postTimeMillis = postTimeMillis;
    }

    public int getIsPaging() {
        return isPaging;
    }

    public void setIsPaging(int isPaging) {
        this.isPaging = isPaging;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getPostTimeMillis() {
        return postTimeMillis;
    }

    public void setPostTimeMillis(long postTimeMillis) {
        this.postTimeMillis = postTimeMillis;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }
}
