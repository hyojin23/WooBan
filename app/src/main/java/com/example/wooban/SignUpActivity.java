package com.example.wooban;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";
    private EditText editId, editPassword, editName, editEmail, editEmailAuth;
    private TextView idCheckMsg, pwCheckMsg, nameCheckMsg, authCheckMsg;



    Boolean idError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

//        // EditText 객체 생성
        // 아이디
        editId = (EditText) findViewById(R.id.sign_up_id);
        // 비밀번호
        editPassword = (EditText) findViewById(R.id.sign_up_password);
        // 이름
        editName = (EditText) findViewById(R.id.sign_up_name);
        // 이메일
        editEmail = (EditText) findViewById(R.id.sign_up_email);
        // 이메일 인증번호
        editEmailAuth = (EditText) findViewById(R.id.sign_up_email_auth_edit);

        // 텍스트뷰 객체 생성
        authCheckMsg = (TextView) findViewById(R.id.sign_up_email_auth_text_view);



//        doLogin = (TextView) findViewById(R.id.do_login);
//        signUpButton = (Button) findViewById(R.id.sign_up_button);

        // 클릭 리스너
        // 회원가입 레이아웃
        findViewById(R.id.sign_up_layout).setOnClickListener(this);
        // 인증번호받기 버튼
        findViewById(R.id.sign_up_email_auth_number_get).setOnClickListener(this);
        // 확인 버튼
        findViewById(R.id.sign_up_email_auth_button).setOnClickListener(this);
        // 회원가입 버튼
        findViewById(R.id.sign_up_button).setOnClickListener(this);
        // 로그인하기 버튼
        findViewById(R.id.do_login).setOnClickListener(this);



        // 비밀번호 변화 감지
        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "텍스트 변화 전 beforeTextChanged()");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, " 텍스트 변화 중 onTextChanged()");
            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordCheck();
                Log.d(TAG, "텍스트 변화 후 afterTextChanged()");
            }
        });

        // 이름 변화 감지
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "텍스트 변화 전 beforeTextChanged()");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, " 텍스트 변화 중 onTextChanged()");
            }

            @Override
            public void afterTextChanged(Editable s) {
                nameCheck();
                Log.d(TAG, "텍스트 변화 후 afterTextChanged()");
            }
        });

        // 아이디 입력란 포커스 확인
        editId.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean gainFocus) {

                //포커스가 주어졌을 때
                if (gainFocus) {
                    //to do
                }
                //포커스를 잃었을 때
                else {
                    idCheck();
//                    //키보드 내리기
//                    InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        });


    } // onCreate 종료

    // 회원가입 버튼 누를 때 실행되는 함수
    private void userSignUp() {
        // 사용자가 입력한 아이디
        String id = editId.getText().toString().trim();
        // 사용자가 입력한 비밀번호
        String password = editPassword.getText().toString().trim();
        // 사용자가 입력한 이름
        String name = editName.getText().toString().trim();
        // 사용자가 입력한 이메일
        String email = editEmail.getText().toString().trim();
        // 사용자가 입력한 이메일 인증번호
        String authNumber = editEmailAuth.getText().toString().trim();


        // 발급된 인증번호와 사용자가 입력한 이메일 인증번호를 비교
        boolean check = emailAuthCheck(random, authNumber);


        if (id.isEmpty()) {
            editId.setError("아이디를 입력해주세요.");
            editId.requestFocus();
            return;
        }

        // 중복된 아이디나 형식에 맞지 않는 아이디일 경우
        if (idError) {
            editId.setError("다른 아이디를 사용하세요.");
            editId.requestFocus();
            return;
        }


        if (password.isEmpty()) {
            editPassword.setError("비밀번호를 입력해주세요.");
            editPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("비밀번호는 6자리 이상이어야 합니다.");
            editPassword.requestFocus();
            return;
        }

        if (name.isEmpty()) {
            editName.setError("이름을 입력해주세요.");
            editName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editEmail.setError("이메일을 입력해주세요.");
            editEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("유효한 이메일이 아닙니다.");
            editEmail.requestFocus();
            return;
        }

        if (!check) {
            editEmailAuth.setError("인증번호를 다시 입력해주세요.");
            editEmailAuth.requestFocus();
            authCheckMsg.setText("메일 인증에 실패했습니다. 인증번호를 정확히 입력해 주세요.");
            authCheckMsg.setTextColor(getResources().getColor(R.color.idWarning));
            return;
        }

        // 해쉬맵 객체 생성
        Map<String, String> params = new HashMap<>();

        // 해쉬맵에 key-value 형태로 데이터를 넣는다.
        params.put("id", id);
        params.put("password", password);
        params.put("name", name);
        params.put("email", email);

        // 프로필 이미지 uri의 절대경로를 얻어옴
//        String pathToStoredImage = getRealPathFromURIPath(imageUri, SignUpActivity.this);
//        Log.d(TAG, "userSignUp: pathToStoredImage: "+ pathToStoredImage);
//        // 프로필 이미지 파일 객체 생성
//        File file = new File(pathToStoredImage);
//        Log.d(TAG, "userSignUp: 프로필 이미지 파일 객체 생성");
//        // RequestBody 생성
//        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
//        Log.d(TAG, "userSignUp: RequestBody 생성");
//        // 실제로 서버에 보내지는 이미지 파일 객체
//        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);
//        Log.d(TAG, "userSignUp: 이미지 파일 전송,  file.getName(): "+ file.getName());


        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                // Api의 sign_up 함수에 해쉬맵 데이터를 넣어줌
                .signUp(params);


        call.enqueue(new Callback<ResponseBody>() {

            // 응답 성공시
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    String data = response.body().string();
                    Toast.makeText(SignUpActivity.this, data, Toast.LENGTH_SHORT).show();

                    // 회원가입이 완료되면 현재 액티비티 종료
                    finish();
                    // 로그인 액티비티로 이동
                    // Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    // startActivity(intent);

                    // 회원가입이 완료되면 입력된 텍스트를 지움
                    editId.setText("");
                    editPassword.setText("");
                    editName.setText("");
                    editEmail.setText("");


                    Log.d(TAG, "userSignUp() 응답 성공");
                    Log.d(TAG, data);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // 응답 실패시
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "userSignUp() 응답 실패");
            }
        });

    }

    //TODO 아이디 중복체크 함수
    // 아이디 중복체크 함수
    private void idCheck() {
        Log.d(TAG, "idCheck() 실행");
        final String id = editId.getText().toString().trim();


        Call<ResponseBody> call = RetrofitClient
                .getInstance()
                .getApi()
                .idCheck(id);

        call.enqueue(new Callback<ResponseBody>() {

            // 응답 성공시
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    String data = response.body().string();
                    // 데이터로 받아온 string을 boolean 값으로 변환
                    boolean check = Boolean.valueOf(data);


                    // 아이디 정규식. 6~16 자리 영소문자+숫자. 첫번째 문자는 반드시 영소문자
                    String idPattern = "^[a-z]{1}[a-z0-9]{5,15}$";
                    Matcher matcher = Pattern.compile(idPattern).matcher(id);


                    // 아이디 중복체크 메세지를 표시하는 텍스트뷰
                    idCheckMsg = (TextView) findViewById(R.id.id_check_text_view);
                    idCheckMsg.setVisibility(View.VISIBLE);

                    // 입력된 아이디의 길이
                    int idLength = editId.getText().toString().length();


                    // 아이디 입력란이 공백일 경우
                    if (idLength == 0) {
                        Log.d(TAG, "아이디 공백");
                        idCheckMsg.setVisibility(View.GONE);


                        // 중복된 아이디일 경우 (check == true 일 경우)
                    } else if (check) {
                        Log.d(TAG, "아이디 중복");
//                        Toast.makeText(SignUpActivity.this, data, Toast.LENGTH_SHORT).show();
                        idCheckMsg.setText("이미 존재하는 아이디입니다.");
                        idCheckMsg.setTextColor(getResources().getColor(R.color.idWarning));
                        idError = true;
//                        Toast.makeText(SignUpActivity.this, "중복된 아이디", Toast.LENGTH_SHORT).show();


                        // 중복이 아니지만 정규식을 만족하지 않을 경우
                    } else if (!matcher.matches()) {
                        Log.d(TAG, "아이디 형식 오류");
//                        Toast.makeText(SignUpActivity.this, data, Toast.LENGTH_SHORT).show();
                        idCheckMsg.setText("아이디는 6~16 자리 영소문자, 숫자 조합만 가능합니다.");
                        idCheckMsg.setTextColor(getResources().getColor(R.color.idMessage));
                        idError = true;


//                        signUpButton = findViewById(R.id.sign_up_button);
//                        // 중복된 아이디를 입력한 뒤 회원가입 버튼을 누를 경우
//                        signUpButton.setOnClickListener(new Button.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                editId.setError("다른 아이디를 사용하세요.");
//                                editId.requestFocus();
//                                return;
//                            }
//                        });


                    } else {
                        Log.d(TAG, "아이디 사용가능");
//                        Toast.makeText(SignUpActivity.this, data, Toast.LENGTH_SHORT).show();
                        idCheckMsg.setText("사용 가능한 아이디입니다.");
                        idCheckMsg.setTextColor(getResources().getColor(R.color.idOk));
                        idError = false;
//                        Toast.makeText(SignUpActivity.this, "사용 가능", Toast.LENGTH_SHORT).show();
                    }
//                    Toast.makeText(SignUpActivity.this, data, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "idCheck() 응답 성공");


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            // 응답 실패시
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "idCheck() 응답 실패");
            }
        });

    }


    // 비밀번호 체크 함수
    public void passwordCheck() {

        String password = editPassword.getText().toString().trim();

        // 비밀번호 정규식. 6~16 자리 영문+숫자+특수문자
        String pwPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{6,16}$";
        // 비밀번호가 정규식과 일치하는지 확인
        Matcher matcher = Pattern.compile(pwPattern).matcher(password);


        // 비밀번호에 대한 메세지를 표시하는 텍스트뷰
        pwCheckMsg = (TextView) findViewById(R.id.pw_check_text_view);
        // 눈에 보이게 함
        pwCheckMsg.setVisibility(View.VISIBLE);


        // 정규표현식 형식에 벗어나면
        if (!matcher.matches()) {
            pwCheckMsg.setText("비밀번호는 6~16 자리 숫자, 문자, 특수문자 조합으로 이루어져야 합니다.");
            // 색상 지정
            pwCheckMsg.setTextColor(getResources().getColor(R.color.idMessage));

        } else {
            pwCheckMsg.setText("사용 가능한 비밀번호입니다.");
            pwCheckMsg.setTextColor(getResources().getColor(R.color.idOk));
        }
    }

    // 이름 체크 함수
    public void nameCheck() {

        String name = editName.getText().toString().trim();

        // 비밀번호 정규식. 6~16 자리 영문+숫자+특수문자
        String namePattern = "^[가-힣a-zA-Z]{2,16}$";
        // 비밀번호가 정규식과 일치하는지 확인
        Matcher matcher = Pattern.compile(namePattern).matcher(name);


        // 비밀번호에 대한 메세지를 표시하는 텍스트뷰
        nameCheckMsg = (TextView) findViewById(R.id.name_check_text_view);
        // 눈에 보이게 함
        nameCheckMsg.setVisibility(View.VISIBLE);


        // 정규표현식 형식에 벗어나면
        if (!matcher.matches()) {
            nameCheckMsg.setText("2~16 자리 한글, 영문만 사용 가능합니다.");
            // 색상 지정
            nameCheckMsg.setTextColor(getResources().getColor(R.color.idMessage));

        } else {
            nameCheckMsg.setText("사용 가능한 이름입니다.");
            nameCheckMsg.setTextColor(getResources().getColor(R.color.idOk));
        }


    }

    // 랜덤한 문자열 생성
    StringBuffer buffer = randomString();
    String random = buffer.toString();

    // 클릭시 반응
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // 회원가입 레이아웃 화면 전체를 누르면
            case R.id.sign_up_layout:
                keyBoradDown();
                break;

            // 인증번호받기 버튼을 누르면
            case R.id.sign_up_email_auth_number_get:

                // 입력한 이메일
                String email = editEmail.getText().toString().trim();

                // 이메일 유효성 체크
                if (email.isEmpty()) {
                    editEmail.setError("이메일을 입력해주세요.");
                    editEmail.requestFocus();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editEmail.setError("유효한 이메일이 아닙니다.");
                    editEmail.requestFocus();
                    return;
                }

                // asynctask 실행되어 인증메일 전송
                MyAsyncTask asyncTask = new MyAsyncTask();
                // 랜덤한 문자열과 입력한 이메일을 인자로 받아 실행
                asyncTask.execute(random, email);

                // 디버깅시 인증번호 확인용 토스트
                // Toast.makeText(SignUpActivity.this, random, Toast.LENGTH_SHORT).show();

                Log.d(TAG, "인증번호받기 버튼");
                dialogShow("메일로 인증번호가 전송되었습니다.");
                break;


            // 인증 버튼을 누르면
            case R.id.sign_up_email_auth_button:
                Log.d(TAG, "인증 버튼");

                // 사용자가 입력한 이메일 인증번호
                String authNumber = editEmailAuth.getText().toString().trim();

                // 발급된 인증번호와 사용자가 입력한 이메일 인증번호를 비교
                boolean check = emailAuthCheck(random, authNumber);
