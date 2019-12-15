package com.example.wooban;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.gson.JsonObject;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoInfoAdd extends AppCompatActivity {

    private final static String TAG = "VideoInfoAdd";
    private VideoView videoView;
    private ImageView profileImage;
    private EditText titleEdit, discriptionEdit, tagEditText;
    Uri videoUri;
    String name_string, profile_image_url, thumbnail, video_duration_string;
    TextView name;
    ProgressDialog progressDialog;
    //애플리케이션 클라이언트 아이디값
    String clientId = "PjLQ7ILrIfzdACwc68Cu";
    //애플리케이션 클라이언트 시크릿값
    String clientSecret = "hooAt8sEPp";
    // 태그 텍스트 리스트
    ArrayList<String> koTextList = new ArrayList<>();
    ArrayList<String> enTextList = new ArrayList<>();
    // 해시태그 헬퍼
    private HashTagHelper mEditTextHashTagHelper;

    // 이미지 분석 후 문자를 이어붙이기 위한 stringBuilder
    StringBuilder stringBuilder = new StringBuilder();
    String totalText;

    // 어댑터에서 추천 태그 클릭시 발생하는 콜백 리스너
    private ClickCallbackListener callbackListener = new ClickCallbackListener() {
        @Override
        public void callBack(String text) {
            Log.d(TAG, "callBack: "+text+" 버튼 클릭됨");
            tagEditText.append(text);

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_info_add);

        // 툴바
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 툴바 제목
        getSupportActionBar().setTitle("세부정보 추가");

        // 비디오뷰
        videoView = findViewById(R.id.video_info_add_videoView);
        // 프로필 이미지
        profileImage = findViewById(R.id.video_info_add_profile_image_view);
        // 이름
        name = findViewById(R.id.video_info_add_name_text_view);
        // 제목 입력란
        titleEdit = findViewById(R.id.video_info_add_title_edit_text);
        // 설명 입력란
        discriptionEdit = findViewById(R.id.video_info_add_discription_edit_text);
        // 태그 입력란
        tagEditText = findViewById(R.id.video_info_add_tag_edit_text);


        Intent intent = getIntent();
        // 영상 uri
        videoUri = Uri.parse(intent.getStringExtra("video"));
        // 작성자 이름
        name_string = intent.getStringExtra("name");
        // 작성자 프로필 이미지
        profile_image_url = intent.getStringExtra("profile_image_url");
        // 썸네일
        thumbnail = intent.getStringExtra("thumbnail");
        // 비디오 재생시간
        video_duration_string = intent.getStringExtra("video_duration");

        Log.d(TAG, "onCreate: name_string: " + name_string);
        Log.d(TAG, "onCreate: profile_image_url: " + profile_image_url);
        Log.d(TAG, "onCreate: videoUri: " + videoUri);
        Log.d(TAG, "onCreate: thumbnail: " + thumbnail);
        // 이미지 태깅
        imageTagging(thumbnail);

        // 선택한 영상의 썸네일을 비트맵 이미지로 만든다.
//        thumbnail = ThumbnailUtils.createVideoThumbnail(intent.getStringExtra("video"), MediaStore.Video.Thumbnails.MICRO_KIND);


        // 영상 세팅
        videoView.setVideoURI(videoUri);
        // 프로필 이미지 세팅

        if (profile_image_url.contains("default")) {
            // 기본 이미지 중 선택한 이미지를 이미지뷰에 넣어 보여준다.
            DefaultProfileImage defaultImage = new DefaultProfileImage();
            defaultImage.changeToAnimal(VideoInfoAdd.this, profile_image_url, profileImage);
        } else {
            Glide.with(VideoInfoAdd.this).load(profile_image_url).apply(RequestOptions.circleCropTransform()).into(profileImage);
        }

        // 이름 세팅
        name.setText(name_string);


        // 재생 버튼, 일시정지 버튼 등을 만들기 위해 미디어 컨트롤러에 비디오뷰를 넣음
        final MediaController mediaController = new MediaController(VideoInfoAdd.this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        // 프레임을 0.001초 실행시켜 검은 화면이 나오지 않게 함
        videoView.seekTo(1);
        // 영상 재생 시작
//        videoView.start();

        // 스크롤 될때 미디어 컨트롤러 버튼을 숨김
        ScrollView scrollView = findViewById(R.id.video_info_add_scroll_view);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {
                mediaController.hide();
            }
        });

        // 해시태그 헬퍼
        mEditTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.tagColor), null);
        mEditTextHashTagHelper.handle(tagEditText);

    }

    // 영상을 서버에 업로드하는 메소드
    private void uploadVideoToServer(String pathToVideoFile) {
        Log.d(TAG, "uploadVideoToServer() 실행");

        // 영상 파일 객체 생성
        File videoFile = new File(pathToVideoFile);
        Log.d(TAG, "videoFile: " + videoFile);

        // 영상 업로드에 필요한 객체
        RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
        MultipartBody.Part vFile = MultipartBody.Part.createFormData("video", videoFile.getName(), videoBody);
        Log.d(TAG, "videoFile.getName(): " + videoFile.getName());
        Log.d(TAG, "videoBody: " + videoBody);

        // 썸네일 이미지 업로드 객체
        File imageFile = new File(thumbnail);
        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        // 실제로 서버에 보내지는 이미지 파일 객체
        MultipartBody.Part iFile = MultipartBody.Part.createFormData("image", imageFile.getName(), imageBody);

        // 영상 제목
        RequestBody title = createPartFromString(titleEdit.getText().toString().trim());
        // 영상 설명
        RequestBody description = createPartFromString(discriptionEdit.getText().toString().trim());
        // 영상 태그
        RequestBody tag = createPartFromString(tagEditText.getText().toString().trim());
        // 작성자
        RequestBody request_name = createPartFromString(name_string);
        // 작성자 프로필 url
        RequestBody profile_url = createPartFromString(profile_image_url);
        RequestBody request_videoUri = createPartFromString(videoUri.toString());
        // millisecond로 나타낸 영상 업로드 시간(String으로 변환하고 보냄)
        RequestBody current_time = createPartFromString(String.valueOf(System.currentTimeMillis()));
        // 영상 재생시간
        RequestBody video_duration = createPartFromString(video_duration_string);

        // 요청에 담아 보낼 데이터들
        HashMap<String, RequestBody> map = new HashMap<>();
        // 제목
        map.put("title", title);
        Log.d(TAG, "uploadVideoToServer: title: " + title);
        // 설명
        map.put("description", description);
        Log.d(TAG, "uploadVideoToServer: description: " + description);
        // 태그
        map.put("tag", tag);
        Log.d(TAG, "uploadVideoToServer: tag: "+tag);
        // 작성자 이름
        map.put("request_name", request_name);
        Log.d(TAG, "uploadVideoToServer: request_name: " + request_name);
        // 작성자 프로필 이미지
        map.put("profile_url", profile_url);
        Log.d(TAG, "uploadVideoToServer:  profile_url: " + profile_url);
        // 업로드 시간
        map.put("current_time", current_time);
        // 영상 재생시간
        map.put("video_duration", video_duration);


        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .videoUpload(getSharedToken(), iFile, vFile, map);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 응답 성공");
                // json 객체
                JsonObject object = response.body();
                Log.d(TAG, "onResponse: object: " + object);
                progressDialog.dismiss();
                // 메인 화면으로 이동
                Intent intent = new Intent(VideoInfoAdd.this, MainActivity.class);
                // 메인화면 위에 쌓여있던 액티비티를 지우고 메인화면이 올라옴
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 응답 실패");
            }
        });


    }


