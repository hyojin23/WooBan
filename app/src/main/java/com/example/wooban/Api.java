package com.example.wooban;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface Api {


    // json 형태로 데이터 보냄
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("token_sign_up.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<ResponseBody> signUp(
            @Body Map<String, String> params
    );


    @FormUrlEncoded
    // POST 방식
    @POST("id_check.php")
        // @Field("name")으로 보내면 php에서 $_POST['name'] 으로 받는다.
    Call<ResponseBody> idCheck(
            @Field("id") String id
    );


    // json 형태로 데이터 보냄
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("token_login.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> login(
            @Body Map<String, String> params
    );


    // main 화면에서 통신
    @Headers({
            "Content-Type:application/json",
    })
    // POST 방식
    @POST("main.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> getMainData(
            @Header("Authorization") String token
    );


    // 프로필 편집 액티비티에서 이미지와 텍스트를 바꿀 때
    @Multipart
    @POST("profile_image_upload.php")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part image,
            @Header("Authorization") String token,
            @Part("name") RequestBody name

    );

    // 프로필 편집 액티비티에서 텍스트만 바꿀 때
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("profile_text_change.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> profileNameChange(
            @Header("Authorization") String token,
            @Body Map<String, String> params
    );

    // 토큰이 만료되었는지 확인하는 요청
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("check_token.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> checkToken(
            @Header("Authorization") String token
//            @Body Map<String, String> params
    );

    // 기본 아이콘을 변경하는 요청
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("profile_default_change.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> profileDefaultChange(
            @Header("Authorization") String token,
            @Body Map<String, String> params
    );


    // 영상을 업로드하고 정보를 db에 저장하는 요청
    // POST 방식

    @Multipart
    @POST("video_upload.php")
        // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> videoUpload(
            @Header("Authorization") String token,
            @Part MultipartBody.Part video,
            @Part MultipartBody.Part image,
            @PartMap() Map<String, RequestBody> partMap

    );

    // db에 저장된 정보를 메인화면에 불러오는 요청
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("fetch_video_info.php")
    Call<JsonObject> fetchVideoInfo(
            @Header("Authorization") String token

    );

    // db에 저장된 정보를 다음 동영상 리사이클러뷰에 불러오는 요청
    @FormUrlEncoded
    // POST 방식
    @POST("fetch_next_video.php")
    Call<JsonObject> fetchNextVideoInfo(
            @Header("Authorization") String token,
            @Field("video_index") int video_index

    );

    // 네이버 파파고 api 요청

    @FormUrlEncoded
    // POST 방식
    @POST("https://openapi.naver.com/v1/papago/n2mt")
    Call<JsonObject> papagoNmt(
            @Header("X-Naver-Client-Id") String clientId,
            @Header("X-Naver-Client-Secret") String clientSecret,
            @FieldMap() HashMap<String, String> map
    );

    // 댓글 작성시 댓글에 대한 정보 전송

    @FormUrlEncoded
    // POST 방식
    @POST("reply_post.php")
        // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> reply_post(
            @Header("Authorization") String token,
            @FieldMap() HashMap<String, String> map,
            @Field("video_index") int video_index
    );

    // db에 저장된 댓글 정보를 불러옴
    @FormUrlEncoded
    // POST 방식
    @POST("fetch_reply_info.php")
    Call<JsonObject> fetchReplyInfo(
            @Header("Authorization") String token,
            @Field("video_index") int video_index,
            @Field("reply_index") int reply_index,
            @Field("client_array_size") int array_size,
            @Field("is_paging") int is_paging

    );

    // 인덱스 번호를 기준으로 db에 저장된 댓글을 삭제함
    @FormUrlEncoded
    // POST 방식
    @POST("reply_delete.php")
    Call<JsonObject> replyDelete(
            @Header("Authorization") String token,
            @Field("video_index") int video_index,
            @Field("reply_index") int reply_index

    );

    // 인덱스 번호를기준으로 db에 저장된 댓글을 수정함
    @FormUrlEncoded
    // POST 방식
    @POST("reply_modify.php")
    Call<JsonObject> replyModify(
            @Header("Authorization") String token,
            @Field("video_index") int video_index,
            @Field("reply_index") int reply_index,
            @Field("new_reply_content") String new_reply_content
    );

    // 해당 영상의 전체 댓글 수를 구한다.
    @FormUrlEncoded
    // POST 방식
    @POST("get_reply_count.php")
    Call<JsonObject> getReplyCount(
            @Header("Authorization") String token,
            @Field("video_index") int video_index
    );

    // 인덱스 번호를 기준으로 영상 정보를 불러온다.
    @FormUrlEncoded
    // POST 방식
    @POST("fetch_video_info_by_idx.php")
    Call<JsonObject> fetchVideoInfoByIdx(
            @Header("Authorization") String token,
            @Field("video_index") int video_index
    );

    // 방 정보를 db에 저장하는 요청을 할 때
    @Multipart
    @POST("chat_room_info_upload.php")
    Call<JsonObject> uploadRoomInfo(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image,
            @PartMap() Map<String, RequestBody> partMap

    );

    // 방 정보를 불러온다
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("fetch_chat_room_info.php")
    Call<JsonObject> fetchChatRoomInfo(
            @Header("Authorization") String token
    );


    // 토큰에 있는 나의 id를 기준으로 해당 방 하나의 정보를 불러온다
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("fetch_one_chat_room_info.php")
    Call<JsonObject> fetchOneChatRoomInfo(
            @Header("Authorization") String token

    );

    // 인덱스 번호를 기준으로 영상 정보를 불러온다.
