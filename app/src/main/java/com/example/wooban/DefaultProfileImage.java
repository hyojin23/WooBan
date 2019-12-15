package com.example.wooban;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class DefaultProfileImage {

    private static final String TAG = "DefaultProfileImage";


   public void changeToAnimal(Activity activity, String select_animal, ImageView imageView) {

        switch (select_animal) {
            case "default_dog":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 개");
                Glide.with(activity).load(R.drawable.dog_face_icon).into(imageView);
                break;
            case "default_cat":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 고양이");
                Glide.with(activity).load(R.drawable.cat_face_icon).into(imageView);
                break;
            case "default_fox":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 여우");
                Glide.with(activity).load(R.drawable.fox_face_icon).into(imageView);
                break;
            case "default_rabbit":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 토끼");
                Glide.with(activity).load(R.drawable.rabbit_face_icon).into(imageView);
                break;
            case "default_monkey":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 원숭이");
                Glide.with(activity).load(R.drawable.monkey_face_icon).into(imageView);
                break;
            case "default_lion":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 사자");
                Glide.with(activity).load(R.drawable.lion_face_icon).into(imageView);
                break;
            case "default_panda":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 판다");
                Glide.with(activity).load(R.drawable.panda_face_icon).into(imageView);
                break;
            case "default_bear":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 곰");
                Glide.with(activity).load(R.drawable.bear_face_icon).into(imageView);
                break;
            case "default_hedgehog":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 고슴도치");
                Glide.with(activity).load(R.drawable.hedgehog_face_icon).into(imageView);
                break;
            case "default_wolf":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 늑대");
                Glide.with(activity).load(R.drawable.wolf_face_icon).into(imageView);
                break;

        }
    }

    public void changeToAnimal(Context context, String select_animal, ImageView imageView) {
        switch (select_animal) {
            case "default_dog":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 개");
                Glide.with(context).load(R.drawable.dog_face_icon).into(imageView);
                break;
            case "default_cat":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 고양이");
                Glide.with(context).load(R.drawable.cat_face_icon).into(imageView);
                break;
            case "default_fox":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 여우");
                Glide.with(context).load(R.drawable.fox_face_icon).into(imageView);
                break;
            case "default_rabbit":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 토끼");
                Glide.with(context).load(R.drawable.rabbit_face_icon).into(imageView);
                break;
            case "default_monkey":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 원숭이");
                Glide.with(context).load(R.drawable.monkey_face_icon).into(imageView);
                break;
            case "default_lion":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 사자");
                Glide.with(context).load(R.drawable.lion_face_icon).into(imageView);
                break;
            case "default_panda":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 판다");
                Glide.with(context).load(R.drawable.panda_face_icon).into(imageView);
                break;
            case "default_bear":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 곰");
                Glide.with(context).load(R.drawable.bear_face_icon).into(imageView);
                break;
            case "default_hedgehog":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 고슴도치");
                Glide.with(context).load(R.drawable.hedgehog_face_icon).into(imageView);
                break;
            case "default_wolf":
                Log.d(TAG, "changeToAnimal: 기본 프로필 이미지: 늑대");
                Glide.with(context).load(R.drawable.wolf_face_icon).into(imageView);
                break;
        }
    }
}