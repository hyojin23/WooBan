package com.example.wooban;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private final static String TAG = "MainAdapter";
    private final Bundle bundle;
    private Context context;
    private JsonArray jsonArray;
    private JsonObject jsonObject;
    private Activity activity;


    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thumImageView;
        ImageView profileImageView;
        TextView titleTextView, videoInfoTextView, videoDurationTextView;
        ConstraintLayout selectorLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder: 실행");
            // 뷰 객체 참조
            thumImageView = itemView.findViewById(R.id.main_image_view);
            profileImageView = itemView.findViewById(R.id.main_profile_image_view);
            titleTextView = itemView.findViewById(R.id.main_video_title_text_view);
            videoInfoTextView = itemView.findViewById(R.id.main_video_infomation);
            selectorLayout = itemView.findViewById(R.id.main_recycler_item_layout);
            videoDurationTextView = itemView.findViewById(R.id.main_video_duration_text_view);

        }
    }

    // 메인 액티비티에서 인자로 받은 값을 사용할 수 있게 하는 생성자
    public MainAdapter(Context context, JsonObject jsonObject, Bundle bundle, Activity activity) {
        this.context = context;
        this.jsonObject = jsonObject;
        this.activity = activity;
        this.bundle = bundle;
    }


    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: 실행");
        // 커스텀한 뷰 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_recycler_item, parent, false);

        return new MainAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MainAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: 실행");
        // JsonArray를 가져옴
        jsonArray = jsonObject.getAsJsonArray("video_info");
        /*각 인덱스에 맞는 값을 가져옴*/
        // 제목
        final String title = jsonArray.get(position).getAsJsonObject().get("title").getAsString();
        Log.d(TAG, "onBindViewHolder: title: " + title);
        // 설명
        final String description = jsonArray.get(position).getAsJsonObject().get("description").getAsString();
        Log.d(TAG, "onBindViewHolder: description: " + description);
        // 영상 url
        final String video_url = jsonArray.get(position).getAsJsonObject().get("video_url").getAsString();
        Log.d(TAG, "onBindViewHolder: video_url: " + video_url);
        // 작성자 이름
        final String name = jsonArray.get(position).getAsJsonObject().get("name").getAsString();
        Log.d(TAG, "onBindViewHolder: name: " + name);
        // 조회수
        final int views = jsonArray.get(position).getAsJsonObject().get("views").getAsInt();
        Log.d(TAG, "onBindViewHolder: views: " + views);
        // 좋아요 수
        final int like_count = jsonArray.get(position).getAsJsonObject().get("like_count").getAsInt();
        Log.d(TAG, "onBindViewHolder: like_count: " + like_count);
        // 싫어요 수
        final int dislike_count = jsonArray.get(position).getAsJsonObject().get("dislike_count").getAsInt();
        Log.d(TAG, "onBindViewHolder: dislike_count: " + dislike_count);
        // 프로필 이미지 url
        final String profile_image_url = jsonArray.get(position).getAsJsonObject().get("profile_image_url").getAsString();
        Log.d(TAG, "onBindViewHolder: profile_image_url: " + profile_image_url);
        // 업로드한 시간
        final long post_time_millis = jsonArray.get(position).getAsJsonObject().get("post_time_millis").getAsLong();
        Log.d(TAG, "onBindViewHolder: post_time_millis: " + post_time_millis);
        // 썸네일 url
        String thumbnail_url = jsonArray.get(position).getAsJsonObject().get("thumbnail_url").getAsString();
        // 영상 재생시간
        String video_duration = jsonArray.get(position).getAsJsonObject().get("video_duration").getAsString();
        Log.d(TAG, "onBindViewHolder: video_duration: " + video_duration);
        // 태그
        final String tag = jsonArray.get(position).getAsJsonObject().get("tag").getAsString();
        Log.d(TAG, "onBindViewHolder: tag: " + tag);
        // 인덱스 번호
        final int video_index = jsonArray.get(position).getAsJsonObject().get("idx").getAsInt();
        Log.d(TAG, "onBindViewHolder: video_index: "+video_index);


        // 제목 세팅
        holder.titleTextView.setText(title);
        // 작성자, 조회수, 업로드 시간 세팅 ex) 말왕 · 678회 · 2주전
        holder.videoInfoTextView.setText(videoInfoText(name, views, formatTimeString(post_time_millis)));
        // 영상 세팅