//    @FormUrlEncoded
//    // POST 방식
//    @POST("add_chat_room_idx_to_user.php")
//    Call<JsonObject> addChatRoomIdxToUser(
//            @Header("Authorization") String token,
//            @Field("chat_room_idx") String chat_room_idx
//    );

    // 해당 채팅방의 인덱스 번호를 보내 채팅방 접속 횟수를 가져온다.
    @FormUrlEncoded
    // POST 방식
    @POST("get_chat_room_access_count.php")
    Call<JsonObject> getChatRoomAccessCount(
            @Header("Authorization") String token,
            @Field("chat_room_idx") String chat_room_idx
    );

    // 해당 채팅방의 채팅방 접속 횟수를 db에 저장한다.
    @FormUrlEncoded
    // POST 방식
    @POST("set_chat_room_access_count.php")
    Call<JsonObject> setChatRoomAccessCount(
            @Header("Authorization") String token,
            @Field("chat_room_idx") String chat_room_idx
    );

    // 내가 들어가 있는 채팅방 정보를 불러온다.
    @Headers({
            "Content-Type:application/json"
    })
    // POST 방식
    @POST("fetch_my_chat_room_info.php")
    Call<JsonObject> fetchMyChatRoomInfo(
            @Header("Authorization") String token
    );

    // 저장 버튼을 누를 경우 watch_later_video_info 테이블에 해당 영상 정보를 저장한다.
    @FormUrlEncoded
    // POST 방식
    @POST("watch_later_video_info_add.php")
    // @Body: API 요청시 해쉬맵 데이터를 json데이터로 변경
    Call<JsonObject> watchLaterVideoInfoAdd(
            @Header("Authorization") String token,
            @Field("video_index") int video_index
    );

    // 나중에 볼 영상에 대한 정보를 가져온다.
    // POST 방식
    @POST("fetch_watch_later_video_info.php")
    Call<JsonObject> getWatchLaterVideoInfo(
            @Header("Authorization") String token
    );

    // 좋아요 또는 싫어요 표시를 db에 저장
    @FormUrlEncoded
    // POST 방식
    @POST("like_or_dislike_add.php")
    Call<JsonObject> likeOrDislikeAdd(
            @Header("Authorization") String token,
            @Field("video_index") int video_index,
            @Field("like_or_dislike") String like_or_dislike
    );

    // 좋아요 또는 싫어요 상태 확인
    @FormUrlEncoded
    // POST 방식
    @POST("fetch_like_or_dislike_status.php")
    Call<JsonObject> getlikeOrDislikeStatus(
            @Header("Authorization") String token,
            @Field("video_index") int video_index
    );

    // 좋아요 또는 싫어요 수를 더하거나 뺌
    @FormUrlEncoded
    // POST 방식
    @POST("set_like_or_dislike_count.php")
    Call<JsonObject> setlikeOrDislikeCount(
            @Header("Authorization") String token,
            @Field("video_index") int video_index,
            @Field("like_or_dislike") String like_or_dislike,
            @Field("plus_or_minus") String plus_or_minus
    );


    // 좋아요 또는 싫어요 수를 가져옴
    @FormUrlEncoded
    // POST 방식
    @POST("get_like_or_dislike_count.php")
    Call<JsonObject> getlikeOrDislikeCount(
            @Header("Authorization") String token,
            @Field("video_index") int video_index,
            @Field("like_or_dislike") String like_or_dislike,
            @Field("plus_or_minus") String plus_or_minus
    );

    // 조회수를 증가시킴
    @FormUrlEncoded
    // POST 방식
    @POST("add_views_count.php")
    Call<JsonObject> addViewsCount(
            @Header("Authorization") String token,
            @Field("video_index") int video_index
    );

    // access_token을 재발급
    @FormUrlEncoded
    // POST 방식
    @POST("renew_access_token.php")
    Call<JsonObject> renewAccessToken(
            @Header("Authorization") String access_token,
            @Field("refresh_token") String refresh_token,
            @Field("id") String id

    );

}