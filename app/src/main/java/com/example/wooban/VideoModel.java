package com.example.wooban;

public class VideoModel {

    private String str_path, str_thumb, duration, name, profile_image_url;

    private boolean boolean_selected;

    public String getStr_path() {
        return str_path;
    }

    public void setStr_path(String str_path) {
        this.str_path = str_path;
    }

    public String getStr_thumb() {
        return str_thumb;
    }

    public void setStr_thumb(String str_thumb) {
        this.str_thumb = str_thumb;
    }

    public boolean isBoolean_selected() {
        return boolean_selected;
    }

    public void setBoolean_selected(boolean boolean_selected) {
        this.boolean_selected = boolean_selected;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
}
