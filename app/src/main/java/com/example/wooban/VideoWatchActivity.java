package com.example.wooban;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoWatchActivity extends AppCompatActivity {

    // 내용 나오게 하는 화살표 버튼, 댓글 입력창 옆에 있는 프로필 이미지뷰
    ImageView arrowButton, replyProfileImageView;
    Animation rotate;
    // 글 내용이 담긴 레이아웃
    ConstraintLayout descriptionLayout;
    private JsonObject jsonObject;
    // 해시태그 헬퍼
    private HashTagHelper tagTextHashTagHelper;
    private final static String TAG = "VideoWatchActivity";
    String reply_profile_image_url = null;
    public Context context;
    private RecyclerView recyclerView;
    private StateBroadcastingVideoView videoView;
    String title, profile_image_url, name, description, tag, post_time, views_text, video_url;
    int views, like_count, dislike_count;
    HashMap<String, String> video_info;
    private Bundle bundle;
    private JsonArray jsonArray;
    private ProgressBar progressBar;
    private int video_index;
    private ArrayList<JsonElement> arrayList = new ArrayList();
    public static boolean loadingMore = true;
    private static final String SEGMENT_VIDEO = "video";
    private VideoViewAsync videoViewAsync;
    // 댓글 정보를 저장하는 리스트
    ArrayList<ReplyInfoModel> replyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_watch);
        Log.d(TAG, "onCreate: 실행");
        // 내 프로필 이미지 정보 불러옴
