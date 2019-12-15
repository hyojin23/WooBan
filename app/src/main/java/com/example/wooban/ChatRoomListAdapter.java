package com.example.wooban;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.shapes.RoundRectShape;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.example.wooban.ChattingActivity.idx;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {
    private ArrayList<ChatRoomListModel> chatRoomDataList;
    private static final String TAG = "ChatRoomListAdapter";
    String room_idx, room_title, room_tag, writer_id, writer_name, writer_profile_image_url, people_number, room_image;
    private Context context;
    private Bundle bundle;
    private Activity activity;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView, tagTextView, peopleNumberTextView;
        private ImageView hostProfileImageView, descriptionImageView;


        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            titleTextView = itemView.findViewById(R.id.chatting_room_list_item_title);
            tagTextView = itemView.findViewById(R.id.chatting_room_list_item_tag);
            peopleNumberTextView = itemView.findViewById(R.id.chatting_room_list_item_people_number);
            hostProfileImageView = itemView.findViewById(R.id.chatting_room_list_item_host_profile_image);
            descriptionImageView = itemView.findViewById(R.id.chatting_room_list_image_view);

            // 아이템 클릭시 이벤트
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 해당 방의 포지션
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // 데이터 리스트로부터 아이템 데이터 참조.
                        Log.d(TAG, "onClick: 채팅방 목록 아이템 포지션: "+position);
                        // 해당 채팅방의 db에 저장된 인덱스 번호
                        room_idx = chatRoomDataList.get(position).getIdx();
                        Log.d(TAG, "onClick: room_idx: "+room_idx);
                        // 채팅방에 접속한 횟수를 가져옴
                        getChatRoomAccessCount(room_idx, position);
                    }

                    // 다이얼로그 생성
//                    ChatRoomEnterDialog(getAdapterPosition());

//                    int position = getAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION) {
//                        // 데이터 리스트로부터 아이템 데이터 참조.
//                        Log.d(TAG, "onClick: 채팅방 목록 아이템 포지션: "+position);
//                        // 해당 아이템의 db에 저장된 인덱스 번호
//                        room_idx = chatRoomDataList.get(position).getIdx();
//                        Log.d(TAG, "onClick: room_idx: "+room_idx);
//                        // 서버에 해당 채팅방 인덱스를 전송
//                        sendChatRoomIdx(room_idx);
//                        // 채팅방에 입장
//                        Intent chatIntent = new Intent(context, ChattingActivity.class);
//                        // 채팅방 인덱스 번호
//                        chatIntent.putExtra("idx", room_idx);
//                        // 채팅방에 내 아이디, 이름, 프로필 이미지가 담긴 번들을 넘김
//                        chatIntent.putExtra("bundle", bundle);
//                        context.startActivity(chatIntent);
//                    }

                }
            });

        }
    }

    // 생성자에서 객체들을 전달받음.
    ChatRoomListAdapter(Activity activity, Context context, Bundle bundle, ArrayList<ChatRoomListModel> list) {
        this.activity = activity;
        this.context = context;
        this.bundle = bundle;
        this.chatRoomDataList = list;
    }


    @NonNull
    @Override
    public ChatRoomListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.chatting_room_list_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomListAdapter.ViewHolder holder, int position) {
        // 제목
        holder.titleTextView.setText(chatRoomDataList.get(position).getTitle());
        // 태그
        holder.tagTextView.setText(chatRoomDataList.get(position).getTag());
        // 작성자 프로필 이미지
        Glide.with(holder.hostProfileImageView).load(chatRoomDataList.get(position).getWriterProfileImage())
                .apply(RequestOptions.circleCropTransform()).into(holder.hostProfileImageView);
        // 참여인원수
//        holder.peopleNumberTextView.setText(chatRoomDataList.get(position).getPeopleNumber());
        holder.peopleNumberTextView.setText("");
        // 방 소개 이미지
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(16));
        Glide.with(holder.descriptionImageView).load(chatRoomDataList.get(position).getDescriptionImage())
                .apply(requestOptions).into(holder.descriptionImageView);


    }

    @Override
    public int getItemCount() {
        return (chatRoomDataList != null ? chatRoomDataList.size() : 0);
    }


    // 채팅방 클릭시 나타나는 다이얼로그
    private void ChatRoomEnterDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("채팅방 입장");
        builder.setMessage("<" + chatRoomDataList.get(position).getTitle() + ">" + " 채팅방에 들어갈까요?");
        // 예를 눌렀을 때
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 채팅방 입장 예를 선택");
                        if (position != RecyclerView.NO_POSITION) {
                            // 데이터 리스트로부터 아이템 데이터 참조.
                            Log.d(TAG, "onClick: 채팅방 목록 아이템 포지션: "+position);
                            // 해당 아이템의 db에 저장된 인덱스 번호
                            room_idx = chatRoomDataList.get(position).getIdx();
                            Log.d(TAG, "onClick: room_idx: "+room_idx);
                            // 채팅방에 입장
                            Intent chatIntent = new Intent(context, ChattingActivity.class);
                            // 채팅방 인덱스 번호
                            chatIntent.putExtra("idx", room_idx);
                            // 채팅방 접속 횟수(채팅방에 0을 넘겨주면 채팅방 액티비티가 실행될 때 1을 더함)
                            chatIntent.putExtra("access_count", 0);
                            // 채팅방에 내 아이디, 이름, 프로필 이미지가 담긴 번들을 넘김
                            chatIntent.putExtra("bundle", bundle);
                            context.startActivity(chatIntent);
                        }

                    }
                });
        // 아니오를 눌렀을 때
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 채팅방 입장 아니오를 선택");
                    }
                });
        builder.show();
    }
