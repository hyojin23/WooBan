package com.example.wooban;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
    public static final String APP_SHARED_PREFS = "shared";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferenceUtil(Context context) {
        this.sharedPreferences = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void setSharedString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getSharedString(String key) {
        // 키값과 디폴트값. "defValue"는 키에 대한 값이 없을 경우 리턴해줄 값
        return sharedPreferences.getString(key, "defValue");
    }

    public void setSharedBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getSharedBoolean(String key) {
        // 키값과 디폴트값. "defValue"는 키에 대한 값이 없을 경우 리턴해줄 값
        return sharedPreferences.getBoolean(key, false);
    }

}