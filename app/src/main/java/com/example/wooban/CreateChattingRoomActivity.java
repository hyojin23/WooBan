package com.example.wooban;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateChattingRoomActivity extends AppCompatActivity {
    private static final String TAG = "CreateChatRoomActivity";
    EditText titleEditText, tagEditText;
    ImageView chatRoomImageView;
    Button imageSelectButton, completeButton;
    String room_title, room_tag, writer_profile_image_url, room_image_url;
    String my_id, my_name, my_profile_image_url, room_number;
    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chatting_room);

        // 툴바
        Toolbar mToolbar = (Toolbar) findViewById(R.id.create_chatting_room_toolbar);
        setSupportActionBar(mToolbar);

        // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 툴바 제목이 안 나타나게 함
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.activity_create_chatting_room);

        // 채팅방 목록 액티비티에서 받은 번들 객체(내 아이디, 이름, 프로필 이미지)
        final Intent intent = getIntent();
        final Bundle bundle = intent.getBundleExtra("bundle");

        // 내 아이디
        my_id = bundle.getString("my_id");
        Log.d(TAG, "onCreate: my_id: " + my_id);
        // 내 이름
        my_name = bundle.getString("my_name");
        Log.d(TAG, "onCreate:  my_name: " + my_name);
        // 내 프로필 이미지
        my_profile_image_url = bundle.getString("my_profile_image_url");
        Log.d(TAG, "onCreate: my_profile_image_url: " + my_profile_image_url);


        // 제목 입력창
        titleEditText = findViewById(R.id.create_chatting_room_title_edit_text);
        // 태그 입력창
        tagEditText = findViewById(R.id.create_chatting_room_tag_edit_text);
        // 채팅방 이미지
        chatRoomImageView = findViewById(R.id.create_chatting_room_image_view);
        // 이미지뷰의 기본 이미지
        Glide.with(CreateChattingRoomActivity.this).load(R.drawable.default_image_thumbnail).into(chatRoomImageView);
        // 이미지 선택 버튼
        imageSelectButton = findViewById(R.id.create_chatting_room_image_select_button);
        // 완료 버튼
        completeButton = findViewById(R.id.create_chatting_room_complete_button);

        // 이미지 선택 버튼 클릭시
        imageSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeImageFromGallery();

            }
        });

        // 완료 버튼 클릭시
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 방 제목
                room_title = titleEditText.getText().toString().trim();
                Log.d(TAG, "onClick: room_title" + room_title);
                // 방 태그
                room_tag = tagEditText.getText().toString().trim();
                Log.d(TAG, "onClick: room_tag: " + room_tag);

                if (room_title.isEmpty()) {
                    titleEditText.setError("방 제목을 입력해주세요.");
                    titleEditText.requestFocus();
                }

                if (room_tag.isEmpty()) {
                    tagEditText.setError("태그를 입력해주세요.");
                    tagEditText.requestFocus();
                }

                // 방 소개 이미지 절대 경로
                if (image_uri != null) {
                    room_image_url = getRealPathFromURIPath(image_uri, CreateChattingRoomActivity.this);
                } else {
                    room_image_url = "default";
                }
                Log.d(TAG, "onClick: room_image_url: " + room_image_url);
                // 이미지 파일 객체 생성
                File file = new File(room_image_url);
                // 이미지 RequestBody 생성
                RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
                // 방 제목 RequestBody
                RequestBody title = createPartFromString(room_title);
                // 방 태그 RequestBody
                RequestBody tag = createPartFromString(room_tag);
                // 방 작성자 아이디 RequestBody
                RequestBody writer_id = createPartFromString(my_id);
                // 방 작성자 이름 RequestBody
                RequestBody writer_name = createPartFromString(my_name);
                // 방 작성자 프로필 이미지 urlv RequestBody
                RequestBody writer_profile_image_url = createPartFromString(my_profile_image_url);


                // 실제로 서버에 보내지는 이미지 파일 객체
                MultipartBody.Part iFile = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
                // 부가정보를 담아 보낼 맵 객체
                HashMap<String, RequestBody> map = new HashMap<>();
                // 방 제목
                map.put("room_title", title);
                // 방 태그
                map.put("room_tag", tag);
                // 방 작성자 아이디
                map.put("room_writer_id", writer_id);
                // 방 작성자 이름
                map.put("room_writer_name", writer_name);
                // 방 작성자 프로필 이미지
                map.put("room_writer_profile_image_url", writer_profile_image_url);

                // 방 소개 이미지 파일과 부가정보를 db에 저장하는 요청
                Call<JsonObject> call = RetrofitClient
                        .getInstance()
                        .getApi()
                        .uploadRoomInfo(getSharedToken(), iFile, map);
                call.enqueue(new Callback<JsonObject>() {


                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        Log.d(TAG, "onResponse: 방 정보 저장요청에 대한 응답 성공");
                        // 채팅방에 입장
                        Intent chatIntent = new Intent(CreateChattingRoomActivity.this, ChattingActivity.class);
                        // 채팅방에 내 아이디, 이름, 프로필 이미지가 담긴 번들을 넘김
                        chatIntent.putExtra("bundle", bundle);
                        // 채팅방 접속 횟수를 넘김
//                        chatIntent.putExtra("access_count", 1);
                        // CreateChattingRoomActivity에서 채팅방으로 입장한 것인지 판별하기 위해 보냄
                        int from_create_activity = 100;
                        chatIntent.putExtra("from", from_create_activity);
                        startActivity(chatIntent);
                        // 액티비티 종료
                        finish();
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.d(TAG, "onResponse: 방 정보 저장요청에 대한 응답 실패");

                    }

                });


            }
        });
    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 실행");
    }

    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: 실행");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "갤러리 앱을 실행 후 종료한 상황");
            // uri를 얻어옴
            image_uri = data.getData();
            Log.d(TAG, "갤러리에서 얻은 uri :" + image_uri);
            // 이미지뷰에 보이게 함
            Glide.with(CreateChattingRoomActivity.this).load(image_uri).into(chatRoomImageView);


        }
    }

    // 갤러리를 실행해 사진을 불러오는 메소드
    private void takeImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 100);
    }

    // 파일의 절대경로를 구하는 메소드
    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Log.d(TAG, "getRealPathFromURIPath() 실행 1");
        Log.d(TAG, "getRealPathFromURIPath: contentURI: " + contentURI);
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        Log.d(TAG, "getRealPathFromURIPath() 실행 2");

        if (cursor == null) {
            Log.d(TAG, "getRealPathFromURIPath() 실행 3");
            return contentURI.getPath();

        } else {
            Log.d(TAG, "getRealPathFromURIPath() 실행 4");
            // 커서를 제일 첫번째 행으로 이동
            cursor.moveToFirst();
            // 해당 컬럼의 인덱스를 얻어옴
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            // 인덱스 번호를 string으로 바꿔서 반환
            return cursor.getString(idx);
        }
    }

    // String을 RequestBody로 변환
    private RequestBody createPartFromString(String param) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), param);
    }

    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }

}
