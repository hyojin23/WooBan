package com.example.wooban;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.wooban.ChattingActivity.idx;

public class ChattingAdapter extends RecyclerView.Adapter {
    private final static String TAG = "ChattingAdapter";
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_INFO = 3;
    private final Bundle bundle;
    private ArrayList<ChattingModel> chatArrayList;
    private Activity activity;
    private Context context;
    String my_id;

    // 채팅 보내는 사람 뷰홀더
    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "SentMessageHolder: 실행");
            messageText = (TextView) itemView.findViewById(R.id.sent_text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.sent_text_message_time);
        }

    }

    // 생성자
    ChattingAdapter(Activity activity, Context context, ArrayList<ChattingModel> chatArrayList, Bundle bundle) {
        this.chatArrayList = chatArrayList;
        this.activity = activity;
        this.context = context;
        this.bundle = bundle;
    }

    // 채팅 받는 사람 뷰홀더
    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;
        ImageView profileImage;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "ReceivedMessageHolder: 실행");
            messageText = (TextView) itemView.findViewById(R.id.received_text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.received_text_message_time);
            nameText = (TextView) itemView.findViewById(R.id.received_text_message_name);
            profileImage = (ImageView) itemView.findViewById(R.id.received_message_profile_image);
        }

    }

    // 입장, 퇴장 메세지 뷰홀더
    private class InfoMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        InfoMessageHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "InfoMessageHolder: 실행");
            messageText = itemView.findViewById(R.id.chatting_info_text_view);
        }
    }

    // 뷰타입 지정
    @Override
    public int getItemViewType(int position) {
        // 채팅을 쓴 아이디
        String chat_id = chatArrayList.get(position).getId();
        Log.d(TAG, "getItemViewType: chat_id: " + chat_id);
        // 내 아이디
        my_id = bundle.getString("my_id");
        // 채팅 유형
        String chat_type = chatArrayList.get(position).getType();
        Log.d(TAG, "getItemViewType: my_id: " + my_id);

        // 입장, 퇴장 메세지일때
        if (chat_type.equals("join") || chat_type.equals("quit")) {
            Log.d(TAG, "getItemViewType: 입장, 퇴장 뷰타입, 뷰타입: " + VIEW_TYPE_MESSAGE_INFO);
            return VIEW_TYPE_MESSAGE_INFO;
        // 내가 메세지를 보낼 때
        } else if (chat_id.equals(my_id)) {
            Log.d(TAG, "getItemViewType: 채팅 보내는 뷰타입, 뷰타입: " + VIEW_TYPE_MESSAGE_SENT);
            return VIEW_TYPE_MESSAGE_SENT;
        // 내가 메세지를 받을 때
        } else {
            Log.d(TAG, "getItemViewType: 채팅 받는 뷰타입, 뷰타입: " + VIEW_TYPE_MESSAGE_RECEIVED);
            return VIEW_TYPE_MESSAGE_RECEIVED;

        }

    }

    // 뷰타입에 맞는 뷰홀더 생성
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;
        // 메세지 보내는 사람일 경우
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            Log.d(TAG, "onCreateViewHolder: 메세지 보내는 뷰홀더 생성");
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chatting_message_sent_item, parent, false);
            holder = new SentMessageHolder(view);
            return holder;
            // 메세지 받는 사람일 경우
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            Log.d(TAG, "onCreateViewHolder: 메세지 받는 뷰홀더 생성");
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chatting_message_received_item, parent, false);
            holder = new ReceivedMessageHolder(view);
            return holder;
        } else if (viewType == VIEW_TYPE_MESSAGE_INFO) {
            Log.d(TAG, "onCreateViewHolder: 입장, 퇴장 뷰홀더 생성");
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chatting_message_info_item, parent, false);
            holder = new InfoMessageHolder(view);
            return holder;
        }
            return  null;
    }

    // 뷰홀더 바인딩
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 채팅 내용
        String message = chatArrayList.get(position).getMessage();
        // 채팅 작성자 이름
        String chat_name = chatArrayList.get(position).getName();
        // 채팅 작성 시간
        String chat_time = chatArrayList.get(position).getTime();
        // 채팅 작성자 프로필 이미지 url
        String chat_profile_image_url = chatArrayList.get(position).getProfile_image_url();

        switch (holder.getItemViewType()) {
            // 메세지를 보내는 뷰타입일때
            case VIEW_TYPE_MESSAGE_SENT:
                Log.d(TAG, "onBindViewHolder: 메세지를 보내는 뷰타입일때 onBindViewHolder");
                SentMessageHolder sentMessageHolder = (SentMessageHolder) holder;
                // 채팅 내용 등록
                sentMessageHolder.messageText.setText(message);
                // 채팅 작성 시간 등록
                sentMessageHolder.timeText.setText(chat_time);
                break;
            // 메세지를 받는 뷰타입일때
            case VIEW_TYPE_MESSAGE_RECEIVED:
                Log.d(TAG, "onBindViewHolder: 메세지를 받는 뷰타입일때 onBindViewHolder");
                ReceivedMessageHolder receivedMessageHolder = (ReceivedMessageHolder) holder;
                // 채팅 내용 등록
                receivedMessageHolder.messageText.setText(message);
                // 채팅 작성자 등록
                receivedMessageHolder.nameText.setText(chat_name);
                // 채팅 작성 시간 등록
                receivedMessageHolder.timeText.setText(chat_time);
                // 채팅 작성자 프로필 이미지 등록
                Glide.with(receivedMessageHolder.profileImage).load(chat_profile_image_url)
                        .apply(RequestOptions.circleCropTransform()).into(receivedMessageHolder.profileImage);
                break;
            case VIEW_TYPE_MESSAGE_INFO:
                Log.d(TAG, "onBindViewHolder: 입장, 퇴장 메세지 뷰타입일때 onBindViewHolder");
                InfoMessageHolder infoMessageHolder =  (InfoMessageHolder) holder;
                // 채팅 메세지 등록
                infoMessageHolder.messageText.setText(message);

                break;
        }
    }

    // 아이템 갯수 리턴
    @Override
    public int getItemCount() {
//        Log.d(TAG, "getItemCount: chatArrayList: " + chatArrayList);
        return (chatArrayList != null ? chatArrayList.size() : 0);

    }

    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        Log.d(TAG, "getSharedToken: context: " + context);
        Log.d(TAG, "getSharedToken: context.getApplicationContext(): " + context.getApplicationContext());
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }

    // shared에 저장된 소켓 연결 횟수를 가져오는 메소드
    private int getSharedCount() {
        // 쉐어드 객체
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("shared", MODE_PRIVATE);
        // shared에 저장되어 있는 유저가 connection한 횟수
        int count = sharedPreferences.getInt(my_id + idx, 0);
        Log.d(TAG, "getSharedCount: my_id + idx: "+my_id + idx);
        Log.d(TAG, "getSharedCount: 쉐어드에 저장된 connection 횟수: "+count);
        return count;
    }


}