//
//    private void sendChatRoomIdx(String room_idx) {
//
//        Call<JsonObject> call = RetrofitClient
//                .getInstance()
//                .getApi()
//                // 토큰과 채팅방 인덱스 번호 전송
//                .addChatRoomIdxToUser(getSharedToken(), room_idx);
//        call.enqueue(new Callback<JsonObject>() {
//
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.d(TAG, "onResponse: 채팅방 인덱스 번호 전송 응답 성공");
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d(TAG, "onResponse: 채팅방 인덱스 번호 전송 응답 실패");
//
//            }
//        });
//    }


    // 채팅방 접속 횟수를 가져오는 메소드
    private void getChatRoomAccessCount(final String room_idx, final int position) {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .getChatRoomAccessCount(getSharedToken(), room_idx);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 채팅방 접속 횟수 가져오기 응답 성공");
                // 받아온 데이터
                JsonObject data = response.body();
                if (data != null) {
                    // JsonObject 안에 들어있는 JsonArray
                    JsonArray countArray = data.getAsJsonArray("chat_room_access_count");
                    Log.d(TAG, "onResponse: countArray: "+countArray);
                    // 채팅방 접속이 처음일 때
                    if (countArray.size() == 0) {
                        Log.d(TAG, "onResponse: 채팅방에 처음 접속");
                        // 채팅방에 입장할 것인지 확인하는 다이얼로그
                        ChatRoomEnterDialog(position);
                    // 처음이 아닐 때
                    } else {
                        Log.d(TAG, "onResponse: 채팅방에 처음 접속 아님");
                        // 채팅방 접속 횟수
                        int access_count = countArray.get(0).getAsJsonObject().get("access_count").getAsInt();
                        Log.d(TAG, "onResponse: 채팅방 접속 횟수: "+access_count);
                        // 채팅방에 입장
                        Intent chatIntent = new Intent(context, ChattingActivity.class);
                        // 채팅방 인덱스 번호
                        chatIntent.putExtra("idx", room_idx);
                        // 채팅방 접속 횟수
                        chatIntent.putExtra("access_count", access_count);
                        // 채팅방에 내 아이디, 이름, 프로필 이미지가 담긴 번들을 넘김
                        chatIntent.putExtra("bundle", bundle);
                        context.startActivity(chatIntent);

                    }

                }


                Log.d(TAG, "onResponse: data: "+data);

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 채팅방 접속 횟수 가져오기 응답 실패");

            }
        });
    }


    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }

}