//        serverCom.enqueue(new Callback<ResultObject>() {
//            @Override
//            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
//                Log.d(TAG, "uploadVideoToServer() 응답 성공");
//                ResultObject result = response.body();
//                if (!TextUtils.isEmpty(result.getSuccess())) {
//                    // 영상이 업로드 되었다는 토스트를 띄움
//                    Toast.makeText(VideoUploadActivity.this, result.getSuccess(), Toast.LENGTH_LONG).show();
//                    Log.d(TAG, "결과 Result " + result.getSuccess());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResultObject> call, Throwable t) {
//                Log.d(TAG, "uploadVideoToServer() 응답 실패");
//                Log.d(TAG, "Error message " + t.getMessage());
//            }
//        });
//    }


    // 파일의 절대경로를 구하는 메소드
    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Log.d(TAG, "getRealPathFromURIPath() 실행 1");
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 툴바의 back키 눌렀을 때 동작
            case android.R.id.home: {
                finish();
                return true;
            }
            // 툴바 오른쪽 send 버튼을 눌렀을 때
            case R.id.video_submit:
                // 영상 업로드
                uploadVideoToServer(videoUri.toString());
                // 프로그레스 다이얼로그 실행
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("잠시만 기다려주세요...");
                progressDialog.setCancelable(true);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
                progressDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    // 툴바 메뉴 버튼 생성
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.video_submit, menu);
        return true;
    }


    // 썸네일 이미지를 보고 태그를 자동으로 지정하는 메소드
    private void imageTagging(String thumbnail) {
        Log.d(TAG, "imageTagging: 이미지 태그 시작");
        // Firebase 이미지 객체
        FirebaseVisionImage image = null;
        // 썸네일 이미지
        Uri image_tagging_uri = Uri.fromFile(new File(thumbnail));
        Log.d(TAG, "imageTagging: image_tagging_uri: " + image_tagging_uri);

        {
            try {
                image = FirebaseVisionImage.fromFilePath(VideoInfoAdd.this, image_tagging_uri);
                Log.d(TAG, "onCreate: 트라이");
            } catch (IOException e) {
                Log.d(TAG, "onCreate: 캐치");
                e.printStackTrace();
            }
        }
        Log.d(TAG, "imageTagging: 라벨 객체 생성");
        // 라벨 객체 생성
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();
        Log.d(TAG, "onCreate: image:" + image);

        // image를 processImage() 메소드에 전달
        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        Log.d(TAG, "onSuccess: ML KIT 태깅 성공");
                        // Task completed successfully
                        for (FirebaseVisionImageLabel label : labels) {
                            // 텍스트
                            String text = label.getText();
                            // 항목 ID
                            String entityId = label.getEntityId();
                            // 정확도
                            float confidence = label.getConfidence();


                            Log.d(TAG, "onSuccess: text: " + text);
                            Log.d(TAG, "onSuccess: entityId: " + entityId);
                            Log.d(TAG, "onSuccess: confidence: " + confidence);


                            // 태그 리스트에 이미지 분석으로 나온 영어단어를 넣음
                            enTextList.add(text);
                            Log.d(TAG, "onSuccess: enTextList.size(): "+enTextList.size());




//                            // 공백을 넣어 문자 구분
//                            String semiText = ", " + text;
//
//                            // 이어붙인 문자열
//                            totalText = stringBuilder.append(semiText).toString();
//                            Log.d(TAG, "onSuccess: totalText: " + totalText);
//                            papagoTranslate(text);


                        }

                        Log.d(TAG, "onSuccess: 리스트에 담은 단어들을 반복문으로 번역 요청");
                        // 리스트에 담은 단어들을 반복문으로 번역 요청
                        for (String word : enTextList) {
//                                Log.d(TAG, "onSuccess: enTextList.size(): "+enTextList.size());
                            // 번역 요청
                            papagoTranslate(word);

                        }


                        Log.d(TAG, "onSuccess: ML KIT 반복문 끝");
                        // 이어붙인 문자열을 번역
//                        papagoTranslate(totalText);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ML KIT 태깅 실패");
                    }
                });
    }

    // 파파고 번역 api
    private void papagoTranslate(final String text) {
        Log.d(TAG, "papagoTranslate: 실행");

        // 해쉬맵 객체 생성
        HashMap<String, String> map = new HashMap<>();

        // 영어를 한국어로 번역하도록 설정
        map.put("source", "en");
        map.put("target", "ko");
        map.put("text", text);


        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .papagoNmt(clientId, clientSecret, map);


        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 응답 성공");
                JsonObject jsonObject = response.body();
                Log.d(TAG, "onResponse: jsonObject: " + jsonObject);
                if (jsonObject != null) {
                    // 번역된 단어 ex) 개, 말, 의자, 발표, 하늘 등
                    String word = jsonObject.getAsJsonObject("message").getAsJsonObject("result").get("translatedText").getAsString();
                    // 단어 끝에 붙은 온점 제거
                    String translatedText = word.replace(".","");
                    // 번역된 단어를 리스트에 추가
                    koTextList.add(translatedText);
                    // 리스트 크기
                    Log.d(TAG, "onResponse: koTextList.size(): "+koTextList.size());

//                    TextView tagTextView = new TextView(VideoInfoAdd.this);
//                    tagTextView.setText(translatedText);
//                    buttonLayout.addView(tagTextView);
//
//                    mHorizentalScrollView = new HorizontalScrollView(VideoInfoAdd.this);
//                    mHorizentalScrollView.addView(buttonLayout);

                    Log.d(TAG, "onResponse: 번역 결과: " + translatedText);

                    if (enTextList.size() == koTextList.size()) {
                        Log.d(TAG, "onResponse: 번역 완료");

                        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
                        RecyclerView recyclerView = findViewById(R.id.video_info_add_tag_recycler_view) ;
                        recyclerView.setLayoutManager(new LinearLayoutManager(VideoInfoAdd.this, LinearLayoutManager.HORIZONTAL, false)) ;

                        // 리사이클러뷰에 VideoInfoTagAdapter 객체 지정.
                        VideoInfoTagAdapter adapter = new VideoInfoTagAdapter(koTextList, callbackListener) ;

                        recyclerView.setAdapter(adapter) ;
                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 응답 실패");

            }
        });


    }




}
