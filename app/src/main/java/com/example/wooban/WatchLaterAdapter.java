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

import java.util.ArrayList;

public class WatchLaterAdapter extends RecyclerView.Adapter<WatchLaterAdapter.ViewHolder> {
    private final static String TAG = "WatchLaterAdapter";
    private final Bundle bundle;
    private ArrayList<WatchLaterVideoModel> videoInfoList = null;
    private Context context;
    private Activity activity;

    // 생성자
    WatchLaterAdapter(ArrayList<WatchLaterVideoModel> videoInfoList, Bundle bundle, Context context, Activity activity) {
        this.videoInfoList = videoInfoList;
        this.bundle = bundle;
        this.context = context;
        this.activity = activity;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemLayout, videoLayout;
        ImageView videoImage;
        TextView videoDuration, videoTitle, writerName;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            itemLayout = itemView.findViewById(R.id.watch_later_recycler_item_layout);
            videoLayout = itemView.findViewById(R.id.watch_later_recycler_thumbnail_layout);
            videoImage = itemView.findViewById(R.id.watch_later_recycler_thumbnail);
            videoDuration = itemView.findViewById(R.id.watch_later_duration);
            videoTitle = itemView.findViewById(R.id.watch_later_recycler_title);
            writerName = itemView.findViewById(R.id.watch_later_recycler_name);

        }
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.watch_later_item, parent, false) ;
        return new ViewHolder(view);
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // 영상 썸네일
        Glide.with(holder.videoImage).load(videoInfoList.get(position).getThumbnailUrl()).into(holder.videoImage);
        // 영상 제목
        holder.videoTitle.setText(videoInfoList.get(position).getTitle());
        // 영상 시간
        holder.videoDuration.setText(videoInfoList.get(position).getVideoDuration());
        // 영상 작성자
        holder.writerName.setText(videoInfoList.get(position).getName());
        // 아이템을 클릭할 경우
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "영상을 클릭" );
                Intent intent = new Intent(context, VideoWatchActivity.class);
                // 영상 url
                intent.putExtra("video_url", videoInfoList.get(position).getVideoUrl());
                Log.d(TAG, "onClick: 영상 url: " + videoInfoList.get(position).getVideoUrl());
                // 영상 제목
                intent.putExtra("title", videoInfoList.get(position).getTitle());
                // 조회수
                intent.putExtra("views", videoInfoList.get(position).getViews());
                // 작성자 프로필 이미지
                intent.putExtra("profile_image_url", videoInfoList.get(position).getProfileImageUrl());
                // 작성자 이름
                intent.putExtra("name", videoInfoList.get(position).getName());
                // 영상 설명
                intent.putExtra("description", videoInfoList.get(position).getVideoDuration());
                // 글 작성 시간
                intent.putExtra("post_time_millis", videoInfoList.get(position).getPostTimeMillis());
                Log.d(TAG, "onClick: 글 작성 시간: "+videoInfoList.get(position).getPostTimeMillis());
                // 좋아요 수
                intent.putExtra("like_count", videoInfoList.get(position).getLikeCount());
                Log.d(TAG, "onClick: like_count: " + videoInfoList.get(position).getLikeCount());
                // 싫어요 수
                intent.putExtra("dislike_count", videoInfoList.get(position).getDislikeCount());
                // 영상 태그
                intent.putExtra("tag", videoInfoList.get(position).getTag());
                // 내 이름과 내 프로필 이미지 번들 객체 전송
                intent.putExtra("bundle", bundle);
                Log.d(TAG, "onClick: 어댑터에서 보내는 번들 객체: " + bundle);
                // 인덱스 번호
                intent.putExtra("video_index", videoInfoList.get(position).getVideoIdx());
                Log.d(TAG, "onClick: 인덱스 번호: " + videoInfoList.get(position).getVideoIdx());

                activity.startActivity(intent);
            }
        });


    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return videoInfoList.size();
    }


}