//        getMyData(getSharedToken());

        // 인텐트 받음
        Intent intent = getIntent();
        // intent.getAction(): 딥링크로 실행시킬 경우에는 android.intent.action.VIEW, 그냥 실행될 경우 null
        String action = intent.getAction();
        // intent.getDataString(): 딥링크로 실행시킬 경우 https://wooban.com/video?index=46, 그냥 실행될 경우 null
        String data = intent.getDataString();
        Log.d(TAG, "onCreate: 데이터: " + data);
        Log.d(TAG, "onCreate: action: " + action);
        // 딥링크로 실행될 경우
        if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && data != null) {
            Log.d(TAG, "onCreate: 딥링크로 실행됨");
            handleDeepLink(intent);
        } else {
            Log.d(TAG, "onCreate: 딥링크로 실행되지 않음");

            // 영상 url
            video_url = intent.getStringExtra("video_url");
            // 영상 제목
            title = intent.getStringExtra("title");
            // 조회수
            views = intent.getIntExtra("views", 0);
            // 작성자 프로필 이미지
            profile_image_url = intent.getStringExtra("profile_image_url");
            // 작성자 이름
            name = intent.getStringExtra("name");
            // 영상 설명
            description = intent.getStringExtra("description");
            // 글 작성 시간
            long post_time_millis = intent.getLongExtra("post_time_millis", 0);
            // 영상 태그
            tag = intent.getStringExtra("tag");
            // 내 이름과 내 프로필 이미지가 들어있는 맵 객체
            bundle = intent.getBundleExtra("bundle");
            Log.d(TAG, "onCreate: 영상 시청하는 화면에서 받은 번들 객체: " + bundle);
            // 영상 인덱스 번호
            video_index = intent.getIntExtra("video_index", 0);
            // 좋아요 수
            like_count = intent.getIntExtra("like_count", 0);
            Log.d(TAG, "onCreate: like_count: " + like_count);
            // 싫어요 수
            dislike_count = intent.getIntExtra("dislike_count", 0);
            Log.d(TAG, "onCreate: 영상 시청하는 화면에서 받은 비디오 인덱스: " + video_index);

            Log.d(TAG, "onCreate: post_time_millis: " + post_time_millis);

            Log.d(TAG, "onCreate: video_url: " + video_url);

            // 밀리초를 나타낼 형식으로 변환
            SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. dd.", Locale.KOREA);
            String format_time = format.format(post_time_millis);
            Log.d(TAG, "onCreate: format_time: " + format_time);
            post_time = "게시일: " + format_time;
            views_text = "조회수 " + views + "회";

            // 영상에 대한 정보들
            video_info = new HashMap<>();
            bundle.putString("title", title);
            bundle.putString("profile_image_url", profile_image_url);
            bundle.putString("name", name);
            bundle.putString("description", description);
            bundle.putString("tag", tag);
            bundle.putString("post_time", post_time);
            bundle.putString("views_text", views_text);
            bundle.putInt("like_count", like_count);
            bundle.putInt("dislike_count", dislike_count);
            // 번들에 video_index 추가
            bundle.putInt("video_index", video_index);

            // 다음 동영상 정보 불러옴
            fetchNextVideo();

            // 객체 참조
            videoView = findViewById(R.id.video_watch_video_view);
            // 영상 재생, 제목과 조회수 표시
            videoView.setVideoURI(Uri.parse(video_url));

            videoView.start();
            // 재생 버튼, 일시정지 버튼 등을 만들기 위해 미디어 컨트롤러에 비디오뷰를 넣음
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            // VideoView를 extends 한 커스텀 클래스 StateBroadcastingVideoView를 만들고 인터페이스를 구현해 영상 시작과 일시정지 순간을 포착
            videoView.setPlayPauseListener(new StateBroadcastingVideoView.PlayPauseListener() {
                @Override
                // 영상이 시작되는 순간 영상의 진행률을 파악하는 videoViewAsync 실행
                public void onPlay() {
                    Log.d(TAG, "onPlay: 영상 시작");
                    // Async 생성
                    videoViewAsync = new VideoViewAsync();
                    // Async 실행
                    videoViewAsync.execute();

                }

                @Override
                // 영상을 일시정지 하면 videoViewAsync 종료
                public void onPause() {
                    Log.d(TAG, "onPause: 영상 일시정지");

                    videoViewAsync.cancel(true);

                }
            });
            // 프레임을 0.001초 실행시켜 검은 화면이 나오지 않게 함
            // videoView.seekTo(1);
        }

    }

    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: 실행");
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: 실행");
    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 실행");
    }

    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: 실행");
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: 실행");
        // videoViewAsync 종료
        try {
            if (videoViewAsync.getStatus() == AsyncTask.Status.RUNNING) {
                videoViewAsync.cancel(true);
            }
        } catch (Exception e) {
        }

    }

    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: 실행");
    }


    // 다음 동영상 정보를 불러옴
    private void fetchNextVideo() {
        Log.d(TAG, "fetchNextVideo: 실행되어 다음 동영상 데이터를 가져옴");

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchNextVideoInfo(getSharedToken(), video_index);
        Log.d(TAG, "fetchNextVideo: video_index: " + video_index);

        call.enqueue(new Callback<JsonObject>() {


            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 응답 성공");
                jsonObject = response.body();
                Log.d(TAG, "onResponse: jsonObject: " + jsonObject);
                jsonArray = jsonObject.getAsJsonArray("video_info");

                // 다음 동영상 및 댓글을 보여주는 리사이클러뷰
                recyclerView = findViewById(R.id.video_watch_next_video_recycler_view);
                recyclerBottom(recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManagerWrapper(VideoWatchActivity.this));
                Log.d(TAG, "onCreate: 어댑터 객체 생성 및 리사이클러뷰에 어댑터 장착");
                // 어댑터 객체 생성 및 리사이클러뷰에 어댑터 장착
                NextVideoAdapter adapter = new NextVideoAdapter(getApplicationContext(), jsonArray, replyList, bundle, VideoWatchActivity.this);

                // 화살표 클릭을 위한 리스너 설정
//                findViewById(R.id.next_video_recycler_title_const_layout).setOnClickListener(VideoWatchActivity.this);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 응답 실패");

            }
        });

    }

    // 리사이클러뷰 하단 감지
    private void recyclerBottom(RecyclerView recyclerView) {
        // 리스너 생성
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                // 전체 아이템 갯수
                int totalItemCount = layoutManager.getItemCount();
                // findLastCompletelyVisibleItemPosition - 아이템이 완전히 보일 때 해당 아이템 포지션을 알려줌
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                Log.d(TAG, "onScrolled: totalItemCount: " + totalItemCount);
                Log.d(TAG, "onScrolled: 마지막 아이템 뷰의 포지션: " + lastVisible);
                Log.d(TAG, "onScrolled: loadingMore: " + loadingMore);

                if (lastVisible == totalItemCount - 1 && loadingMore) {
                    Log.d(TAG, "리사이클러뷰 하단 도착");
                    // 프로그레스바 참조
                    progressBar = findViewById(R.id.video_watch_progress_circle);
                    // 댓글 정보를 불러온다.
                    fetchReplyInfo();
                    // 댓글 정보를 한 번 불러온 뒤 다시 불러오는 것을 막는다.
                    loadingMore = false;


                }
            }
        };
        // 리사이클러뷰에 리스너 등록
        recyclerView.addOnScrollListener(onScrollListener);
    }


    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }


    // TODO 댓글에 대한 정보를 보냄
