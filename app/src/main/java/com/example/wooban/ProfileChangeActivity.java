package com.example.wooban;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.wooban.RetrofitClient.getClient;


public class ProfileChangeActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final String TAG = "ProfileChangeActivity";
    private ImageView profileImage;
    private EditText editName;
    private TextView textName;
    private Uri imageUri, newUri;
    private static final String SERVER_PATH = "http://54.180.104.190/android/";
    private CropImageView mCropView;
    private int REQEST_CROP = 200;
    private static final int READ_REQUEST_CODE = 500;
    private String select_animal;
    private static final String TAG_TEXT = "text";
    private static final String TAG_IMAGE = "image";
    List<Map<String, Object>> dialogItemList;
    ProgressDialog progressDialog;

    int[] image = {
            R.drawable.dog_face_icon,
            R.drawable.cat_face_icon,
            R.drawable.fox_face_icon,
            R.drawable.rabbit_face_icon,
            R.drawable.monkey_face_icon,
            R.drawable.lion_face_icon,
            R.drawable.panda_face_icon,
            R.drawable.bear_face_icon,
            R.drawable.hedgehog_face_icon,
            R.drawable.wolf_face_icon,

    };

    String[] text = {"개", "고양이", "여우", "토끼", "원숭이", "사자", "판다", "곰", "고슴도치", "늑대"};
    String[] animal = {"dog", "cat", "fox", "rabbit", "monkey", "lion", "panda", "bear", "hedgehog", "wolf"};

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Log.d(TAG, "onPermissionGranted: TED 퍼미션 권한 허가됨");
            Toast.makeText(ProfileChangeActivity.this, "권한 허가", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Log.d(TAG, "onPermissionGranted: TED 퍼미션 권한 거부됨");
            Toast.makeText(ProfileChangeActivity.this, "권한 거부\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        // 외부 저장소에 대한 권한 요청
        checkPermission();

//        TedPermission.with(this)
//                .setPermissionListener(permissionlistener)
//                .setRationaleMessage("프로필 이미지 변경을 위해서는 외부 저장소에 대한 접근 권한이 필요합니다.")
//                .setDeniedMessage("왜 거부하셨어요...\n하지만 [설정] > [권한] 에서 권한을 허용할 수 있어요.")
//                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
//                .check();


        // 받은 프로필 이미지 url
        Intent intent = getIntent();
        select_animal = intent.getStringExtra("profile_image_url");
        Log.d(TAG, "onCreate: intent.getStringExtra(\"profile_image_url\"): " + intent.getStringExtra("profile_image_url"));

        // 프로필 이미지
        profileImage = (ImageView) findViewById(R.id.profile_change_user_image);

        // 이름
        textName = (TextView) findViewById(R.id.profile_change_name_text);
        textName.setText("이름: ");

        // 이름 적는 공간
        editName = (EditText) findViewById(R.id.profile_change_name_edit);


        // 프로필 이미지 수정 버튼
        findViewById(R.id.profile_change_image_button).setOnClickListener(this);
        // 수정하기 버튼
        findViewById(R.id.profile_change_button).setOnClickListener(this);

        // 아이템 리스트 객체
        dialogItemList = new ArrayList<>();

        // ex) itemMap: {text=개, image=2131099745}
        //     dialogItemList: [{text=개, image=2131099745}]
        // 이미지와 텍스트를 매칭할 수 있게 이미지 이름과 텍스트를 만든다.
        for (int i = 0; i < image.length; i++) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(TAG_IMAGE, image[i]);
            itemMap.put(TAG_TEXT, text[i]);
            Log.d(TAG, "onCreate: itemMap: " + itemMap);

            dialogItemList.add(itemMap);
            Log.d(TAG, "onCreate: dialogItemList: " + dialogItemList);
        }

        // 데이터 불러오기
        getData();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: 실행");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, ProfileChangeActivity.this);
    }




    @Override

    public void onClick(View v) {
        switch (v.getId()) {

            // 프로필 이미지 수정 버튼 클릭시
            case R.id.profile_change_image_button:
                profileDialogShow();
                break;

            // 수정하기 버튼 클릭시
            case R.id.profile_change_button:
                profileChange(newUri);
//                defaultImageChange();
                // 프로그레스 다이얼로그 실행
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("잠시만 기다려주세요...");
                progressDialog.setCancelable(true);
                progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
                progressDialog.show();

                break;
        }
    }


    // 수정하기 버튼 누르면 실행되는 메소드
    private void profileChange(Uri uri) {
        Log.d(TAG, "profileChange: 프로필 편집 액티비티에서 수정하기 버튼을 누름");
        // 입력한 이름
        String name = editName.getText().toString().trim();
        Log.d(TAG, "profileChange: 입력한 이름 name: " + name);

        // uri에 content, default라는 문자가 포함되어 있지 않으면
        // 즉 이미지가 이미 서버에 업로드 되어 http://54.180.123.194/uploads/scv20190915_011759.jpeg 형태의 uri이면
        if (!uri.toString().contains("content") && !uri.toString().contains("default")) {
            Log.d(TAG, "profileChange: uri: " + uri);
            Log.d(TAG, "profileChange: ");
            // 해쉬맵 객체 생성
            Map<String, String> params = new HashMap<>();

            // 해쉬맵에 key-value 형태로 데이터를 넣는다.
            params.put("name", name);

            Call<JsonObject> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    // 이미지 파일과 토큰, 이름을 넣어 전송
                    .profileNameChange(getSharedToken(), params);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d(TAG, "onResponse: 응답 성공");
                    progressDialog.dismiss();
                    Toast.makeText(ProfileChangeActivity.this, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "onFailure: 응답 실패");
                    Log.d(TAG, "onFailure: 응답 실패 Error message: " + t.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(ProfileChangeActivity.this, "프로필 수정 오류 발생", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            // uri에 default라는 문자가 포함되어 있으면 (default가 포함되었다는 것은 기본 아이콘 이미지를 바꾸지 않았거나
            // 최종적으로 기본 아이콘 이미지를 선택했다는 것임
        } else if (uri.toString().contains("default")) {
            Log.d(TAG, "profileChange: uri에 default라는 문자가 포함되어 있는 경우");
            defaultImageChange();


            // 서버에 올린적이 없는 사진인 경우
        } else {

            // 프로필 이미지 uri의 절대경로를 얻어옴
            String pathToStoredImage = getRealPathFromURIPath(uri, ProfileChangeActivity.this);
            Log.d(TAG, "userSignUp: pathToStoredImage: " + pathToStoredImage);
            // 프로필 이미지 파일 객체 생성
            File file = new File(pathToStoredImage);
            Log.d(TAG, "userSignUp: 프로필 이미지 파일 객체 생성, file: " + file);
            // RequestBody 생성
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
            Log.d(TAG, "userSignUp: RequestBody 생성");
            // 실제로 서버에 보내지는 이미지 파일 객체
            MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
            Log.d(TAG, "userSignUp: 이미지 파일 전송,  file.getName(): " + file.getName());


//        RequestBody requestBody = new FormBody.Builder().add("name", name).build();
            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), name);
            Log.d(TAG, "profileChange: requestBody: " + requestBody);

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

//        Call<ResultObject> call = RetrofitClient
//                .getInstance()
//                .getApi()
//                // 이미지 파일과 토큰, 이름을 넣어 전송
//                .uploadImage(body, getSharedToken());

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_PATH)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Api api = retrofit.create(Api.class);
            Call<ResponseBody> call = api.uploadImage(part, getSharedToken(), requestBody);


            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "onResponse: 응답 성공");
                    ResponseBody result = response.body();
                    Log.d(TAG, "onResponse: result: " + result);
                    progressDialog.dismiss();
                    Toast.makeText(ProfileChangeActivity.this, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show();
                    finish();


                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, "onFailure: 응답 실패");
                    Log.d(TAG, "onFailure: 응답 실패 Error message: " + t.getMessage());
                    progressDialog.dismiss();
                    Toast.makeText(ProfileChangeActivity.this, "프로필 수정 오류 발생 " + t.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }
            });

        }
    }


    // 프로필 이미지 수정 버튼 누르면 나오는 다이얼로그
    public void profileDialogShow() {
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("카메라로 사진 찍기");
        ListItems.add("갤러리에서 이미지 가져오기");
        ListItems.add("기본 아이콘");
        // 리스트를 배열로 바꿈
        final CharSequence[] items = ListItems.toArray(new String[ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진 선택");


        // 다이얼로그에서 각 아이템이 선택될 때
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {

                switch (position) {
                    // 카메라로 사진 찍기를 선택한 경우
                    case 0:
                        sendTakePhotoIntent();
                        Log.d(TAG, "onClick: " + items[0].toString());
                        break;
                    // 갤러리에서 사진 불러오기를 선택한 경우
                    case 1:
                        takeImageFromGallery();
                        Log.d(TAG, "onClick: " + items[1].toString());
                        break;
                    // 기본 아이콘을 선택한 경우
                    case 2:
                        showAlertDialog();

                        Log.d(TAG, "onClick: " + items[2].toString());
                        break;

                }


                String selectedText = items[position].toString(); // 선택된 텍스트는 배열의 각 포지션에 위치한 값이다.
//                Toast.makeText(ProfileChangeActivity.this, selectedText, Toast.LENGTH_SHORT).show(); // 선택된 텍스트를 토스트로 띄운다.
            }
        });
        builder.show();
    }

    // 갤러리를 실행해 사진을 불러오는 메소드
    private void takeImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 2);
    }


    // 이미지 파일을 생성하는 메소드(이미지가 저장될 파일을 만듬)
    private File createImageFile() throws IOException {
        // 현재 날짜와 시간
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        // 이미지 파일명
        String imageFileName = "TEST_" + timeStamp + "_";
        // 파일 저장 경로(외부저장소)
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        Log.d(TAG, "createImageFile() imageFileName: " + imageFileName);
        Log.d(TAG, "createImageFile() storageDir: " + storageDir);
        Log.d(TAG, "createImageFile() image: " + image);
//        private String imageFilePath;
//        imageFilePath = image.getAbsolutePath();
        return image;
    }

    // 인텐트를 이용하여 카메라로 사진을 찍으라는 요청을 보냄
    private void sendTakePhotoIntent() {
        Log.d(TAG, "카메라 앱을 실행시킴");
        // 카메라 앱 실행
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "sendTakePhotoIntent: 파일 생성");

            } catch (IOException ex) {
                Log.d(TAG, "sendTakePhotoIntent: 예외 발생");
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                Log.d(TAG, "sendTakePhotoIntent: photoFile: " + photoFile);
                imageUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    // intent로 불러온 이미지를 이미지뷰에 띄운다.(이미지 회전 후 onActivityResult)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 카메라 앱을 실행 후 종료한 상황에서 동작
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d(TAG, "카메라 앱을 실행 후 종료한 상황");

//        // 이미지 회전 없이 사진으로 찍은 이미지를 이미지뷰에 세팅(이미지가 돌아가서 나옴)
//        ((ImageView)findViewById(R.id.write_image)).setImageURI(photoUriU);
            // ImageView imageView = (ImageView)findViewById(R.id.write_image);
            Log.d(TAG, "카메라에서 얻은 uri: " + imageUri);
            String stringUri = imageUri.toString();
            Intent intent = new Intent(ProfileChangeActivity.this, CropActivity.class);
            Log.d(TAG, "onActivityResult: 인텐트에 uri를 넣어 CropActivity 실행");
            intent.putExtra("uri", stringUri);
            startActivityForResult(intent, REQEST_CROP);

//            Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(profileImage);


//        // 이미지 회전시킬때 사용
//        Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
//        ExifInterface exif = null;
//
//        try {
//            exif = new ExifInterface(imageFilePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        int exifOrientation;
//        int exifDegree;
//
//        if (exif != null) {
//            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//            exifDegree = exifOrientationToDegrees(exifOrientation);
//        } else {
//            exifDegree = 0;
//        }
//
//        ((ImageView) findViewById(R.id.write_image)).setImageBitmap(rotate(bitmap, exifDegree));

            //갤러리 앱을 실행 후 종료한 상황에서 동작
        } else if (requestCode == 2 && resultCode == RESULT_OK && data != null && data.getData() != null) {


            Log.d(TAG, "갤러리 앱을 실행 후 종료한 상황");
            // uri를 얻어옴
            imageUri = data.getData();
            Log.d(TAG, "갤러리에서 얻은 uri :" + imageUri);

            // uri를 String 형태로 바꿔 크롭 액티비티로 전달
            String stringUri = imageUri.toString();
            Intent intent = new Intent(ProfileChangeActivity.this, CropActivity.class);
            Log.d(TAG, "onActivityResult: 인텐트에 uri를 넣어 CropActivity 실행");
            intent.putExtra("uri", stringUri);
            startActivityForResult(intent, REQEST_CROP);


//            mCropView = (CropImageView) findViewById(R.id.cropImageView);
//
//            mCropView.load(imageUri);
//
////
//            mCropView.crop(imageUri)
//                    .execute(new CropCallback() {
//                        @Override
//                        public void onSuccess(Bitmap cropped) {
//                            mCropView.save(cropped)
//                                    .execute(saveUri, mSaveCallback);
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//                        }
//                    });


//            Glide.with(this).load(imageUri).apply(RequestOptions.circleCropTransform()).into(profileImage);

            //((ImageView)findViewById(R.id.write_image)).setImageURI(selectedImageUri);
            //imageView.setImageURI(selectedImageUri);
        } else if (requestCode == REQEST_CROP && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: Crop 액티비티 종료 후 실행");
            String croppedUri = data.getStringExtra("croppedUri");
            Log.d(TAG, "onActivityResult: croppedUri: " + croppedUri);
            newUri = Uri.parse(croppedUri);
            Log.d(TAG, "onActivityResult: 이미지뷰에 crop된 이미지를 넣어 보여줌");
            Glide.with(this).load(newUri).apply(RequestOptions.circleCropTransform()).into(profileImage);

        }
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

    // jwt 토큰을 가져오는 함수
    private String getSharedToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        // jwt라는 key에 저장된 값이 있는지 확인. 값이 없으면 null 반환
        String token = sharedPreferences.getString("access_token", null);
        return token;
    }

    // 유저 정보를 불러와 프로필 편집 액티비티에서 유저의 프로필 사진과 이름이 나타나게 한다.
    private void getData() {
        Log.d(TAG, "getData: getData() 실행");

        // 레트로핏 객체
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .getMainData(getSharedToken());

        call.enqueue(new Callback<JsonObject>() {

            // 응답 성공시
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "응답 성공");
                // 서버에서 받아온 데이터
                JsonObject data = response.body();

                // 토큰으로 정상적으로 로그인 된 경우
                if (data != null) {
                    ImageView image = (ImageView) findViewById(R.id.navigation_header_profile_image);
                    Log.d(TAG, "onResponse: 이미지뷰 객체 생성");
//                    Glide.with(MainActivity.this).load(R.drawable.dog_face_icon).apply(RequestOptions.circleCropTransform()).into(image);

                    // 이름
                    String name = data.get("name").getAsString();
                    // 프로필 이미지 url
                    String profile_image_url = null;

                    // 프로필 이미지 url값이 null이 아니면

                    profile_image_url = data.get("profile_image").getAsString();
                    Log.d(TAG, "onResponse: profile_image_url : " + profile_image_url);

                    // 이미지를 변경하지 않고 수정하기 버튼을 눌렀을 때 NullPointerException이 뜨지 않게 하기 위해 사용
                    newUri = Uri.parse(profile_image_url);
                    Log.d(TAG, "onResponse: newUri: " + newUri);

                    /* 프로필 이미지가 default이라는 단어를 포함하면 기본 이미지가 나타나고,
                     그렇지 않으면 본인이 선택한 이미지가 나타남 */
                    if (profile_image_url.contains("default")) {
                        Log.d(TAG, "프로필 이미지가 default. onResponse: profile_image_url: " + profile_image_url);
                        // 기본 이미지 중 선택한 이미지를 이미지뷰에 넣어 보여준다.
                        DefaultProfileImage defaultImage = new DefaultProfileImage();
                        defaultImage.changeToAnimal(ProfileChangeActivity.this, profile_image_url, profileImage);
                    } else {
                        Log.d(TAG, "프로필 이미지가 default 아님. onResponse: profile_image_url: " + profile_image_url);
                        Glide.with(ProfileChangeActivity.this).load(profile_image_url).apply(RequestOptions.circleCropTransform()).into(profileImage);

                    }
                    Log.d(TAG, "onResponse: name: " + name);

                    // 이름 변경 EditText에 이름이 나타나게 함
                    editName.setText(name);
                    String message = data.get("message").getAsString();
                    Log.d(TAG, "message: " + message);


                    // 토큰이 만료되어 로그인 되지 않은 경우(서버에서 받은 데이터가 null이 됨)
                } else {
                    Toast.makeText(ProfileChangeActivity.this, "로그아웃 되었습니다. 다시 로그인해 주세요", Toast.LENGTH_SHORT).show();

                }

            }

            // 응답 실패시 (토큰이 없는 상황, 로그아웃 상태)
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 응답 실패");

            }
        });


    }

    // 기본 아이콘 목록을 보여주는 다이얼로그
    private void showAlertDialog() {
        Log.d(TAG, "showAlertDialog: 실행");
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileChangeActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        // 다이얼로그 레이아웃 생성
        View view = inflater.inflate(R.layout.alert_dialog, null);
        // 레이아웃을 다이얼로그에 넣음
        builder.setView(view);

        final ListView listview = (ListView) view.findViewById(R.id.listview_alterdialog_list);
        final AlertDialog dialog = builder.create();

        // Context, 연동하고자 하는 데이터, 레이아웃, HashMap에서 데이터 추출을 위한 키값, 추출된 데이터를 출력하기 위한
        // 파일 내의 뷰 id
        SimpleAdapter simpleAdapter = new SimpleAdapter(ProfileChangeActivity.this, dialogItemList,
                R.layout.alert_dialog_row,
                new String[]{TAG_IMAGE, TAG_TEXT},
                new int[]{R.id.alertDialogItemImageView, R.id.alertDialogItemTextView});

        listview.setAdapter(simpleAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            // 아이템을 클릭할 경우
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(ProfileChangeActivity.this, text[position] + " 선택", Toast.LENGTH_SHORT).show();
                select_animal = "default_" + animal[position];
                Log.d(TAG, "onItemClick: select_animal: " + select_animal);

                // 선택한 동물을 이미지뷰에 보여준다.
                DefaultProfileImage defaultImage = new DefaultProfileImage();
                defaultImage.changeToAnimal(ProfileChangeActivity.this, select_animal, profileImage);
                dialog.dismiss();

                // ex) newUri = default_dog
                newUri = Uri.parse(select_animal);
                Log.d(TAG, "onItemClick: newUri: " + newUri);
            }

        });


        // 뒤로가기 버튼으로 다이얼로그 취소 가능
        dialog.setCancelable(true);
        // 다이얼로그 배경을 투명하게 하여 모서리가 둥근 카드뷰 모양만 보이게 함
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    // 기본 이미지 아이콘 변경 메소드
    private void defaultImageChange() {
        // 입력한 이름
        String name = editName.getText().toString().trim();

        // 레트로핏 객체
        Log.d(TAG, "defaultImageChange: 실행");
        // 해쉬맵 객체 생성
        Map<String, String> params = new HashMap<>();

        // 해쉬맵에 key-value 형태로 데이터를 넣는다.
        params.put("default_image", newUri.toString());
        params.put("name", name);

        Log.d(TAG, "defaultImageChange: 레트로핏 실행");
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .profileDefaultChange(getSharedToken(), params);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "defaultImageChange() onResponse: 응답 성공");
                progressDialog.dismiss();
                Toast.makeText(ProfileChangeActivity.this, "프로필이 수정되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "defaultImageChange() onFailure: 응답 실패");
                progressDialog.dismiss();
                Toast.makeText(ProfileChangeActivity.this, "프로필 수정 오류 발생", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }

    // 외부 저장소에 대한 권한 체크
    public void checkPermission() {

        // 권한이 있는지 확인한 후 권한이 있을 경우
        if (EasyPermissions.hasPermissions(ProfileChangeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Log.d(TAG, "checkPermission: 외부 저장소에 대한 접근 권한이 있음");

            // 권한이 없을 경우 권한 요청
        } else {
            Log.d(TAG, "checkPermission: 외부 저장소에 대한 접근 권한이 없음");
            EasyPermissions.requestPermissions(ProfileChangeActivity.this, getString(R.string.read_file), READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    // 권한이 허용되었을 때
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted: 권한 허용됨");

    }

    // 권한이 허용되지 않았을 때
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied: 권한 거부됨");
        Log.d(TAG, "onPermissionsDenied: 거부 코드: " + requestCode);
        Toast.makeText(this, "원활한 앱 사용을 위해 권한 동의가 필요합니다. \n [설정] > [권한] 에서 권한을 허용할 수 있습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }



}