//        holder.videoView.setVideoPath(video_url);
        // 이미지 세팅
        Glide.with(context).load(thumbnail_url).skipMemoryCache(false).into(holder.thumImageView);
        // 영상 재생시간 세팅
        holder.videoDurationTextView.setText(video_duration);

//        // 재생 버튼, 일시정지 버튼 등을 만들기 위해 미디어 컨트롤러에 비디오뷰를 넣음
//        MediaController mediaController = new MediaController(context);
//        holder.videoView.setMediaController(mediaController);
//        mediaController.setAnchorView(holder.videoView);
        // 프레임을 0.001초 실행시켜 검은 화면이 나오지 않게 함
//        holder.videoView.seekTo(1);

        if (profile_image_url.contains("default")) {
            // 기본 이미지 중 선택한 이미지를 이미지뷰에 넣어 보여준다.
            DefaultProfileImage defaultImage = new DefaultProfileImage();
            defaultImage.changeToAnimal(context, profile_image_url, holder.profileImageView);
        } else {
            // 작성자 프로필 이미지 세팅
            Glide.with(context).load(profile_image_url).apply(RequestOptions.circleCropTransform()).into(holder.profileImageView);
        }
        // 영상 클릭시 로그인이 되어 있으면 영상을 볼 수 있는 페이지로 이동하고 로그인이 안 되어 있으면 로그인 화면으로 이동한다.
        holder.selectorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bundle.getString("my_profile_image_url") == null) {
                    Log.d(TAG, "onClick: 영상 클릭시 로그인되어 있지 않아 로그인 화면으로 이동");
                    Intent intent = new Intent(context, LoginActivity.class);
                    activity.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, VideoWatchActivity.class);
                    // 영상 url
                    intent.putExtra("video_url", video_url);
                    // 영상 제목
                    intent.putExtra("title", title);
                    // 조회수
                    intent.putExtra("views", views);
                    // 작성자 프로필 이미지
                    intent.putExtra("profile_image_url", profile_image_url);
                    // 작성자 이름
                    intent.putExtra("name", name);
                    // 영상 설명
                    intent.putExtra("description", description);
                    // 글 작성 시간
                    intent.putExtra("post_time_millis", post_time_millis);
                    // 영상 태그
                    intent.putExtra("tag", tag);
                    // 좋아요 수
                    intent.putExtra("like_count", like_count);
                    // 싫어요 수
                    intent.putExtra("dislike_count", dislike_count);
                    // 내 이름과 내 프로필 이미지 번들 객체 전송
                    intent.putExtra("bundle", bundle);
                    Log.d(TAG, "onClick: 메인 어댑터에서 보내는 번들 객체: " + bundle);
                    // 인덱스 번호
                    intent.putExtra("video_index", video_index);

                    activity.startActivity(intent);
                }

            }
        });

    }

    // 전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: 실행");
        jsonArray = jsonObject.getAsJsonArray("video_info");
        Log.d(TAG, "getItemCount: jsonArray.size(): " + jsonArray.size());
        return jsonArray.size();
    }


    /**
     * 몇분전, 방금 전,
     */
    static class TIME_MAXIMUM {
        static final int SEC = 60;
        static final int MIN = 60;
        static final int HOUR = 24;
        static final int DAY = 30;
        static final int MONTH = 12;
    }

    static String formatTimeString(long regTime) {
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;
        String msg = null;
        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        return msg;
    }


    // 작성자, 조회수, 업로드 시간을 표시 ex) 말왕 · 678회 · 2주전
    private String videoInfoText(String name, int views, String time) {
        String info = name + " · 조회수 " + views + "회 · " + time;
        return info;
    }


}


