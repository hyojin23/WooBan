package com.example.wooban;

import com.google.gson.annotations.SerializedName;

public class WatchLaterVideoModel {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("video_url")
    private String videoUrl;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("views")
    private String views;

    @SerializedName("like_count")
    private int likeCount;

    @SerializedName("dislike_count")
    private int dislikeCount;

    @SerializedName("profile_image_url")
    private String profileImageUrl;

    @SerializedName("post_time_millis")
    private long postTimeMillis;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("video_duration")
    private String videoDuration;

    @SerializedName("tag")
    private String tag;

    @SerializedName("idx")
    private int idx;

    @SerializedName("video_idx")
    private int videoIdx;

    public WatchLaterVideoModel(String title, String description, String videoUrl, String id, String name,
                                String views, int likeCount, int dislikeCount, String profileImageUrl, long postTimeMillis, String thumbnailUrl,
                                String videoDuration, String tag, int idx, int videoIdx) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.id = id;
        this.name = name;
        this.views = views;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.profileImageUrl = profileImageUrl;
        this.postTimeMillis = postTimeMillis;
        this.thumbnailUrl = thumbnailUrl;
        this.videoDuration = videoDuration;
        this.tag = tag;
        this.idx = idx;
        this.videoIdx = videoIdx;
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

    public int getVideoIdx() {
        return videoIdx;
    }

    public void setVideoIdx(int videoIdx) {
        this.videoIdx = videoIdx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
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

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getPostTimeMillis() {
        return postTimeMillis;
    }

    public void setPostTimeMillis(long postTimeMillis) {
        this.postTimeMillis = postTimeMillis;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(String videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}