//    // 댓글 작성시 댓글에 대한 정보를 보내는 메소드
////    private void sendReplyInfo() {
////
////        HashMap<String, String> map = new HashMap<>();
////        map.put("name", na)
////
////
////        Call<JsonObject> call = RetrofitClient
////                .getInstance()
////                .getApi()
////                .reply_post(getSharedToken());
////
////
////    }

    // 사용자 본인의 프로필 이미지와 이름을 불러온다.
//    private void getMyData(String token) {
//        Log.d(TAG, "getMyData: 실행");
//        // 레트로핏 객체
//        Call<JsonObject> call = RetrofitClient
//                .getInstance()
//                .getApi()
//                .getMainData(token);
//
//        call.enqueue(new Callback<JsonObject>() {
//
//            // 응답 성공시
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.d(TAG, "응답 성공");
//                // 서버에서 받아온 데이터
//                JsonObject data = response.body();
//
//                // 토큰으로 정상적으로 로그인 된 경우
//                if (data != null) {
//                    // 이름
//                    String name = data.get("name").getAsString();
//                    Log.d(TAG, "onResponse: name: " + name);
//
//                    // 프로필 이미지 url값이 null이 아닐 때 프로필 이미지 url
//                    reply_profile_image_url = data.get("profile_image").getAsString();
//                    Log.d(TAG, "onResponse: reply_profile_image_url : " + reply_profile_image_url);
//
//                    /* 프로필 이미지가 default이라는 단어를 포함하면 기본 이미지가 나타나고,
//                     그렇지 않으면 본인이 선택한 이미지가 나타남 */
//                    if (reply_profile_image_url.contains("default")) {
//                        Log.d(TAG, "프로필 이미지가 default. onResponse: reply_profile_image_url: " + reply_profile_image_url);
//                        // 기본 이미지 중 선택한 이미지를 이미지뷰에 넣어 보여준다.
////                        DefaultProfileImage defaultImage = new DefaultProfileImage();
////                        defaultImage.changeToAnimal(VideoWatchActivity.this, reply_profile_image_url, replyProfileImageView);
//                    } else {
//                        Log.d(TAG, "프로필 이미지가 default 아님. onResponse: reply_profile_image_url: " + reply_profile_image_url);
////                        Glide.with(VideoWatchActivity.this).load(reply_profile_image_url).apply(RequestOptions.circleCropTransform()).into(replyProfileImageView);
//                    }
//
//                    /* 댓글 리사이클러뷰 */
//
//                    // 리사이클러뷰에 표시할 데이터 리스트 생성.
//                    ArrayList<String> list = new ArrayList<>();
//                    for (int i = 0; i < 10; i++) {
//                        list.add(String.format("TEXT %d", i));
//                    }
//
//                    // 리사이클러뷰에 LinearLayoutManager 객체 지정.
////                    RecyclerView recyclerView = findViewById(R.id.video_watch_reply_recycler_view);
//                    recyclerView.setLayoutManager(new LinearLayoutManager(VideoWatchActivity.this));
//                    // 리사이클러뷰 하단 감지
////                    recyclerBottom(recyclerView);
//
//                    // 리사이클러뷰에 VideoReplyAdapter 객체 지정. VideoReplyAdapter에 list와 이름, 프로필 이미지를 넘김
//                    context = getApplicationContext();
//                    VideoReplyAdapter adapter = new VideoReplyAdapter(list, reply_profile_image_url, name, context);
//                    Log.d(TAG, "onResponse: 어댑터에 전송되는 프로필 이미지: " + reply_profile_image_url);
//                    Log.d(TAG, "onResponse: 어댑터에 전송되는 이름: " + name);
//                    Log.d(TAG, "onResponse: 어댑터에 전송되는 context: " + context);
//                    // 커스텀한 리스너 객체를 어댑터에 전달
////        adapter.setOnMyProfileImageListener(new VideoReplyAdapter.myProfileImageListener() {
////            @Override
////            public void profileImage() {
////                getMyData(getSharedToken());
////
////            }
////        });
//                    recyclerView.setAdapter(adapter);
//
//
//                    // 토큰이 만료되어 로그인 되지 않은 경우(서버에서 받은 데이터가 null이 됨)
//                } else {
//                    Log.d(TAG, "onResponse: 토큰 만료되어 로그아웃된 상태");
//
//                }
//
//            }
//
//            // 응답 실패시 (토큰이 없는 상황, 로그아웃 상태)
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d(TAG, "onFailure: 응답 실패");
//
//            }
//        });
//
//
//    }

    // 페이징을 위해 db에 있는 댓글 정보를 불러오는 메소드
