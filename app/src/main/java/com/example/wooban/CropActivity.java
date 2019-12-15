package com.example.wooban;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.INotificationSideChannel;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.isseiaoki.simplecropview.util.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CropActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CropActivity";
    private Uri imageUri;
    private CropImageView mCropView;
    private LoadCallback mCallback;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
//    private CropCallback mCropCallback;
//    private SaveCallback mSaveCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        // 버튼
        findViewById(R.id.button_rotate_left).setOnClickListener(this);
        findViewById(R.id.button_rotate_right).setOnClickListener(this);
        findViewById(R.id.button_done).setOnClickListener(this);

        // 갤러리에서 받아온 uri
        Intent intent = getIntent();
        String uri = intent.getStringExtra("uri");
        Log.d(TAG, "onCreate: uri: " + uri);

        // String으로 받아온 값을 다시 Uri 형태로 만들어줌
        imageUri = Uri.parse(uri);
        Log.d(TAG, "onCreate: imageUri: " + imageUri);

        // 바인딩
        mCropView = (CropImageView) findViewById(R.id.cropImageView);
        // 이미지뷰로 글라이드 사용
//        Glide.with(this).load(imageUri).into(mCropView);
        // 프레임을 원형으로 보이게 하고 저장할 때는 사각형으로 저장
        mCropView.setCropMode(CropImageView.CropMode.CIRCLE_SQUARE);
        // 이미지 불러옴
        mCropView.load(imageUri).execute(mCallback);

        Log.d(TAG, "onCreate: 저장");


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // 왼쪽으로 도는 화살표 버튼 누르면
            case R.id.button_rotate_left:
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
                break;

            // 오른쪽으로 도는 화살표 버튼 누르면
            case R.id.button_rotate_right:
                mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                break;

            // 확인 버튼을 누르면
            case R.id.button_done:
                Log.d(TAG, "onClick: 확인 버튼을 누름");
                // 크롭된 이미지 저장

                mCropView.crop(imageUri).execute(new CropCallback() {

                    // 성공시
                    @Override
                    public void onSuccess(Bitmap cropped) {
                        Log.d(TAG, "onSuccess: 성공");


                        // 새로운 uri 생성하여 그 uri에 크롭된 이미지를 저장
                        mCropView.save(cropped)
                                .compressFormat(mCompressFormat)
                                .execute(createSaveUri(), mSaveCallback);


                    }

                    // 실패시
                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: 실패");
                    }
                });


//                Toast.makeText(CropActivity.this, "uri 저장 성공, saveUri: "+ saveUri.toString(), Toast.LENGTH_LONG).show();
//                Log.d(TAG, "onClick: saveUri: "+saveUri.toString());
                break;

        }
        Log.i(TAG, "onClick: ");

    }


    public Uri createSaveUri() {
        Log.d(TAG, "createSaveUri: 실행");
        return createNewUri(CropActivity.this, mCompressFormat);
    }


    public static Uri createNewUri(Context context, Bitmap.CompressFormat format) {
        Log.d(TAG, "createNewUri: 실행");
        long currentTimeMillis = System.currentTimeMillis();
        Log.d(TAG, "createNewUri: currentTimeMillis: " + currentTimeMillis);
        // 날짜 객체
        Date today = new Date(currentTimeMillis);
        // 날짜 표시 형태
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String title = dateFormat.format(today);
        Log.d(TAG, "createNewUri: title: " + title);
        String dirPath = getDirPath();
        Log.d(TAG, "createNewUri: dirPath: " + dirPath);
        String fileName = "scv" + title + "." + getMimeType(format);
        Log.d(TAG, "createNewUri: fileName: " + fileName);
        String path = dirPath + "/" + fileName;
        Log.d(TAG, "createNewUri: path: " + path);
        File file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format));
        values.put(MediaStore.Images.Media.DATA, path);
        long time = currentTimeMillis / 1000;
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        Log.d(TAG, "createNewUri: time: " + time);
        Log.d(TAG, "createNewUri: file.length(): " + file.length());
        Log.d(TAG, "createNewUri: file.exists(): " + file.exists());

        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
            Log.d(TAG, "createNewUri: file.length(): " + file.length());
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Logger.i("SaveUri = " + uri);
        Log.d(TAG, "createNewUri: uri: " + uri);
        return uri;
    }

    // 파일 경로 구하는 메소드
    public static String getDirPath() {
        Log.d(TAG, "getDirPath: 실행");
        String dirPath = "";
        File imageDir = null;
        File extStorageDir = Environment.getExternalStorageDirectory();
        Log.d(TAG, "getDirPath: extStorageDir: " + extStorageDir);

        if (extStorageDir.canWrite()) {
            imageDir = new File(extStorageDir.getPath() + "/simplecropview");
            Log.d(TAG, "getDirPath: imageDir: " + imageDir);
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs();
                Log.d(TAG, "getDirPath: imageDir.mkdirs(): " + imageDir.mkdirs());
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.getPath();
                Log.d(TAG, "getDirPath: dirPath: " + dirPath);
            }
        }
        return dirPath;
    }

    // 이미지 파일 형식을 구하는 메소드
    public static String getMimeType(Bitmap.CompressFormat format) {
        Logger.i("getMimeType CompressFormat = " + format);
        switch (format) {
            case JPEG:
                return "jpeg";
            case PNG:
                return "png";
        }
        return "png";
    }

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override
        public void onSuccess(Uri outputUri) {
            Log.d(TAG, "onSuccess: SaveCallback 실행");
            Log.d(TAG, "onSuccess: outputUri: "+outputUri);

            // 인텐트에 담아 원래 액티비티(프로필 변경 액티비티)로 이동시킴
            Intent intent = new Intent();
            intent.putExtra("croppedUri", outputUri.toString());
            setResult(RESULT_OK, intent);
            finish();
        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG, "onError: SaveCallback 에러");

        }
    };
}







