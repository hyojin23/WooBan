package com.example.wooban;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoUploadActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "VideoUploadActivity";
    private Button videoUploadBtn;
    private static final int REQUEST_VIDEO_CAPTURE = 300;
    private static final int READ_REQUEST_CODE = 200;
    private Uri uri;
    // 영상 절대경로, 재생시간
    private String name, profile_image_url, pathToStoredVideo, duration;
    // private VideoView displayRecordedVideo;
    private static final String SERVER_PATH = "http://54.180.104.190/android/";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    private ArrayList<VideoModel> arrayListVideos;

    // 권한을 설정하는 String 배열
//    String[] permission_list = {
//            Manifest.permission.READ_EXTERNAL_STORAGE
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);
        Log.d(TAG, "onCreate: 실행");

        // 툴바
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 뒤로가기 화살표를 원하는 아이콘으로 변경
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        // 툴바 제목
        getSupportActionBar().setTitle("");

        // 외부 저장소에 대한 접근 권한 확인
        checkPermission();


        // 녹화 버튼
        findViewById(R.id.video_caputure_button_layout).setOnClickListener(this);
        // 실시간 스트리밍 시작 버튼
        findViewById(R.id.streaming_start_button_layout).setOnClickListener(this);


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: 실행");
        recyclerViewInit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 동영상 촬영이 종료되면
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_VIDEO_CAPTURE) {
            Log.d(TAG, "onActivityResult() 실행");
            // onRestart()가 호출되며 영상을 다시 불러온다.

        }
    }

    private String getFileDestinationPath() {
        String generatedFilename = String.valueOf(System.currentTimeMillis());
        String filePathEnvironment = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File directoryFolder = new File(filePathEnvironment + "/video/");
        if (!directoryFolder.exists()) {
            directoryFolder.mkdir();
        }
        Log.d(TAG, "Full path " + filePathEnvironment + "/video/" + generatedFilename + ".mp4");
        return filePathEnvironment + "/video/" + generatedFilename + ".mp4";
    }

    // 권한 허용 결과
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: 실행");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoUploadActivity.this);
    }


    // 권한이 허용되었을 경우
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted() 실행");
        // 리사이클러뷰 초기화 및 영상 가져오기
        recyclerViewInit();

        // 녹화가 종료되고 권한이 허용된 것이 확인되면 영상의 절대경로를 구하고 녹화된 영상을 서버에 업로드한다.
        if (uri != null) {
            if (EasyPermissions.hasPermissions(VideoUploadActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Log.d(TAG, "onPermissionsGranted: 외부 저장소에 대한 권한이 허용되었다면");
//                displayRecordedVideo.setVideoURI(uri);
//                displayRecordedVideo.start();

                pathToStoredVideo = getRealPathFromURIPath(uri, VideoUploadActivity.this);
                Log.d(TAG, "Recorded Video Path " + pathToStoredVideo);
                //Store the video to your server
                uploadVideoToServer(pathToStoredVideo);

            }
        }
    }


    // 권한을 거부했을 경우
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: 퍼미션 요청을 거부");
        Toast.makeText(this, "원활한 앱 사용을 위해 권한 동의가 필요합니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    // 영상을 서버에 업로드하는 메소드
    private void uploadVideoToServer(String pathToVideoFile) {
        Log.d(TAG, "uploadVideoToServer() 실행");

        // 파일 객체 생성
        File videoFile = new File(pathToVideoFile);
        Log.d(TAG, "videoFile: " + videoFile);


        // 요청에 필요한 객체
        RequestBody videoBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
        MultipartBody.Part vFile = MultipartBody.Part.createFormData("video", videoFile.getName(), videoBody);

        Log.d(TAG, "videoFile.getName(): " + videoFile.getName());
        Log.d(TAG, "videoBody: " + videoBody);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SERVER_PATH)
//                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        VideoInterface vInterface = retrofit.create(VideoInterface.class);
        Call<ResultObject> serverCom = vInterface.uploadVideoToServer(vFile);
        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                Log.d(TAG, "uploadVideoToServer() 응답 성공");
                ResultObject result = response.body();
                if (!TextUtils.isEmpty(result.getSuccess())) {
                    // 영상이 업로드 되었다는 토스트를 띄움
                    Toast.makeText(VideoUploadActivity.this, result.getSuccess(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "결과 Result " + result.getSuccess());
                }
            }

            @Override
            public void onFailure(Call<ResultObject> call, Throwable t) {
                Log.d(TAG, "uploadVideoToServer() 응답 실패");
                Log.d(TAG, "Error message " + t.getMessage());
            }
        });
    }

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

    // 레트로핏 디버깅 위한 HttpLoggingInterceptor
    public static OkHttpClient getClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        return client;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // 녹화 버튼 클릭
            case R.id.video_caputure_button_layout:
                Log.d(TAG, "녹화 버튼 클릭");
                // 동영상 촬영을 위해 인텐트로 카메라 앱 실행
                Intent videoCaptureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                // 인텐트를 수신할 수 있는 앱이 있는지 확인
                if (videoCaptureIntent.resolveActivity(getPackageManager()) != null) {
                    // 인텐트 실행
                    startActivityForResult(videoCaptureIntent, REQUEST_VIDEO_CAPTURE);
                }
                break;
            // 실시간 스트리밍 시작 버튼 클릭
            case R.id.streaming_start_button_layout:
                Log.d(TAG, "onClick: 실시간 스트리밍 시작 버튼 클릭");
                Toast.makeText(VideoUploadActivity.this, "추후 업데이트 될 기능입니다.", Toast.LENGTH_SHORT).show();

        }
    }

    // 리사이클러뷰 초기화
    private void recyclerViewInit() {
        Log.d(TAG, "recyclerView: recyclerViewInit() 실행");
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewVideo);
        // 그리드 레이아웃. spanCount: 비디오를 보여줄 칼럼 수
        recyclerViewLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        arrayListVideos = new ArrayList<>();
        // 갤러리에 비디오를 가져온다
        fetchVideoFromGallery();

    }


    //
    public void checkPermission() {

        // 권한이 있는지 확인한 후 권한이 있을 경우
        if (EasyPermissions.hasPermissions(VideoUploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.d(TAG, "checkPermission: 외부 저장소에 대한 접근 권한이 있음");
            // 비디오 파일 경로 ex) /storage/emulated/0/DCIM/Camera/20190907_215330.mp4
            recyclerViewInit();


            // 절대경로를 얻어옴
//            pathToStoredVideo = getRealPathFromURIPath(uri, VideoUploadActivity.this);
//            Log.d(TAG, "checkPermission: 외부 저장소에 대한 접근 권한이 있음");
//            Log.d(TAG, "Recorded Video Path " + pathToStoredVideo);
            //Store the video to your server
            // 이 동영상을 서버에 저장
//            uploadVideoToServer(pathToStoredVideo);


            // 권한이 없을 경우 권한 요청
        } else {
            Log.d(TAG, "checkPermission: 외부 저장소에 대한 접근 권한이 없음");
            EasyPermissions.requestPermissions(VideoUploadActivity.this, getString(R.string.read_file), READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }


    }

    /*   권한 요청 부분. easy permission 을 사용하기 때문 주석처리 */

//    public void checkPermission(){
//        Log.d(TAG, "checkPermission: 퍼미션 체크");
//        //현재 안드로이드 버전이 6.0미만이면 메서드를 종료한다.
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return;
//        }
//
//        for(String permission : permission_list){
//            //권한 허용 여부를 확인한다.
//            int chk = checkCallingOrSelfPermission(permission);
//
//            // 권한이 거부된 상태이면
//            if(chk == PackageManager.PERMISSION_DENIED){
//                //권한 허용을여부를 확인하는 창을 띄운다
//                requestPermissions(permission_list,0);
//                // 권한이 허용되엇으면 init() 실행
//            } else {
//                recyclerViewInit();
//            }
//        }
//    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode==0)
//        {
//            for(int i=0; i<grantResults.length; i++)
//            {
//                //허용됬다면
//                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
//                    recyclerViewInit();
//                }
//                // 허용되지 않았다면
//                else {
//                    Toast.makeText(getApplicationContext(),"기능 사용을 위한 권한 동의가 필요합니다.",Toast.LENGTH_LONG).show();
//                    finish();
//                }
//            }
//        }
//    }


    private void fetchVideoFromGallery() {
        Log.d(TAG, "fetchVideoFromGallery: 실행");

        // MainActivity로부터 받은 값
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        profile_image_url = intent.getStringExtra("profile_image_url");


        Uri uri;
        Cursor cursor;
        int column_index_data, thum, column_index_id;

        // 이미지 절대 경로, 데이터 아이디
        String absolutePathImage, data_id = null;


        // 외부 저장소에 있는 uri
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Log.d(TAG, "fetchVideoFromGallery: uri:" + uri);

        // 검색된 각 행의 열 배열
        String[] projection =
                {MediaStore.MediaColumns.DATA,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Thumbnails.DATA};
        Log.d(TAG, "fetchVideoFromGallery: projection: " + projection);


        // 날짜순서
        String orderBy = MediaStore.Images.Media.DATE_TAKEN;

        Log.d(TAG, "fetchVideoFromGallery:  orderBy:" + orderBy);

        // 쿼리. 외부저장소에 저장된 데이터를 최신순으로 정렬하여 가져온다.
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        Log.d(TAG, "fetchVideoFromGallery: cursor: " + cursor);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        Log.d(TAG, "fetchVideoFromGallery: column_index_data: " + column_index_data);
        column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
        Log.d(TAG, "fetchVideoFromGallery: column_index_id: " + column_index_id);
        thum = cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);
        Log.d(TAG, "fetchVideoFromGallery: thum: " + thum);


        // 썸네일 구하기 위한 커서와 uri
        Cursor thum_cursor;
        Uri thum_uri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
        // 썸네일 데이터 컬럼
        String[] thum_projection = {
                MediaStore.Video.Thumbnails.DATA};
        // 비디오 id가 큰 순서로 정렬하기 위해 사용
        String thum_orderBy = MediaStore.Video.Thumbnails.VIDEO_ID;
        // MediaStore.MediaColumns.DATA에서 구한 id값과 MediaStore.Video.Thumbnails에서 아이디값이 같은지 비교하기 위한 selection
        String thum_selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";

//TODO 비디오 썸네일
        while (cursor.moveToNext()) {
            // 비디오 절대경로
            absolutePathImage = cursor.getString(column_index_data);
            Log.d(TAG, "fetchVideoFromGallery: column_index_data: " + column_index_data);
            Log.d(TAG, "fetchVideoFromGallery: thum: " + thum);
            // 비디오 데이터의 고유 id
            data_id = cursor.getString(column_index_id);
            long now_id = cursor.getLong(column_index_id);
            Log.d(TAG, "fetchVideoFromGallery: data_id: " + data_id);

            // 비트맵으로 영상에 대한 썸네일을 만들어 /storage/emulated/0/DCIM/.thumbnails에 저장
            Bitmap bit = MediaStore.Video.Thumbnails.getThumbnail(VideoUploadActivity.this.getContentResolver(),
                    now_id, MediaStore.Video.Thumbnails.MICRO_KIND, null);


            VideoModel videoModel = new VideoModel();
            // 선택 되었는지 판단하는 boolean
            videoModel.setBoolean_selected(false);
            // 비디오 경로
            videoModel.setStr_path(absolutePathImage);
            Log.d(TAG, "fetchVideoFromGallery: absolutePathImage:" + absolutePathImage);

            // MediaStore.MediaColumns.DATA에서 구한 id값
            String[] thum_selectionArgs = {data_id};
            Log.d(TAG, "fetchVideoFromGallery: thum_selectionArgs: "+thum_selectionArgs);

            Log.d(TAG, "fetchVideoFromGallery: MediaStore.Video.Thumbnails.VIDEO_ID : "+MediaStore.Video.Thumbnails.VIDEO_ID );

            Log.d(TAG, "fetchVideoFromGallery: thum_uri: "+thum_uri);
            Log.d(TAG, "fetchVideoFromGallery: thum_projection: "+thum_projection);
            // 쿼리문
            thum_cursor = getApplicationContext()
                    .getContentResolver().query(thum_uri, thum_projection, thum_selection, thum_selectionArgs, thum_orderBy + " DESC");
            // 커서를 다음 행으로 이동시킴
            thum_cursor.moveToNext();
            Log.d(TAG, "fetchVideoFromGallery: thum_cursor.getCount() 수: "+thum_cursor.getCount());
            // .thumbnail 폴더에 썸네일 이미지가 있을 경우(썸네일 경로를 가져온다.)
            if(thum_cursor.getCount() > 0)
            {
                Log.d(TAG, "fetchVideoFromGallery: thum_cursor.getCount() > 0 ");
                // 결과를 받음
                String thumbnail = thum_cursor.getString(0);
                Log.d(TAG, "fetchVideoFromGallery: thumbnail: "+thumbnail);
                videoModel.setStr_thumb(thumbnail);
            // .thumbnail 폴더에 썸네일 이미지가 없는 경우(썸네일을 직접 만든다.)
            } else {
                Log.d(TAG, "fetchVideoFromGallery: thum_cursor.getCount() <= 0");
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(absolutePathImage, MediaStore.Images.Thumbnails.MINI_KIND);
                // 현재시간으로 파일이름을 만듦
                long time = System.currentTimeMillis();
                String imageName = String.valueOf(time);
                Log.d(TAG, "fetchVideoFromGallery: imageName: "+imageName);
                saveBitmapToJpeg(bitmap, imageName);
                // 캐시에 저장된 파일 경로를 불러옴
//                String path = getCacheDir()+ "/" + imageName+".jpg";
                // 외부저장소에 저장된 파일 이름
                String exPath = Environment.getExternalStorageDirectory() +"/DCIM/.thumbnails"+imageName+".jpg";
                Log.d(TAG, "fetchVideoFromGallery: exPath: "+exPath);
//                Log.d(TAG, "fetchVideoFromGallery: path: "+path);
                videoModel.setStr_thumb(exPath);
            }


//            getThumbnails(data_id);



            // 비디오 재생시간
            videoModel.setDuration(getVideoDuration(absolutePathImage));
            // 이름
            videoModel.setName(name);
            Log.d(TAG, "fetchVideoFromGallery: name: " + name);
            // 프로필 이미지
            videoModel.setProfile_image_url(profile_image_url);
            Log.d(TAG, "fetchVideoFromGallery: profile_image_url: " + profile_image_url);
            // 리스트에 videoModel 객체 추가
            arrayListVideos.add(videoModel);


        }


        // call the adapter class and set it to recyclerview

        Log.d(TAG, "fetchVideoFromGallery: 어댑터를 불러온 뒤 리사이클러뷰에 세팅");
        VideoAdapter videoAdapter = new VideoAdapter(getApplicationContext(), arrayListVideos, VideoUploadActivity.this);
        recyclerView.setAdapter(videoAdapter);

    }

    // 비디오 재생시간 구하는 함수. 인자로 절대경로를 입력하니 안됨
//    public String getVideoDuration(String uri) {
//        Log.d(TAG, "getVideoDuration: uri: " + uri);
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(uri, new HashMap<String, String>());
//        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        long timeInMillisec = Long.parseLong(time);
//        retriever.release();
//        duration = convertMillieToHMmSs(timeInMillisec); //use this duration
//        return duration;
//    }


    // long 형태의 파일 재생 시간을 String으로 바꿔주는 메소드
    public static String convertMillieToHMmSs(long millie) {
        long seconds = (millie / 1000);
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        String result = "";
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            return String.format("%02d:%02d", minute, second);
        }

    }

    // 파일 재생 시간을 구하는 메소드
    private String getVideoDuration(String videoPath) {
        MediaPlayer mediaPlayer = MediaPlayer.create(VideoUploadActivity.this, Uri.parse(videoPath));
        long longDuration = mediaPlayer.getDuration();
        // 미디어 플레이어 연결 해제
        mediaPlayer.release();
        String duration = convertMillieToHMmSs(longDuration);
        Log.d(TAG, "getVideoDuration: long 형태의 영상 길이: " + longDuration);
        Log.d(TAG, "get: duration: " + duration);
        return duration;
    }

    // 메뉴 선택시
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 툴바의 back키 눌렀을 때 동작
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // 비트맵을 jpeg로 저장
    private void saveBitmapToJpeg(Bitmap bitmap, String name) {
        //내부저장소 캐시 경로를 받아옵니다.
        File storage = getCacheDir();
        // 외부저장소 경로 ex) /storage/emulated/0
        File externalStorage = Environment.getExternalStorageDirectory();
        Log.d(TAG, "saveBitmapToJpeg: externalStorage: "+externalStorage);
        //저장할 파일 이름
        String fileName = name + ".jpg";
        Log.d(TAG, "saveBitmapToJpeg: fileName: "+fileName);
        //storage 에 파일 인스턴스를 생성합니다.
        File tempFile = new File(externalStorage+"/DCIM/.thumbnails", fileName);
        try {
            Log.d(TAG, "saveBitmapToJpeg: tempFile: "+tempFile);
            // 자동으로 빈 파일을 생성합니다.
            tempFile.createNewFile();
            Log.d(TAG, "saveBitmapToJpeg:  tempFile.createNewFile(): "+ tempFile.createNewFile());
            Log.d(TAG, "saveBitmapToJpeg: 빈 파일 생성");
            // 파일을 쓸 수 있는 스트림을 준비합니다.
            FileOutputStream out = new FileOutputStream(tempFile);
            Log.d(TAG, "saveBitmapToJpeg: out: "+out);
            // compress 함수를 사용해 스트림에 비트맵을 저장합니다.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.d(TAG, "saveBitmapToJpeg: 비트맵 저장");
            // 스트림 사용후 닫아줍니다.
            out.close();
        } catch (FileNotFoundException e) {
            Log.e("MyTag","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("MyTag","IOException : " + e.getMessage());
        }
    }







    private void getThumbnails(String id) {
        // 커서 생성
        Cursor cursor;
        Uri uri = MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Video.Thumbnails.DATA};

        String orderBy = MediaStore.Video.Thumbnails.VIDEO_ID;

        String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
        String[] selectionArgs = {id};

        cursor = getApplicationContext().getContentResolver().query(uri, projection, selection, selectionArgs, orderBy + " DESC");
        while (cursor.moveToNext()) {

            cursor.getString(0);
            Log.d(TAG, "getThumbnails: cursor.getString(0): "+cursor.getString(0));

        }
    }
}