//                Log.d(TAG, random);
//                Log.d(TAG, authNumber);

                // 인증번호 입력란 밑에 있는 텍스트뷰가 나타남
                authCheckMsg.setVisibility(View.VISIBLE);

                if (check) {
                    dialogShow("메일 인증이 완료되었습니다.");
                    authCheckMsg.setText("메일 인증이 완료되었습니다.");
                    authCheckMsg.setTextColor(getResources().getColor(R.color.idOk));
                    // 키보드 내리기
                    keyBoradDown();
                } else {

                    authCheckMsg.setText("메일 인증에 실패했습니다. 인증번호를 정확히 입력해 주세요.");
                    authCheckMsg.setTextColor(getResources().getColor(R.color.idWarning));
                    keyBoradDown();
                }


                break;


            // 회원가입 버튼을 누르면
            case R.id.sign_up_button:
                Log.d(TAG, "회원가입 버튼");
                userSignUp();
                break;


            // 이미 아이디가 있나요? 로그인하기 버튼을 누르면
            case R.id.do_login:
                Log.d(TAG, "이미 아이디가 있나요? 로그인하기 버튼");
                finish();
                break;

        }
    }

    //
    public void dialogShow(final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("안내");
        builder.setMessage(message);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, message + " 확인 선택");
                    }
                });
        builder.show();
    }


    /* 영문 대소문자, 숫자를 섞은 문자열 생성
       영문자는 int 타입의 숫자를 char 타입으로 캐스팅 하면 아스키코드 문자로 변환됨*/
    public StringBuffer randomString() {
        // StringBuffer 객체 생성
        StringBuffer temp = new StringBuffer();
        // 랜덤수 생성
        Random random = new Random();
        for (int i = 0; i < 6; i++) {

            // 0~2까지의 랜덤한 숫자
            int rIndex = random.nextInt(3);
            switch (rIndex) {
                case 0:
                    // 영소문자 a-z (아스키코드 97~122)
                    temp.append((char) ((random.nextInt(26)) + 97));
                    break;
                case 1:
                    // 영대문자 A-Z (아스키코드 65~122)
                    temp.append((char) ((random.nextInt(26)) + 65));
                    break;
                case 2:
                    // 숫자 0-9
                    temp.append((random.nextInt(10)));
                    break;
            }
        }
        return temp;

    }

    // 클라이언트가 입력한 값과 실제 메일로 보낸 인증번호를 비교
    public boolean emailAuthCheck(String inputString, String authNumber) {
        if (inputString.equals(authNumber)) {
            return true;
        } else {
            return false;
        }

    }

    // 키보드 내리기
    public void keyBoradDown() {
        // 키보드 내리기
        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        immhide.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }




}