//    private void fetchReplyInfo() {
//
//        /*헤더와 푸터가 포함되기 전 jsonArray.size() = 5 이므로 2를 더함
//         * 페이징으로 댓글을 불러오면 불러온 댓글 갯수만큼 jsonArray.size()가 늘어남 */
//        Call<JsonObject> call = RetrofitClient
//                .getInstance()
//                .getApi()
//                .fetchReplyInfo(getSharedToken(), video_index, 0, jsonArray.size() + 2, 0);
//        Log.d(TAG, "fetchReplyInfo: jsonArray.size()+2: " + jsonArray.size() + 2);
//
//        call.enqueue(new Callback<JsonObject>() {
//
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                Log.d(TAG, "onResponse: 댓글 정보 가져오기 성공");
//                JsonObject reply_data = response.body();
//                Log.d(TAG, "onResponse: reply_data: " + reply_data);
//
//                /*가져온 댓글 정보가 없으면, 즉 paged_reply_data.size()가 0이 되면 마지막 댓글임을 알리고
//                 * 프로그레스 바를 안보이게 함*/
//                JsonArray pagedReplyData = null;
//                if (reply_data != null) {
//                    pagedReplyData = reply_data.getAsJsonArray("reply_info");
//                    Log.d(TAG, "onResponse: pagedReplyData: " + pagedReplyData);
//                    Log.d(TAG, "onResponse: pagedReplyData.size(): " + pagedReplyData.size());
//                }
//
//                if (pagedReplyData != null && pagedReplyData.size() == 0) {
//                    Toast.makeText(VideoWatchActivity.this, "마지막 댓글입니다.", Toast.LENGTH_SHORT).show();
//                }
//
//                // JsonObject가 아닌 값을 넣음(인덱스 5번에 들어가게 됨)
//                jsonArray.add(false);
//
//
//                /** 어댑터 이용한 프로그레스바 */
//                Log.d(TAG, "onResponse: jsonArray.size(): " +jsonArray.size());
//
//                // 어댑터에 변경된 데이터를 알리면 데이터가 false인 포지션에서 프로그레스 바가 나타남
//                // 포지션 7에서 프로그레스 바가 나타남
//                int position = jsonArray.size() + 1;
//                recyclerView.getAdapter().notifyItemInserted(position);
//                Log.d(TAG, "onResponse: notifyItemInserted 포지션: " + position);
//                Log.d(TAG, "onResponse: 데이터가 추가된 후 jsonArray: " + jsonArray);
//
//                // 1초 딜레이
//                Handler handler = new Handler();
//                final JsonArray finalPagedReplyData = pagedReplyData;
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        // jsonArray 인덱스 5의 데이터(false) 삭제. jsonArray 사이즈 = 5 됨
//                        jsonArray.remove(jsonArray.size() - 1);
//                        int number = jsonArray.size() - 1;
//                        Log.d(TAG, "run: 삭제된 데이터가 있는 인덱스: " + number);
//                        Log.d(TAG, "run: 삭제된 후 jsonArray: " + jsonArray);
//                        // 스크롤 포지션 = 7
//                        int scrollPosition = jsonArray.size() + 2;
//                        // 포지션 7번 아이템이 제거된것을 알림
//                        recyclerView.getAdapter().notifyItemRemoved(scrollPosition);
//                        // 한번 페이징에 보여줄 댓글 갯수
//                        int show_once = 10;
//                        // 몇번째 페이징인지
//                        int require_count = (int) Math.floor(((jsonArray.size() - 7) / show_once) + 1);
//
//                        int jsonArrayReplyCount = recyclerView.getAdapter().getItemCount() - 7 - (10 * (require_count - 1));
//                        Log.d(TAG, "run: 리사이클러뷰 jsonArray의 각 페이징 단계에 들어가 있는 댓글 갯수: " + jsonArrayReplyCount);
//                        Log.d(TAG, "run: 가져온 댓글 갯수: " + finalPagedReplyData.size());
//
//                        // jsonArray 인덱스 5번부터 페이징해 가져온 댓글들을 넣음.
//                        // 댓글이 jsonArray에 들어가 있으면 중복되어 들어가지 않게 함
//                        // ex) 댓글 6개일때 jsonArray에 이미 댓글 데이터가 들어가 리사이클러뷰에 보여지고 있다면 jsonArrayReplyCount = 6,
//                        // 댓글이 25개이면 두번째 페이징 단계에 필요한 댓글 갯수는 8개이므로 jsonArrayReplyCount = 8
//
//                        if (finalPagedReplyData != null && jsonArrayReplyCount != finalPagedReplyData.size() ) {
//                            for (JsonElement element : finalPagedReplyData) {
//                                jsonArray.add(element);
//                            }
//                        }
//                        // 어댑터에 데이터 변경을 알림
//                        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
//                        // 다시 데이터 로딩이 될 수 있게 함
//                        loadingMore = true;
//
//                    }
//                }, 1000);
//
//
//                Log.d(TAG, "onResponse: 댓글 추가된 jsonArray: " + jsonArray);
//
//            }
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//                Log.d(TAG, "onFailure: 댓글 정보 가져오기 실패");
//
//            }
//        });
//    }


    // 페이징을 위해 db에 있는 댓글 정보를 불러오는 메소드
    private void fetchReplyInfo() {

        /*헤더와 푸터가 포함되기 전 jsonArray.size() = 5 이므로 2를 더함
         * 페이징으로 댓글을 불러오면 불러온 댓글 갯수만큼 jsonArray.size()가 늘어남 */
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchReplyInfo(getSharedToken(), video_index, 0, replyList.size(), 0);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 댓글 정보 가져오기 성공");
                JsonObject object = response.body();
                Log.d(TAG, "onResponse: object: " + object);
                JsonElement element = object.get("reply_info");
                boolean lastPage = element.getAsJsonArray().get(0).getAsJsonObject().get("last_page").getAsBoolean();
                // 댓글 총 갯수와 리스트 크기가 같아지면

                if (NextVideoAdapter.total_reply_count != 0) {
                    if (lastPage) {
                        Toast.makeText(VideoWatchActivity.this, "마지막 댓글입니다.", Toast.LENGTH_SHORT).show();
                        loadingMore = true;
                    } else {
                        JsonObject reply_data = response.body();
                        Log.d(TAG, "onResponse: reply_data: " + reply_data);
                        // 페이징 위한 작업
                        paging(reply_data);

                    }
                } else {
                    Toast.makeText(VideoWatchActivity.this, "첫번째 댓글을 남겨보세요!", Toast.LENGTH_SHORT).show();
                    loadingMore = true;
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 댓글 정보 가져오기 실패");

            }
        });
    }


    // 딥링크 처리
    private void handleDeepLink(Intent intent) {
        Log.d(TAG, "handleDeepLink: 실행");
        FirebaseDynamicLinks.getInstance()
                // getDynamicLink(): Intent로부터 deep link가 존재하는지 알아옴
                .getDynamicLink(intent)
                .addOnSuccessListener(VideoWatchActivity.this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Log.d(TAG, "onSuccess: 딥링크 추출 성공");
                        if (pendingDynamicLinkData == null) {
                            Log.d(TAG, "onSuccess: pendingDynamicLinkData: " + pendingDynamicLinkData);
                            Log.d(TAG, "No have dynamic link");
                            return;
                        }
                        Uri deepLink = pendingDynamicLinkData.getLink();
                        Log.d(TAG, "deepLink: " + deepLink);

                        String segment = deepLink.getLastPathSegment();
                        switch (segment) {
                            case SEGMENT_VIDEO:
                                // 영상 인덱스 번호
                                video_index = Integer.parseInt(Objects.requireNonNull(deepLink.getQueryParameter("index")));
                                String link_reply_profile_image_url = deepLink.getQueryParameter("reply_profile_image_url");
                                String link_reply_name = deepLink.getQueryParameter("reply_name");
                                Log.d(TAG, "onSuccess: link_video_index: " + video_index);
                                Log.d(TAG, "onSuccess: link_reply_profile_image_url: " + link_reply_profile_image_url);
                                Log.d(TAG, "onSuccess: link_reply_name: " + link_reply_name);
                                fetchVideoInfoByIdx(video_index, link_reply_profile_image_url, link_reply_name);
                                break;
                        }
                    }
                })
                .addOnFailureListener(VideoWatchActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: 실패");
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    // 영상 정보를 불러옴
    private void fetchVideoInfoByIdx(int video_index, final String reply_profile_image_url, final String reply_name) {
        Log.d(TAG, "fetchVideoInfo: 실행");

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchVideoInfoByIdx(getSharedToken(), video_index);

        call.enqueue(new Callback<JsonObject>() {


            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 영상 정보 불러오기 응답 성공");
                jsonObject = response.body();
                Log.d(TAG, "onResponse: jsonObject: " + jsonObject);
                JsonObject videoInfo = jsonObject.getAsJsonArray("video_info").get(0).getAsJsonObject();
                // 영상 url
                video_url = videoInfo.get("video_url").getAsString();

                // 번들을 만듬
                makeBundle(videoInfo, reply_profile_image_url, reply_name);
                Log.d(TAG, "onResponse: ");

                // 다음 동영상 정보 불러옴
                fetchNextVideo();

                // 객체 참조
                videoView = findViewById(R.id.video_watch_video_view);
                // 영상 재생, 제목과 조회수 표시
                videoView.setVideoURI(Uri.parse(video_url));
                videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });
                videoView.start();
                // 재생 버튼, 일시정지 버튼 등을 만들기 위해 미디어 컨트롤러에 비디오뷰를 넣음
                MediaController mediaController = new MediaController(VideoWatchActivity.this);
                videoView.setMediaController(mediaController);
                mediaController.setAnchorView(videoView);
                // 프레임을 0.001초 실행시켜 검은 화면이 나오지 않게 함
                // videoView.seekTo(1);

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 영상 정보 불러오기 응답 실패");

            }
        });
    }

    private void makeBundle(JsonObject videoInfo, String reply_profile_image_url, String reply_name) {

        // 밀리초를 나타낼 형식으로 변환
        SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. dd.", Locale.KOREA);
        String format_time = format.format(videoInfo.get("post_time_millis").getAsLong());
        Log.d(TAG, "onCreate: format_time: " + format_time);
        post_time = "게시일: " + format_time;
        views_text = "조회수 " + views + "회";

        // 번들에 영상 데이터 추가
        Log.d(TAG, "makeBundle: videoInfo.get(\"title\").getAsString(): " + videoInfo.get("title").getAsString());
        bundle = new Bundle();
        // 영상 제목
        bundle.putString("title", videoInfo.get("title").getAsString());
        // 영상 작성자 프로필 이미지
        bundle.putString("profile_image_url", videoInfo.get("profile_image_url").getAsString());
        // 영상 작성자 이름
        bundle.putString("name", videoInfo.get("name").getAsString());
        // 영상 제목
        bundle.putString("title", videoInfo.get("title").getAsString());
        // 영상 설명
        bundle.putString("description", videoInfo.get("description").getAsString());
        // 영상 태그
        bundle.putString("tag", videoInfo.get("tag").getAsString());
        Log.d(TAG, "makeBundle: 태그 내용: " + videoInfo.get("tag").getAsString());
        // 영상 인덱스 번호
        bundle.putInt("video_index", videoInfo.get("idx").getAsInt());
        Log.d(TAG, "makeBundle: 영상 인덱스 번호: " + videoInfo.get("idx").getAsString());
        // 영상 게시일
        bundle.putString("post_time", post_time);
        // 영상 조회수
        Log.d(TAG, "makeBundle: views_text: " + views_text);
        bundle.putString("views_text", views_text);
        // 댓글 창 옆 내 이름
        bundle.putString("my_name", reply_name);
        Log.d(TAG, "makeBundle: reply_name:" + reply_name);
        // 댓글 창 옆 내 프로필 이미지
        bundle.putString("my_profile_image_url", reply_profile_image_url);
        // 토큰에서 가져온 내 아이디
        bundle.putString("my_id", videoInfo.get("id").getAsString());
        // 좋아요 수
        bundle.putInt("like_count", videoInfo.get("like_count").getAsInt());
        // 싫어요 수
        bundle.putInt("dislike_count", videoInfo.get("dislike_count").getAsInt());


    }

    // 영상을 얼마나 시청했는지 파악하여 조회수를 증가시키는 AsyncTask
    private class VideoViewAsync extends AsyncTask<Void, Integer, Void> {
        int duration = 1;
        int current = 0;
        int progress = 0;

        @Override
        protected Void doInBackground(Void... params) {

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {

                }
            });

            do {
                duration = videoView.getDuration();
                current = videoView.getCurrentPosition();
                Log.d(TAG, "doInBackground: 영상 전체 길이 duration: " + duration);
                try {
                    // publishProgress()를 통해 onProgressUpdate()가 호출되며 publishProgress()의 매개변수 값을 넘겨받음
                    // progress = 영상 진행정도
                    progress = current * 100 / duration;
                    Log.d(TAG, "doInBackground: 현재까지 재생된 길이 current: " + current);
                    Log.d(TAG, "doInBackground: 진행률 progress: " + progress);
                    publishProgress(progress);
                    // progress가 50 이상이거나 asyc가 종료되면 반복문 종료
                    Log.d(TAG, "doInBackground: 종료되었는가: " + videoViewAsync.isCancelled());
                    if (progress >= 50 || videoViewAsync.isCancelled()) {
                        Log.d(TAG, "doInBackground: 반복문 종료");
                        break;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "doInBackground: 예외 발생");

                    // 예외 내용 출력
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String exceptionAsStrting = sw.toString();

                    Log.d(TAG, exceptionAsStrting);
                }
                // 진행정도가 50 이하이면 반복문이 계속됨.
            } while (progress <= 50);

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // 영상을 절반까지 봤을 경우
            if (values[0] == 50) {
                // Toast.makeText(VideoWatchActivity.this, "진행률: " + values[0] + "% 달성", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onProgressUpdate: 진행률" + values[0] + "% 달성");
                // 조회수 증가
                addViewsCount();
            }
        }
    }

    // 영상 조회수를 증가시킴
    private void addViewsCount() {

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .addViewsCount(getSharedToken(), video_index);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 조회수 증가 응답 성공");
            }


            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 조회수 증가 응답 실패");


            }
        });
    }

    // 페이징을 위한 작업
    private void paging(final JsonObject reply_data) {
        replyList.add(null);

        /** 어댑터 이용한 프로그레스바 */

        // 어댑터에 변경된 데이터를 알리면 데이터가 null인 포지션에서 프로그레스 바가 나타남
        int num = recyclerView.getAdapter().getItemCount() - 1;
        recyclerView.getAdapter().notifyItemInserted(num);
        Log.d(TAG, "onResponse: notifyItemInserted 포지션: " + num);
        Log.d(TAG, "onResponse: 데이터가 추가된 후 리스트: " + replyList);

        // 1초 딜레이
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // jsonArray 인덱스 5의 데이터(false) 삭제. jsonArray 사이즈 = 5 됨
                replyList.remove(replyList.size() - 1);
                int number = replyList.size() - 1;
                Log.d(TAG, "run: 삭제된 데이터가 있는 인덱스: " + number);
                Log.d(TAG, "run: 삭제된 후 replyList: " + replyList);
                // null이 들어간 스크롤 포지션
                int scrollPosition = replyList.size() + jsonArray.size() + 2;
                // 스크롤 포지션 아이템이 제거된것을 알림
                recyclerView.getAdapter().notifyItemRemoved(scrollPosition);

                // 가져온 JsonArray
                assert reply_data != null;
                JsonArray array = reply_data.getAsJsonArray("reply_info");

                // JsonArray 안에 있는 JsonObject를 꺼내 파싱
                for (JsonElement object : array) {
                    ReplyInfoModel model = new Gson().fromJson(object, ReplyInfoModel.class);

                    // 리스트에 모델 추가
                    replyList.add(model);
                    // 어댑터 갱신
                    Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                    // 다시 데이터 로딩이 될 수 있게 함
                    loadingMore = true;
                }

            }
        }, 1000);
    }


}