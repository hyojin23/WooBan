package com.example.wooban;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoInfoTagAdapter extends RecyclerView.Adapter<VideoInfoTagAdapter.ViewHolder> {

    private static final String TAG = "VideoInfoTagAdapter";
    // 데이터 리스트
    private ArrayList<String> mData = null;
    private ClickCallbackListener callbackListener;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        Button tagButton;

        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            tagButton = itemView.findViewById(R.id.video_info_recycle_button);
            // 아이템뷰 클릭 리스너
            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition() ;
                    Log.d(TAG, "onClick: pos: "+pos);
                    // 뷰홀더가 참조하는 아이템이 어댑터에서 삭제되면 getAdapterPosition() 메서드는 NO_POSITION을 리턴
                    if (pos != RecyclerView.NO_POSITION) {
                        // 데이터 리스트로부터 아이템 데이터 참조.
                        String clickedData = mData.get(pos) ;
                        Log.d(TAG, "onClick: clickedData: "+clickedData);
                        String hashTagText = "#"+clickedData;
                        Log.d(TAG, "onClick: hashTagText: "+hashTagText);
                        callbackListener.callBack(hashTagText);
                    }
                }
            });

        }
    }

    // 생성자
    public VideoInfoTagAdapter(ArrayList<String> list, ClickCallbackListener callbackListener) {
        mData = list;
        this.callbackListener = callbackListener;
    }

    @NonNull
    @Override
    public VideoInfoTagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 커스텀한 뷰 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_info_recycler_item, parent, false);
        return new VideoInfoTagAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoInfoTagAdapter.ViewHolder holder, int position) {
        // 리스트에 있는 텍스트
        String tag = mData.get(position);
        Log.d(TAG, "onBindViewHolder: tag: "+tag);
        // 버튼에 넣음
        holder.tagButton.setText(tag);

    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }
}