//public class SignUpActivity extends AppCompatActivity {
//
//    private static String IP_ADDRESS = "54.180.123.194";
//    private static String TAG = "phptest";
//
//    private EditText editName, editEmail, editPassword,editId;
//    private EditText mEditTextCountry;
//    private TextView mTextViewResult;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
//
//        // EditText 객체 생성
//        editName = (EditText)findViewById(R.id.sign_up_name);
//        editEmail = (EditText)findViewById(R.id.sign_up_email);
//        editPassword = (EditText)findViewById(R.id.sign_up_password);
//        editId = (EditText)findViewById(R.id.sign_up_phone);
//
////        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());
//
//
//        Button buttonSignUp = (Button)findViewById(R.id.sign_up_button);
//        buttonSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String name = editName.getText().toString();
//                String email = editEmail.getText().toString();
//                String password = editPassword.getText().toString();
//                String id = editId.getText().toString();
//
//                // InsertData
//                InsertData task = new InsertData();
//                //
//                task.execute("http://" + IP_ADDRESS + "/android/sign_up.php", name, email, password, id);
//
//
//                // 회원가입 버튼 누른 후 EditText를 빈칸으로 만듬
//                editName.setText("");
//                editEmail.setText("");
//                editPassword.setText("");
//                editId.setText("");
//
//            }
//        });
//
//    }
//
//
//
//    class InsertData extends AsyncTask<String, Void, String>{
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            progressDialog = ProgressDialog.show(SignUpActivity.this,
//                    "Please Wait", null, true, true);
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            progressDialog.dismiss();
////            mTextViewResult.setText(result);
//            Log.d(TAG, "POST response  - " + result);
//        }
//
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            String name = (String)params[1];
//            String email = (String)params[2];
//            String password = (String)params[3];
//            String id = (String)params[4];
//
//            String serverURL = (String)params[0];
//            // "" 안의 값이 php코드에서 키값으로 사용된다.
//            String postParameters = "name=" + name + "&email=" + email + "&password=" + password + "&id=" + id;
//
//
//            try {
//
//                URL url = new URL(serverURL);
//                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//
//
//                httpURLConnection.setReadTimeout(5000);
//                httpURLConnection.setConnectTimeout(5000);
//                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.connect();
//
//
//                OutputStream outputStream = httpURLConnection.getOutputStream();
//                outputStream.write(postParameters.getBytes("UTF-8"));
//                outputStream.flush();
//                outputStream.close();
//
//
//                int responseStatusCode = httpURLConnection.getResponseCode();
//                Log.d(TAG, "POST response code - " + responseStatusCode);
//
//                InputStream inputStream;
//                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
//                    inputStream = httpURLConnection.getInputStream();
//                }
//                else{
//                    inputStream = httpURLConnection.getErrorStream();
//                }
//
//
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder sb = new StringBuilder();
//                String line = null;
//
//                while((line = bufferedReader.readLine()) != null){
//                    sb.append(line);
//                }
//
//
//                bufferedReader.close();
//
//
//                return sb.toString();
//
//
//            } catch (Exception e) {
//
//                Log.d(TAG, "InsertData: Error ", e);
//
//                return new String("Error: " + e.getMessage());
//            }
//
//        }
//    }
//
//
//}
