package com.example.wooban;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private final static String TAG = "VideoAdapter";
    private Context context;
    private ArrayList<VideoModel> arrayListVideos;
    private Activity activity;

    // 아이템 뷰를 저장하는 뷰홀더 클래스
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ConstraintLayout rl_select;
        TextView durationTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder: 실행");
            imageView = (ImageView)itemView.findViewById(R.id.video_image_view);
            rl_select = (ConstraintLayout)itemView.findViewById(R.id.rl_select);
            durationTextView = (TextView)itemView.findViewById(R.id.video_duration_text_view);
        }
    }

    // 생성자
    public VideoAdapter(Context context, ArrayList<VideoModel> arrayListVideos, Activity activity) {
        Log.d(TAG, "VideoAdapter: 생성자");
        this.context = context;
        this.arrayListVideos = arrayListVideos;
        this.activity = activity;

    }

    // 뷰홀더 생성
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: 실행");
        // 커스텀한 뷰 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_video, parent, false);

        return new VideoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: 실행");

        Glide.with(context).load(arrayListVideos.get(position).getStr_thumb()).skipMemoryCache(false).into(holder.imageView);
        Log.d(TAG, "onBindViewHolder: 썸네일: " + arrayListVideos.get(position).getStr_thumb());
        // 비디오 재생시간
        holder.durationTextView.setText(arrayListVideos.get(position).getDuration());

        holder.rl_select.setBackgroundColor(Color.parseColor("#FFFFFF"));
        Log.d(TAG, "onBindViewHolder: holder: "+holder);
//        holder.rl_select.setAlpha(0);

        holder.rl_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 클릭 실행");
                Intent intent = new Intent(context, VideoInfoAdd.class);
                // 비디오 경로
                intent.putExtra("video", arrayListVideos.get(position).getStr_path());
                Log.d(TAG, "onClick: arrayListVideos.get(position).getStr_path(): "+arrayListVideos.get(position).getStr_path());
                // 이름
                intent.putExtra("name",arrayListVideos.get(position).getName());
                Log.d(TAG, "onClick: arrayListVideos.get(position).getName(): "+arrayListVideos.get(position).getName());
                // 프로필 이미지
                intent.putExtra("profile_image_url", arrayListVideos.get(position).getProfile_image_url());
                Log.d(TAG, "onClick: arrayListVideos.get(position).getProfile_image_url(): "+arrayListVideos.get(position).getProfile_image_url());
                // 썸네일
                intent.putExtra("thumbnail", arrayListVideos.get(position).getStr_thumb());
                Log.d(TAG, "onClick: arrayListVideos.get(position).getStr_thumb(): "+arrayListVideos.get(position).getStr_thumb());
                // 비디오 재생시간
                intent.putExtra("video_duration", arrayListVideos.get(position).getDuration());
                // 액티비티 시작
                activity.startActivity(intent);
            }
        });
    }

    // 전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: 실행");
        Log.d(TAG, "getItemCount: arrayListVideos.size(): "+arrayListVideos.size() );
        return arrayListVideos.size();
    }







}
