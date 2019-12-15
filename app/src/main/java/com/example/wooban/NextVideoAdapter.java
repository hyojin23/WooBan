package com.example.wooban;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class NextVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "NextVideoAdapter";
    // 번들에 들어가 있는 것: 이름, 프로필 이미지 url, 영상 인덱스, id
    private Bundle bundle;
    private Context context;
    private JsonArray jsonArray, replyArray;
    private JsonObject jsonObject;
    private Activity activity;
    private final int TYPE_HEADER = 0;
    private int TYPE_VIDEO_ITEM = 1;
    private int TYPE_EDIT_REPLY = 6;
    private int TYPE_REPLY_LIST = 7;
    private int TYPE_PROGRESS = -1;
    private final int TYPE_FOOTER = 2;
    private HeaderViewHolderListener headerViewHolderListener = null;
    private String reply_profile_image_url, reply_name, my_id, reply_post_id, selected_reply_content, like_or_dislike = "none";
    private EditText dialogReplyEditText;
    private AlertDialog dialog;
    private int video_index, reply_index;
    private ArrayList arrayList = new ArrayList();
    // 댓글 입력, 수정을 하는 팝업 다이얼로그 안에 들어있는 이미지뷰
    private ProgressBar dialogSendingProgress;
    private ImageView dialogProfileImage, replySendButton;
    // 수정 버튼이 클릭되었는지 판별
    boolean isModifyClicked;
    private int reply_adapter_position;
    // 댓글을 불러오는 쿼리문을 구분하기 위해 사용. 0 : 댓글 페이징시, 1: 댓글 추가시 2: 댓글 수정시
    private int is_paging;
    private int total_reply_count;
    private static final String SEGMENT_VIDEO = "video";
//    private replyModifyCallback callback;


    // 헤더 뷰홀더에 대한 리스너 인터페이스
    public interface HeaderViewHolderListener {
        void createHeaderViewHolder();
    }

    // VideoWatchActivity 액티비티의 리스너 객체를 어댑터에 전달
    public void setHeaderViewHolderListener(HeaderViewHolderListener listener) {
        headerViewHolderListener = listener;

    }

//    public interface replyModifyCallback {
//        void replyModify(int reply_adapter_position, String content);
//    }

    // 헤더 뷰홀더
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, viewsTextView, descriptionTextView, nameTextView, dateTextView, tagTextView, likeCountTextView, dislikeCountTextView;
        ImageView profileImageView, arrowButton, likeImageView, dislikeImageView;
        ConstraintLayout descriptionLayout, shareLayout, likeLayout, dislikeLayout;
        private HashTagHelper tagTextHashTagHelper;
        ConstraintLayout arrowButtonLayout, watchLaterLayout;
        // 화살표 돌아가게 하는 애니메이션
        Animation rotate;


        HeaderViewHolder(final View headerView) {
            super(headerView);
            Log.d(TAG, "HeaderViewHolder: 다음 동영상 헤더 뷰홀더 실행");

            // 헤더에 있는 뷰 객체 참조
            // 영상 제목
            titleTextView = headerView.findViewById(R.id.next_video_recycler_text_view);
            // 조회수
            viewsTextView = headerView.findViewById(R.id.next_video_recycler_views_text_view);
            // 영상 내용
            descriptionTextView = headerView.findViewById(R.id.video_watch_description);
            // 영상 작성자 프로필 이미지
            profileImageView = headerView.findViewById(R.id.video_watch_profile_image);
            // 작성자 이름
            nameTextView = headerView.findViewById(R.id.video_watch_name);
            // 설명이 담긴 레이아웃
            descriptionLayout = headerView.findViewById(R.id.video_watch_description_layout);
            // 영상 업로드 날짜
            dateTextView = headerView.findViewById(R.id.video_watch_post_date);
            // 태그 텍스트뷰
            tagTextView = headerView.findViewById(R.id.video_watch_tag);
            // 제목과 화살표가 포함된 레이아웃
            arrowButtonLayout = headerView.findViewById(R.id.next_video_recycler_title_const_layout);
            // 좋아요 레이아웃
            likeLayout = headerView.findViewById(R.id.video_watch_like_count_layout);
            // 좋아요 이미지
            likeImageView = headerView.findViewById(R.id.video_watch_like_count_image);
            // 좋아요 수 텍스트
            likeCountTextView = headerView.findViewById(R.id.video_watch_like_count_text);
            // 싫어요 레이아웃
            dislikeLayout = headerView.findViewById(R.id.video_watch_dislike_count_layout);
            // 싫어요 이미지
            dislikeImageView = headerView.findViewById(R.id.video_watch_dislike_count_image);
            // 싫어요 수
            dislikeCountTextView = headerView.findViewById(R.id.video_watch_dislike_count_text);
            // 공유 버튼
            shareLayout = headerView.findViewById(R.id.video_watch_share_layout);
            // 저장 버튼
            watchLaterLayout = headerView.findViewById(R.id.video_watch_watch_later_layout);
            // 영상 인덱스 번호
            video_index = bundle.getInt("video_index");
            Log.d(TAG, "sendReplyInfo: 영상 인덱스 번호: " + video_index);
            // 내 아이디
            my_id = bundle.getString("my_id");
            Log.d(TAG, "HeaderViewHolder: 내 아이디: " + my_id);
            // 댓글 입력창 옆 나의 프로필 이미지 등록
            reply_profile_image_url = bundle.getString("my_profile_image_url");
            // 나의 이름
            reply_name = bundle.getString("my_name");

            // db에 요청하여 총 댓글 갯수를 구함
            getReplyCouont();

            // 좋아요 또는 싫어요 상태를 가져옴
            getLikeOrDislikeStatus(likeImageView, dislikeImageView);

            /* 좋아요가 눌린 상태: like
               싫어요가 눌린 상태: dislike
               아무것도 안 눌린 상태: none */
            // 좋아요 레이아웃 클릭시
            likeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 처음에 좋아요에 눌러져 있을 때
                    if (like_or_dislike.equals("like")) {
                        Log.d(TAG, "onClick: 좋아요 레이아웃 클릭시 처음에 좋아요 눌러져 있을 때");
                        // db에서 like_or_dislike 값을 none로 바꿈
                        likeOrDislikeAdd("none");
                        // 좋아요 버튼을 회색으로 바꿈
                        likeImageView.setColorFilter(Color.parseColor("#757575"));
                        // 좋아요 숫자를 감소시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "like", "minus");
                        // 메세지
                        Toast.makeText(context, "영상에 좋아요를 취소했습니다.", Toast.LENGTH_SHORT).show();
                        // 싫어요에 눌러져 있을 때
                    } else if (like_or_dislike.equals("dislike")) {
                        Log.d(TAG, "onClick: 좋아요 레이아웃 클릭시 싫어요에 눌러져 있을 때");
                        // like 값 db에 저장
                        likeOrDislikeAdd("like");
                        // 버튼을 빨간색으로 바꿈
                        likeImageView.setColorFilter(Color.parseColor("#E53935"));
                        // 싫어요 버튼 회색으로 바꿈
                        dislikeImageView.setColorFilter(Color.parseColor("#757575"));
                        // 좋아요 숫자 증가시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "like", "plus");
                        // 싫어요 숫자 감소시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "dislike", "minus");
                        // 메세지
                        Toast.makeText(context, "영상에 좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                        // 아무것도 안 눌러져 있을 때
                    } else {
                        Log.d(TAG, "onClick: 좋아요 레이아웃 클릭시 아무것도 안 눌러져 있을 때");
                        // like 값 db에 저장
                        likeOrDislikeAdd("like");
                        // 버튼을 빨간색으로 바꿈
                        likeImageView.setColorFilter(Color.parseColor("#E53935"));
                        // 좋아요 숫자 증가시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "like", "plus");
                        // 메세지
                        Toast.makeText(context, "영상에 좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();

                    }

                }
            });

            // 싫어요 레이아웃 클릭시
            dislikeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 처음에 싫어요에 눌러져 있을 때
                    if (like_or_dislike.equals("dislike")) {
                        Log.d(TAG, "onClick: 싫어요 레이아웃 클릭시 처음에 싫어요 눌러져 있을 때");
                        // db에서 like_or_dislike 값을 none로 바꿈
                        likeOrDislikeAdd("none");
                        // 싫어요 버튼을 회색으로 바꿈
                        dislikeImageView.setColorFilter(Color.parseColor("#757575"));
                        // 싫어요 숫자를 감소시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "dislike", "minus");
                        // 메세지
                        Toast.makeText(context, "영상에 싫어요를 취소했습니다.", Toast.LENGTH_SHORT).show();

                        // 좋아요에 눌러져 있을 때
                    } else if (like_or_dislike.equals("like")) {
                        Log.d(TAG, "onClick: 싫어요 레이아웃 클릭시 좋아요에 눌러져 있을 때");
                        // like 값 db에 저장
                        likeOrDislikeAdd("dislike");
                        // 싫어요 버튼 빨간색으로 바꿈
                        dislikeImageView.setColorFilter(Color.parseColor("#E53935"));
                        // 좋아요 버튼 회색으로 바꿈
                        likeImageView.setColorFilter(Color.parseColor("#757575"));
                        // 싫어요 숫자 증가시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "dislike", "plus");
                        // 좋아요 숫자 감소시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "like", "minus");
                        // 메세지
                        Toast.makeText(context, "영상에 싫어요를 눌렀습니다.", Toast.LENGTH_SHORT).show();

                        // 아무것도 안 눌러져 있을 때
                    } else {
                        Log.d(TAG, "onClick: 싫어요 레이아웃 클릭시 아무것도 안 눌러져 있을 때");
                        // dislike 값 db에 저장
                        likeOrDislikeAdd("dislike");
                        // 버튼을 빨간색으로 바꿈
                        dislikeImageView.setColorFilter(Color.parseColor("#E53935"));
                        // 싫어요 숫자 증가시킴
                        setLikeOrDislikeCount(likeCountTextView, dislikeCountTextView, "dislike", "plus");
                        // 메세지
                        Toast.makeText(context, "영상에 싫어요를 눌렀습니다.", Toast.LENGTH_SHORT).show();

                    }
                }
            });
            // 화살표 레이아웃 클릭시
            arrowButtonLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: 아래 화살표 버튼 레이아웃 클릭");
                    // 화살표 버튼 참조
                    arrowButton = headerView.findViewById(R.id.next_video_recycler_arrow_button);

                    Log.d(TAG, "onClick: 선택: " + v.isSelected());
                    /* 버튼이 첫번째로 눌리면 왼쪽으로 돌고, 두번째로 눌리면 오른쪽으로 돌게함 */

                    // 버튼이 두번째로 눌렸을 때 (영상 설명이 안보이게 됨)
                    if (v.isSelected()) {
                        Log.d(TAG, "onClick: 버튼이 오른쪽으로 돈다.");
                        // 버튼을 위쪽 화살표로 만들고 오른쪽으로 돌게함
                        arrowButton.setImageResource(R.drawable.ic_arrow_drop_up_gray_36dp);
                        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate_right_180);
                        arrowButton.startAnimation(rotate);
                        descriptionLayout.setVisibility(View.GONE);
                        v.setSelected(false);
                        // 버튼이 처음 눌렸을 때 (영상 설명이 보이게 됨)
                    } else {
                        Log.d(TAG, "onClick: 버튼이 왼쪽으로 돈다.");
                        // 버튼을 아랫쪽 화살표로 만들고 왼쪽으로 돌게함
                        arrowButton.setImageResource(R.drawable.ic_arrow_drop_down_gray_36dp);
                        // 애니메이션 설정
                        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate_left_180);
                        // 애니메이션 실행
                        arrowButton.startAnimation(rotate);
                        descriptionLayout.setVisibility(View.VISIBLE);
                        v.setSelected(true);
                    }
                }
            });
            // 공유 버튼 클릭시
            shareLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: 공유 버튼 클릭됨");
//                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
//                    intent.setType("text/plain");
////                    intent.putExtra(Intent.EXTRA_TEXT, "https://wooban.page.link/b4tu");
//                    intent.putExtra(Intent.EXTRA_TEXT, Uri.parse("https://wooban"));
//                    // 공유 UI 상단에 입력될 텍스트
//                    context.startActivity(Intent.createChooser(intent, "공유하기"));
                    onDynamicLinkClick();
                }
            });

            // 저장 버튼 클릭시
            watchLaterLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: 저장 버튼 클릭됨");
                    Toast.makeText(context, "나중에 볼 영상에 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    watchLaterVideoInfoAdd();

                }
            });


        }

    }


    // 다음 동영상 뷰홀더
    public class NextViewHolder extends RecyclerView.ViewHolder {
        // 썸네일 이미지뷰, 내용 나오게 하는 화살표 버튼
        ImageView thumImageView;
        TextView titleTextView, nameTextView, viewsTextView, durationTextView;
        ConstraintLayout itemLayout;

        NextViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            thumImageView = itemView.findViewById(R.id.next_video_recycler_thumbnail);
            titleTextView = itemView.findViewById(R.id.next_video_recycler_title);
            nameTextView = itemView.findViewById(R.id.next_video_recycler_name);
            viewsTextView = itemView.findViewById(R.id.next_video_recycler_views);
            durationTextView = itemView.findViewById(R.id.next_video_duration);
            itemLayout = itemView.findViewById(R.id.next_video_recycler_item_layout);


        }
    }


    // 푸터 뷰홀더
    class FooterViewHolder extends RecyclerView.ViewHolder {
        ImageView myProfileImage, dialogProfileImage, replySendButton;
        TextView replyEditTextView, totalReplyTextView, replyInfoTextView;
//        EditText dialogReplyEditText;
//        ProgressBar dialogSendingProgress;


        FooterViewHolder(final View footerView) {
            super(footerView);
            Log.d(TAG, "FooterViewHolder: 실행");

            // 액티비티 내 프로필 이미지
            myProfileImage = footerView.findViewById(R.id.video_reply_profile_image_view);
            // 액티비티 댓글 입력창
            replyEditTextView = footerView.findViewById(R.id.video_reply_edit_text_text_view);
            // 총 댓글 텍스트뷰
            totalReplyTextView = footerView.findViewById(R.id.video_reply_reply_count_text_view);
            // 댓글 사용 규칙 안내
            replyInfoTextView = footerView.findViewById(R.id.video_reply_information);


            // 댓글 입력창 클릭시
            replyEditTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: 댓글 입력창 클릭됨");
                    // 댓글 입력을 위한 EditText 다이얼로그를 띄움
                    alertReplyEditDialog(selected_reply_content);

                }
            });
        }
    }

    // 댓글 뷰홀더
    class ReplyViewHolder extends RecyclerView.ViewHolder {
        ImageView replyProfileImageView, replyLikeImageView, replyDislikeImageView, modifyDeleteMenuButton;
        TextView replyNameTextView, replyContentTextView, replyLikeCountTextView;
        ProgressBar progressBar;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);

            // 댓글 프로필 이미지
            replyProfileImageView = itemView.findViewById(R.id.video_reply_other_profile_image);
            // 댓글 작성자와 작성 시간
            replyNameTextView = itemView.findViewById(R.id.video_reply_name_and_post_time);
            // 댓글 내용
            replyContentTextView = itemView.findViewById(R.id.video_reply_content);
            // 좋아요 버튼
            replyLikeImageView = itemView.findViewById(R.id.video_reply_like_image_view);
            // 싫어요 버튼
            replyDislikeImageView = itemView.findViewById(R.id.video_reply_dislike_image_view);
            // 좋아요 수
            replyLikeCountTextView = itemView.findViewById(R.id.video_reply_like_count_text_view);
            // 수정, 삭제 선택할 수 있는 메뉴버튼
            modifyDeleteMenuButton = itemView.findViewById(R.id.video_reply_modify_delete_select_image_view);
            // 수정, 삭제 선택 버튼 클릭시
            modifyDeleteMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: 수정, 삭제 선택버튼 클릭됨");
                    // 팝업 메뉴 생성
                    PopupMenu popupMenu = new PopupMenu(context, modifyDeleteMenuButton);
                    popupMenu.inflate(R.menu.video_watch_reply_modify_delete_menu);
                    popupMenu.show();
                    // 팝업 메뉴 클릭 감지
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.video_watch_reply_modify_menu_text:
                                    // 수정 메뉴 클릭시
                                    Log.d(TAG, "onMenuItemClick: 수정 클릭됨");
                                    isModifyClicked = true;
                                    // 수정 메뉴 클릭시 해당 댓글의 내용과 인덱스 번호를 구함
                                    selected_reply_content = jsonArray.get(getAdapterPosition() - 2).getAsJsonObject().get("content").getAsString();
                                    reply_index = jsonArray.get(getAdapterPosition() - 2).getAsJsonObject().get("idx").getAsInt();
                                    reply_adapter_position = getAdapterPosition();
                                    // 원래 댓글 내용을 담은 댓글 입력창을 띄움
                                    alertReplyEditDialog(selected_reply_content);

                                    break;
                                case R.id.video_watch_reply_delelte_menu_text:
                                    // 삭제 메뉴 클릭시
                                    Log.d(TAG, "onMenuItemClick: 삭제 클릭됨");
                                    // 삭제 메뉴 클릭시 해당 댓글의 인덱스 번호를 구함
                                    reply_index = jsonArray.get(getAdapterPosition() - 2).getAsJsonObject().get("idx").getAsInt();
                                    Log.d(TAG, "onMenuItemClick: reply_index: " + reply_index);
                                    replyDeleteDialogShow(getAdapterPosition());
                                    break;
                            }
                            return false;
                        }
                    });
                }
            });


        }
    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;

        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.next_video_recycler_progressbar);
        }
    }

    // 아이템 뷰 타입 지정
    @Override
    public int getItemViewType(int position) {


        if (position == 0) {
            Log.d(TAG, "getItemViewType: 어댑터에서 포지션:" + position);
            Log.d(TAG, "getItemViewType: 헤더 타입");
            return TYPE_HEADER;
        } else if (1 <= position && position <= 5) {
            Log.d(TAG, "getItemViewType: 어댑터에서 포지션:" + position);
            Log.d(TAG, "getItemViewType: 다음 동영상 타입");
            TYPE_VIDEO_ITEM = position;
            return TYPE_VIDEO_ITEM;
        } else if (position == 6) {
            Log.d(TAG, "getItemViewType: 어댑터에서 포지션:" + position);
            Log.d(TAG, "getItemViewType: 댓글 입력 타입");
            TYPE_EDIT_REPLY = position;
            return TYPE_EDIT_REPLY;
        } else if (position > 6 && jsonArray.get(position - 2).isJsonObject()) {
            Log.d(TAG, "getItemViewType: 어댑터에서 포지션:" + position);
            Log.d(TAG, "getItemViewType: 댓글 리스트 타입");
            return TYPE_REPLY_LIST;
        } else if (!jsonArray.get(position - 2).isJsonObject()) {
            Log.d(TAG, "getItemViewType: 어댑터에서 포지션: " + position);
            Log.d(TAG, "getItemViewType: 프로그레스바 타입");
            return TYPE_PROGRESS;
        } else {
            return 0;
        }
    }


    // 생성자
    public NextVideoAdapter(Context context, JsonArray jsonArray, Bundle bundle, Activity activity) {
        this.context = context;
        this.jsonArray = jsonArray;
        this.activity = activity;
        this.bundle = bundle;
    }


    // 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: 실행");
        RecyclerView.ViewHolder holder = null;
        // 헤더 반환
        if (viewType == TYPE_HEADER) {
            Log.d(TAG, "onCreateViewHolder: 헤더 뷰홀더 객체 생성하여 리턴");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.next_video_recycler_header, parent, false);
            holder = new HeaderViewHolder(view);
            return holder;
            // 다음 동영상 반환
        } else if (viewType == TYPE_VIDEO_ITEM) {
            // 커스텀한 뷰 생성
            Log.d(TAG, "onCreateViewHolder: 다음 동영상 뷰홀더 객체 생성하여 리턴");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.next_video_recycler_video_item, parent, false);
            holder = new NextVideoAdapter.NextViewHolder(view);
            return holder;
            // 푸터 반환
        } else if (viewType == TYPE_EDIT_REPLY) {
            Log.d(TAG, "onCreateViewHolder: 푸터 뷰홀더 객체 생성하여 리턴");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.next_video_recycler_footer, parent, false);
            holder = new NextVideoAdapter.FooterViewHolder(view);
            return holder;
            // 댓글 목록 반환
        } else if (viewType == TYPE_REPLY_LIST) {
            Log.d(TAG, "onCreateViewHolder: 댓글 뷰홀더 객체 생성하여 리턴");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.next_video_recycler_reply_item, parent, false);
            holder = new NextVideoAdapter.ReplyViewHolder(view);
            return holder;
        } else {
            Log.d(TAG, "onCreateViewHolder: 프로그레스바 뷰홀더 객체 생성하여 리턴");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.next_video_recycler_progress_item, parent, false);
            holder = new NextVideoAdapter.ProgressViewHolder(view);
            return holder;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: 다음 동영상 어댑터 onBindViewHolder 실행");
        // 헤더 뷰홀더의 객체일 경우
        if (holder instanceof HeaderViewHolder) {
            Log.d(TAG, "onBindViewHolder: 헤더 뷰홀더 객체일 경우");
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            Log.d(TAG, "onBindViewHolder: position:" + position);
            // 제목
            String title = bundle.get("title").toString();
            Log.d(TAG, "onBindViewHolder: title: " + title);
            // 조회수 ex) 조회수 0회
            String views_text = bundle.get("views_text").toString();
            Log.d(TAG, "onBindViewHolder: views_text: " + views_text);
            // 프로필 이미지
            String profile_image_url = bundle.get("profile_image_url").toString();
            Log.d(TAG, "onBindViewHolder: profile_image_url: " + profile_image_url);
            // 이름
            String name = bundle.get("name").toString();
            Log.d(TAG, "onBindViewHolder: name: " + name);
            // 설명
            String description = bundle.get("description").toString();
            Log.d(TAG, "onBindViewHolder: description: " + description);
            // 게시일
            String post_time = bundle.get("post_time").toString();
            Log.d(TAG, "onBindViewHolder: post_time: " + post_time);
            // 태그
            String tag = bundle.get("tag").toString();
            Log.d(TAG, "onBindViewHolder: tag: " + tag);
            // 좋아요 수
            int like_count = bundle.getInt("like_count");
            // 싫어요 수
            int dislike_count = bundle.getInt("dislike_count");
            // 영상 제목
            headerViewHolder.titleTextView.setText(title);
            // 조회수
            headerViewHolder.viewsTextView.setText(views_text);
            // 이미지 url에 디폴트가 들어가면
            if (profile_image_url.contains("default")) {
                // 기본 이미지 중 선택한 이미지를 이미지뷰에 넣어 보여준다.
                DefaultProfileImage defaultImage = new DefaultProfileImage();
                defaultImage.changeToAnimal(context, profile_image_url, headerViewHolder.profileImageView);
            } else {
                // 작성자 프로필 이미지
                Glide.with(headerViewHolder.profileImageView).load(profile_image_url).apply(RequestOptions.circleCropTransform()).into(headerViewHolder.profileImageView);
            }
            // 작성자 이름
            headerViewHolder.nameTextView.setText(name);
            // 영상 설명
            headerViewHolder.descriptionTextView.setText(description);
            // 영상 업로드 날짜
            headerViewHolder.dateTextView.setText(post_time);
            // 좋아요 수
            headerViewHolder.likeCountTextView.setText(String.valueOf(like_count));
            // 싫어요 수
            headerViewHolder.dislikeCountTextView.setText(String.valueOf(dislike_count));
            // 해시태그 헬퍼
            headerViewHolder.tagTextView.setText(tag);
            headerViewHolder.tagTextHashTagHelper = HashTagHelper.Creator.create(context.getResources().getColor(R.color.tagColor), null);
            headerViewHolder.tagTextHashTagHelper.handle(headerViewHolder.tagTextView);

            // 다음 동영상
        } else if (holder instanceof NextViewHolder) {
            Log.d(TAG, "onBindViewHolder: 다음 동영상 뷰홀더 객체일 경우");
            // 리사이클러뷰 뷰홀더의 holder를 NextVideoAdapter의 뷰홀더로 형변환
            NextViewHolder viewHolder = (NextViewHolder) holder;
            Log.d(TAG, "onBindViewHolder: position: " + position);

            /* 헤더가 position이 0 이므로 다음 동영상은 position 1 부터 시작. position이 1 부터 시작되기 때문에
             * jsonArray에서 인덱스 번호 0부터 시작하기 위해서는 position - 1을 해야함 */
            // 영상 제목
            final String title = jsonArray.get(position - 1).getAsJsonObject().get("title").getAsString();
            Log.d(TAG, "onBindViewHolder: title: " + title);
            // 작성자 이름
            final String name = jsonArray.get(position - 1).getAsJsonObject().get("name").getAsString();
            Log.d(TAG, "onBindViewHolder: name: " + name);
            // 조회수
            final int views = jsonArray.get(position - 1).getAsJsonObject().get("views").getAsInt();
            Log.d(TAG, "onBindViewHolder: views: " + views);
            // 썸네일 url
            String thumbnail_url = jsonArray.get(position - 1).getAsJsonObject().get("thumbnail_url").getAsString();
            Log.d(TAG, "onBindViewHolder: thumbnail_url: " + thumbnail_url);
            // 영상 재생시간
            final String video_duration = jsonArray.get(position - 1).getAsJsonObject().get("video_duration").getAsString();
            Log.d(TAG, "onBindViewHolder: video_duration: " + video_duration);

            // 이미지뷰
            Glide.with(context).load(thumbnail_url).into(viewHolder.thumImageView);
            // 제목 세팅
            viewHolder.titleTextView.setText(title);
            // 작성자 세팅
            viewHolder.nameTextView.setText(name);
            // 조회수 세팅
            String viewsString = "조회수 " + views + "회";
            viewHolder.viewsTextView.setText(viewsString);
            // 영상 재생시간
            viewHolder.durationTextView.setText(video_duration);
            // 다음 동영상 아이템 클릭시
            viewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: 포지션: " + position + "인 아이템 클릭됨");
                    Intent intent = new Intent(context, VideoWatchActivity.class);
                    // 영상 url
                    intent.putExtra("video_url", jsonArray.get(position - 1).getAsJsonObject().get("video_url").getAsString());
                    // 영상 제목
                    intent.putExtra("title", title);
                    // 조회수
                    intent.putExtra("views", jsonArray.get(position - 1).getAsJsonObject().get("views").getAsString());
                    // 작성자 프로필 이미지
                    intent.putExtra("profile_image_url", jsonArray.get(position - 1).getAsJsonObject().get("profile_image_url").getAsString());
                    // 작성자 이름
                    intent.putExtra("name", name);
                    // 영상 설명
                    intent.putExtra("description", jsonArray.get(position - 1).getAsJsonObject().get("description").getAsString());
                    // 글 작성 시간
                    intent.putExtra("post_time_millis", video_duration);
                    Log.d(TAG, "onClick: 글 작성 시간: "+video_duration);
                    // 영상 태그
                    intent.putExtra("tag", jsonArray.get(position - 1).getAsJsonObject().get("tag").getAsString());
                    // 내 이름과 내 프로필 이미지 번들 객체 전송
                    intent.putExtra("bundle", bundle);
                    Log.d(TAG, "onClick: 어댑터에서 보내는 번들 객체: " + bundle);
                    // 인덱스 번호
                    intent.putExtra("video_index", jsonArray.get(position - 1).getAsJsonObject().get("idx").getAsString());
                    Log.d(TAG, "onClick: 인덱스 번호: " + jsonArray.get(position - 1).getAsJsonObject().get("idx").getAsString());

                    activity.startActivity(intent);
                    // VideoWatchActivity 종료
                    activity.finish();

                }
            });

            // 푸터
        } else if (holder instanceof FooterViewHolder) {
            Log.d(TAG, "onBindViewHolder: 푸터 뷰홀더 객체일 경우");
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            // 댓글 안내 메세지
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            stringBuilder.append("댓글을 사용할 때는 타인을 존중하고 커뮤니티 가이드를 준수해야 합니다.");
            // 안내 메세지 문자열 중 일부 색깔을 바꿈
            stringBuilder.setSpan(new ForegroundColorSpan(Color.parseColor("#2196F3")), 19, 28, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            footerViewHolder.replyInfoTextView.setText(stringBuilder);


            // 댓글 입력창 옆 나의 프로필 이미지 등록
//            reply_profile_image_url = bundle.getString("my_profile_image_url");
            Glide.with(footerViewHolder.myProfileImage).load(reply_profile_image_url).apply(RequestOptions.circleCropTransform()).into(footerViewHolder.myProfileImage);
            Log.d(TAG, "onBindViewHolder: my_profile_image_url: " + reply_profile_image_url);
            // 나의 이름
//            reply_name = bundle.getString("my_name");
            // 총 댓글 갯수 등록
            footerViewHolder.totalReplyTextView.setText(String.valueOf(total_reply_count));
            Log.d(TAG, "onBindViewHolder: total_reply_count: " + total_reply_count);

            // 댓글
        } else if (holder instanceof ReplyViewHolder) {
            /* 헤더가 postion 0, 푸터가 position 6 이므로 댓글은 postion 7부터 시작. jsonArray에서 댓글은 index 5번 부터 시작되므로
            position - 2 해야함 */
            Log.d(TAG, "onBindViewHolder: 댓글 목록 뷰홀더 객체일 경우");
            final ReplyViewHolder replyViewHolder = (ReplyViewHolder) holder;
            // 프로그레스 바 보이게 함
//            replyViewHolder.progressBar.setVisibility(View.VISIBLE);
            // 이름
            String reply_name = jsonArray.get(position - 2).getAsJsonObject().get("name").getAsString();
            Log.d(TAG, "onBindViewHolder: reply_name: " + reply_name);
            // 프로필 이미지
            String reply_profile_image_url = jsonArray.get(position - 2).getAsJsonObject().get("profile_image_url").getAsString();
            // 댓글 작성 시간 밀리세컨드
            String reply_post_time_millis = jsonArray.get(position - 2).getAsJsonObject().get("post_time_millis").getAsString();
            // 댓글 내용
            String reply_content = jsonArray.get(position - 2).getAsJsonObject().get("content").getAsString();
            Log.d(TAG, "onBindViewHolder: reply_content: " + reply_content);
            // 댓글 좋아요 수
            String reply_like_count = jsonArray.get(position - 2).getAsJsonObject().get("like_count").getAsString();
            // 댓글 싫어요 수
            String reply_dislike_count = jsonArray.get(position - 2).getAsJsonObject().get("dislike_count").getAsString();
            // 댓글 인덱스 번호
//            reply_index = jsonArray.get(position - 2).getAsJsonObject().get("idx").getAsInt();
            Log.d(TAG, "onBindViewHolder: 포지션 position: " + position);
            // 댓글 작성자 아이디
            reply_post_id = jsonArray.get(position - 2).getAsJsonObject().get("id").getAsString();
            // 해당 댓글이 수정되었는지 여부(is_paging = 2 이면 수정버튼을 눌러 수정된 것임)
            int is_modified = jsonArray.get(position - 2).getAsJsonObject().get("is_paging").getAsInt();

            Log.d(TAG, "onBindViewHolder: my_id: " + my_id);
            Log.d(TAG, "onBindViewHolder: reply_post_id: " + reply_post_id);

            if (my_id.equals(reply_post_id)) {
                Log.d(TAG, "onBindViewHolder: 내 아이디와 댓글 작성자 아이디가 같음");
                replyViewHolder.modifyDeleteMenuButton.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "onBindViewHolder: 내 아이디와 댓글 작성자 아이디가 다름");
                replyViewHolder.modifyDeleteMenuButton.setVisibility(View.GONE);
            }


            long reply_post_time_millis_long = Long.parseLong(reply_post_time_millis);
            // 댓글 작성 시간을 몇분전, 몇시간전 형태로 바꿈
            String reply_post_time_string = formatTimeString(reply_post_time_millis_long);
            // 작성자 이름 + 시간 ex) 골든리트리버 · 1시간 전
            String name_and_post_time = reply_name + " · " + reply_post_time_string;
            // 수정된 댓글일 경우
            if (is_modified == 2) {
                name_and_post_time = reply_name + " · " + reply_post_time_string + "(수정됨)";
            }
            // 이름과 작성시간 등록
            replyViewHolder.replyNameTextView.setText(name_and_post_time);
            // 프로필 이미지 등록
            Glide.with(replyViewHolder.replyProfileImageView).load(reply_profile_image_url).apply(RequestOptions.circleCropTransform()).into(replyViewHolder.replyProfileImageView);
            // 내용 등록
            replyViewHolder.replyContentTextView.setText(reply_content);
            // 좋아요 등록
            replyViewHolder.replyLikeCountTextView.setText(reply_like_count);

//            callback = new replyModifyCallback() {
//
//
//                @Override
//                public void replyModify(int reply_adapter_position, String content) {
//                    Log.d(TAG, "replyModify: 댓글 뷰홀더에서 댓글 내용을 바꿈");
//                    replyViewHolder.replyContentTextView.setText(content);
//                    Log.d(TAG, "replyModify: content: "+content);
//                    notifyItemChanged(reply_adapter_position);
//                    Log.d(TAG, "replyModify: reply_adapter_position: "+reply_adapter_position);
//                }
//            };


        } else {
            Log.d(TAG, "onBindViewHolder: 프로그레스 뷰홀더 객체인 경우");
        }

    }

//    // position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
//    @Override
//    public void onBindViewHolder(@NonNull NextVideoAdapter.ViewHolder holder, int position) {
//
//        // 영상 제목
//        String title = jsonArray.get(position).getAsJsonObject().get("title").getAsString();
//        // 작성자 이름
//        final String name = jsonArray.get(position).getAsJsonObject().get("name").getAsString();
//        Log.d(TAG, "onBindViewHolder: name: " + name);
//        // 조회수
//        final int views = jsonArray.get(position).getAsJsonObject().get("views").getAsInt();
//        Log.d(TAG, "onBindViewHolder: views: " + views);
//        // 썸네일 url
//        String thumbnail_url = jsonArray.get(position).getAsJsonObject().get("thumbnail_url").getAsString();
//        Log.d(TAG, "onBindViewHolder: thumbnail_url: " + thumbnail_url);
//        // 영상 재생시간
//        String video_duration = jsonArray.get(position).getAsJsonObject().get("video_duration").getAsString();
//        Log.d(TAG, "onBindViewHolder: video_duration: " + video_duration);
//
//        // 이미지뷰
//        Glide.with(context).load(thumbnail_url).into(holder.thumImageView);
//        // 제목 세팅
//        holder.titleTextView.setText(title);
//        // 작성자 세팅
//        holder.nameTextView.setText(name);
//        // 조회수 세팅
//        String viewsString = "조회수 " + views + "회";
//        holder.viewsTextView.setText(viewsString);
//        // 영상 재생시간
//        holder.durationTextView.setText(video_duration);
//    }

    // 전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: 실행");
        // 헤더와 푸터가 포함되어 +2를 함
        int size = jsonArray.size() + 2;
        Log.d(TAG, "getItemCount: 총 아이템 갯수: " + size);
        Log.d(TAG, "getItemCount: jsonArray: " + jsonArray);
        return size;
    }

    // 댓글 작성시 댓글에 대한 정보를 보내는 메소드
    private void sendReplyInfo(final String content, final int reply_adapter_position) {
        if (isModifyClicked) {
            /* 댓글 수정 시 요청*/

            // 댓글 추가 기능이 제대로 동작하도록 수정버튼을 누르지 않은 상태로 변경
            isModifyClicked = false;

            Call<JsonObject> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .replyModify(getSharedToken(), video_index, reply_index, content);
            Log.d(TAG, "sendReplyInfo: video_index: " + video_index);
            Log.d(TAG, "sendReplyInfo: reply_index: " + reply_index);
            Log.d(TAG, "sendReplyInfo: content: " + content);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d(TAG, "onResponse: 댓글 수정요청 응답 성공");
//                    callback.replyModify(reply_adapter_position, content);
//                    Log.d(TAG, "onResponse: 댓글 수정 후 다이얼로그 종료");
                    // 수정된 댓글 jsonObject를 가져옴
                    fetchReplyInfoForModify(reply_adapter_position);

                    dialog.dismiss();


                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "onFailure: 댓글 수정요청 응답 실패");
                }
            });

        } else {
            /* 댓글 추가 시 요청*/
            // 해쉬맵 생성
            HashMap<String, String> map = new HashMap<>();
            // 작성자 프로필 url
            map.put("reply_profile_image_url", reply_profile_image_url);
            Log.d(TAG, "sendReplyInfo: reply_profile_image_url: " + reply_profile_image_url);
            // 작성 시간
            long time = System.currentTimeMillis();
            String post_time_millis = String.valueOf(time);
            map.put("post_time_millis", post_time_millis);
            Log.d(TAG, "sendReplyInfo: post_time_millis: " + post_time_millis);
            // 내용
            map.put("content", content);
            Log.d(TAG, "sendReplyInfo: 댓글 입력 내용: " + content);


            // 댓글 전송 요청
            Call<JsonObject> call = RetrofitClient
                    .getInstance()
                    .getApi()
                    .reply_post(getSharedToken(), map, video_index);

            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    Log.d(TAG, "onResponse: 댓글 전송요청 응답 성공");
                    fetchReplyInfo();


                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d(TAG, "onFailure: 댓글 전송요청 응답 실패");
                    Toast.makeText(context, "댓글 추가 실패", Toast.LENGTH_SHORT).show();

                }
            });

        }


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

    /**
     * 몇분전, 방금 전,
     */
    public static String formatTimeString(long regTime) {
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;
        String msg = null;
        if (diffTime < MainAdapter.TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= MainAdapter.TIME_MAXIMUM.SEC) < MainAdapter.TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= MainAdapter.TIME_MAXIMUM.MIN) < MainAdapter.TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= MainAdapter.TIME_MAXIMUM.HOUR) < MainAdapter.TIME_MAXIMUM.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= MainAdapter.TIME_MAXIMUM.DAY) < MainAdapter.TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        return msg;
    }


    // 댓글 입력 후 db에 있는 댓글 정보를 불러오는 메소드
    private void fetchReplyInfo() {
        is_paging = 1;

        /* 현재 jsonArray.size() 만큼 댓글을 불러온다. */
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchReplyInfo(getSharedToken(), video_index, reply_index, jsonArray.size(), is_paging);
        Log.d(TAG, "fetchReplyInfo: jsonArray.size(): " + jsonArray.size());
        Log.d(TAG, "fetchReplyInfo: video_index: " + video_index);
        Log.d(TAG, "fetchReplyInfo: reply_index: " + reply_index);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 추가된 댓글 정보 가져오기 성공");
                JsonObject reply_data = response.body();
                Log.d(TAG, "onResponse: reply_data: " + reply_data);
                JsonArray replyDataArray = null;


                if (reply_data != null) {
                    replyDataArray = reply_data.getAsJsonArray("reply_info");
                }
                Log.d(TAG, "onResponse: replyDataArray: " + replyDataArray);

                if (replyDataArray != null) {
                    /*jsonArray 인덱스 0~4에는 다음 동영상 정보가 들어있고 인덱스 5부터 댓글 정보가 들어있다.
                     * 추가된 댓글을 가장 위에 보이게 하기 위해 인덱스 5부터 가져온 댓글을 넣는다.*/
                    Log.d(TAG, "onResponse: jsonArray.size(): " +jsonArray.size());
                    for (int i = 5; 4 < i && i < jsonArray.size(); i++) {
                        JsonObject addedReply = replyDataArray.get(i - 5).getAsJsonObject();
                        Log.d(TAG, "onResponse: addedReply: " + addedReply);
                        jsonArray.set(i, addedReply);
                    }


                }
                Log.d(TAG, "onResponse: jsonArray: " + jsonArray);


                // jsonArray에 댓글 오브젝트 추가
//                if (replyDataArray != null) {
//                    for (JsonElement element : replyDataArray) {
//                    }
//                }
                //
                notifyItemInserted(7);
                Toast.makeText(context, "댓글 추가 완료", Toast.LENGTH_SHORT).show();
                // 다이얼로그 종료
                dialog.dismiss();
//                Log.d(TAG, "onResponse: 댓글 추가된 jsonArray: " + jsonArray);
//                // 1초 뒤 프로그레스 바를 종료한다.
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        // 데이터가 추가되었음을 알려줌
//                        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
//                        progressBar.setVisibility(View.GONE);
//                    }
//                }, 1000);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 추가된 댓글 정보 가져오기 실패");

            }
        });
    }

    // 수정된 댓글 정보를 불러오는 메소드

    private void fetchReplyInfoForModify(final int reply_adapter_position) {
        // 수정된 댓글 정보를 가져올 경우 is_paging = 2
        is_paging = 2;

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .fetchReplyInfo(getSharedToken(), video_index, reply_index, jsonArray.size(), is_paging);
        Log.d(TAG, "fetchReplyInfo: jsonArray.size(): " + jsonArray.size());
        Log.d(TAG, "fetchReplyInfo: video_index: " + video_index);
        Log.d(TAG, "fetchReplyInfo: reply_index: " + reply_index);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 수정된 댓글 정보 가져오기 응답 성공");
                JsonObject newReplyData = response.body();
                Log.d(TAG, "onResponse: new_reply_data: " + newReplyData);
                if (newReplyData != null) {
                    // 가져온 데이터 Array
                    JsonArray NewReplyDataArray = newReplyData.getAsJsonArray("reply_info");
                    // NewReplyDataArray 안 JsonObject
                    JsonObject newReplyJsonObject = NewReplyDataArray.get(0).getAsJsonObject();
                    Log.d(TAG, "onResponse: newReplyJsonObject: " + newReplyJsonObject);
                    Log.d(TAG, "onResponse: reply_adapter_position: " + reply_adapter_position);

                    Log.d(TAG, "onResponse: 수정된 데이터 넣기 전 jsonArray: " + jsonArray);
                    // jsonArray에 바꿔넣음
                    jsonArray.set(reply_adapter_position - 2, newReplyJsonObject);
                    Log.d(TAG, "onResponse: 수정된 데이터 넣은 후 jsonArray: " + jsonArray);
                    // 어댑터에 데이터가 변경되었음을 알림
                    notifyDataSetChanged();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 수정된 댓글 정보 가져오기 응답 실패");

            }
        });
    }


    // 키보드 내리기
    public void keyBoradDown(EditText editText) {
        // 키보드 내리기
        InputMethodManager hide = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        hide.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    // 댓글 삭제 클릭시 나타나는 다이얼로그
    private void replyDeleteDialogShow(final int getAdapterPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("댓글 삭제");
        builder.setMessage("댓글을 완전히 삭제할까요?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 예를 선택했습니다.");
                        Toast.makeText(context, "댓글이 삭제되었습니다.", Toast.LENGTH_LONG).show();
                        // db에 댓글 삭제를 요청
                        replyDelete();
                        /*해당 아이템을 jsonArray에서 삭제(adapter에는 header와 footer가 있기 때문에 getAdapterPosition에서 2를 뺀 값이 jsonArray에서
                         * 해당 댓글의 index이다.*/
                        Log.d(TAG, "onClick: 어레이에서 댓글 삭제");
                        jsonArray.remove(getAdapterPosition - 2);
                        Log.d(TAG, "onClick: 댓글 삭제되었음을 어댑터에 반영");
                        // 삭제되었음을 리사이클러뷰 어댑터에 반영
                        notifyItemRemoved(getAdapterPosition);
                        Log.d(TAG, "onClick: 변화 반영");
                        Log.d(TAG, "onClick: jsonArray: " + jsonArray);
                        Log.d(TAG, "onClick: reply_index: " + reply_index);
                        notifyItemRangeChanged(getAdapterPosition, jsonArray.size());


                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "onClick: 아니오를 선택했습니다.");
//                        Toast.makeText(context,"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
    }

    // db에 댓글 삭제를 요청하는 메소드
    private void replyDelete() {
        Log.d(TAG, "replyDelete: replyDelete() 실행");
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .replyDelete(getSharedToken(), video_index, reply_index);
        Log.d(TAG, "replyDelete: 실행 2");
        // 선택된 댓글의 어댑터 포지션으로 댓글 인덱스 번호를 구한다.
//        Log.d(TAG, "replyDelete: jsonArray.get(getAdapterPosition-2): "+jsonArray.get(getAdapterPosition-2));
//        Log.d(TAG, "replyDelete: getAdapterPosition: "+getAdapterPosition);
        Log.d(TAG, "replyDelete: reply_index: " + reply_index);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 댓글 삭제요청 응답 성공");

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 댓글 삭제요청 응답 실패");
            }
        });

    }

    // 댓글 입력이나 수정을 하게 하는 다이얼로그 생성
    private void alertReplyEditDialog(final String selected_reply_content) {
        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_AppCompat_Dialog_Alert);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.reply_edit_text_popup, null);
        builder.setView(dialogView);
        // 다이얼로그 댓글 입력창 참조
        dialogReplyEditText = dialogView.findViewById(R.id.dialog_reply_edit_text);
        // 다이얼로그 댓글 전송버튼 참조
        replySendButton = dialogView.findViewById(R.id.dialog_reply_send_button);
        // 다이얼로그 프로필 이미지 참조
        dialogProfileImage = dialogView.findViewById(R.id.dialog_reply_profile_image_view);
        // 다이얼로그 댓글 내용 참조
        dialogReplyEditText = dialogView.findViewById(R.id.dialog_reply_edit_text);
        Log.d(TAG, "onClick: reply_profile_image_url: " + reply_profile_image_url);
        // 다이얼로그에서 전송중임을 나타내는 프로그레스 바
        dialogSendingProgress = dialogView.findViewById(R.id.dialog_reply_sending_progress);
        // 다이얼로그 댓글 입력란에 프로필 이미지 등록
        Glide.with(dialogProfileImage).load(reply_profile_image_url).apply(RequestOptions.circleCropTransform()).into(dialogProfileImage);
        // 수정 버튼을 클릭한 경우 댓글 입력란에 원래 댓글 내용 등록
        if (isModifyClicked) {
            Log.d(TAG, "alertReplyEditDialog: 수정 버튼이 클릭되어 EditText에 원래 댓글 내용을 보여줌");
            dialogReplyEditText.setText(selected_reply_content);
            // 커서가 글자 맨 뒤로 가게함
            dialogReplyEditText.setSelection(dialogReplyEditText.getText().length());
        } else {
            Log.d(TAG, "alertReplyEditDialog: 수정 버튼이 클릭되지 않아  EditText가 빈칸");
        }

        dialogReplyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: 텍스트 입력 전");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: 텍스트 입력 중");
                replySendButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: 텍스트 입력 후");
                String reply_text = dialogReplyEditText.getText().toString().trim();
                // 댓글 입력창이 공백이면(글을 썼다가 다시 지워서 공백이 될 경우) 전송 버튼을 안 보이게 함
                if (reply_text.isEmpty()) {
                    replySendButton.setVisibility(View.GONE);
                }

                // 댓글 수정중 이전과 같은 내용을 입력한 경우에도 전송 버튼이 안 보이게 됨
                if (isModifyClicked && dialogReplyEditText.getText().toString().equals(selected_reply_content)) {
                    replySendButton.setVisibility(View.GONE);
                }

            }
        });
        // 전송 버튼 클릭시
        replySendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: 댓글 전송 버튼 클릭");
                // EditText에 입력된 내용
                String content = dialogReplyEditText.getText().toString().trim();
                Log.d(TAG, "onClick: content: " + content);
                // 댓글 정보를 db에 전송
                sendReplyInfo(content, reply_adapter_position);
                // 키보드 내려감
//                keyBoradDown(dialogReplyEditText);
                // 전송중임을 알리는 프로그레스바를 보이게 하고 전송버튼을 안 보이게 함
                dialogSendingProgress.setVisibility(View.VISIBLE);
                replySendButton.setVisibility(View.GONE);


            }
        });

        // 키보드를 제어하게 하는 InputMethodManager
        InputMethodManager manager = null;
        manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInputFromInputMethod((dialogReplyEditText.getWindowToken()), InputMethodManager.SHOW_FORCED);

        dialog = builder.create();
        // 다이얼로그를 하단에 배치
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        // 다이얼로그가 나올때 키보드가 항상 나타나게 함
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
        Log.d(TAG, "alertReplyEditDialog: 입력 다이얼로그 나타남");
        // 다이얼로그가 종료되면 수정버튼을 클릭되지 않은 상태로 바꿈
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

                Log.d(TAG, "alertReplyEditDialog: 다이얼로그가 종료되어 isModifyClicked = false");
                isModifyClicked = false;
            }
        });


    }

    // 전체 댓글 갯수를 구하는 메소드
    private void getReplyCouont() {

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .getReplyCount(getSharedToken(), video_index);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 댓글 갯수 가져오기 성공");
                JsonObject data = response.body();
                Log.d(TAG, "onResponse: data: " + data);
                if (data != null) {
                    total_reply_count = data.get("total_reply_count").getAsInt();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 댓글 갯수 가져오기 실패");

            }
        });

    }

    // 딥링크 생성
    private Uri getDeepLink() {
        // ex) https://wooban.com/video?index=46
        String deep_link = "https://wooban.com/" + SEGMENT_VIDEO + "?index=" + video_index
                + "&reply_profile_image_url=" + reply_profile_image_url + "&reply_name=" + reply_name;
        Log.d(TAG, "getDeepLink: deep_link: " + deep_link);
        return Uri.parse(deep_link);
    }

    // https://your_subdomain.page.link/?link=your_deep_link
    // 동적 링크 생성
    private void onDynamicLinkClick() {
        Log.d(TAG, "onDynamicLinkClick: context.getPackageName(): " + context.getPackageName());
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(getDeepLink())
                .setDomainUriPrefix("https://wooban.page.link")
                // 안드로이드 앱에 대한 설정 125버전 아래의 앱을 사용중이면 앱을 실행시키지 않고 설치페이지로 보냄
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder(context.getPackageName()).build())
//                                .setMinimumVersion(125)
//                                .build())
                .setGoogleAnalyticsParameters(
                        new DynamicLink.GoogleAnalyticsParameters.Builder()
                                .setSource("orkut")
                                .setMedium("social")
                                .setCampaign("example-promo")
                                .build())
                .buildShortDynamicLink()
                .addOnCompleteListener(activity, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        Log.d(TAG, "onComplete: 동적 링크 생성");
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: 동적 링크 생성 성공");
                            Uri shortLink = task.getResult().getShortLink();
                            try {
                                Log.d(TAG, "onComplete:  트라이");
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                                sendIntent.setType("text/plain");
                                context.startActivity(Intent.createChooser(sendIntent, "공유하기"));
                            } catch (ActivityNotFoundException ignored) {
                                Log.d(TAG, "onComplete: 캐치");
                            }
                        } else {
                            Log.d(TAG, "onComplete: 동적 링크 생성 실패");
                            Log.w(TAG, task.toString());
                        }
                    }
                });
    }

    // 저장 버튼을 클릭할 경우 나중에 볼 영상에 대한 인덱스 번호를 db에 저장
    private void watchLaterVideoInfoAdd() {

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .watchLaterVideoInfoAdd(getSharedToken(), video_index);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 나중에 볼 영상 정보 저장 성공");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onFailure: 나중에 볼 영상 정보 저장 실패");

            }
        });
    }

    // 좋아요 또는 싫어요 표시를 db에 저장
    private void likeOrDislikeAdd(String like_or_dislike_para) {

        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .likeOrDislikeAdd(getSharedToken(), video_index, like_or_dislike_para);

        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 좋아요 or 싫어요 표시 저장 응답 성공");
                JsonObject data = response.body();
                Log.d(TAG, "onResponse: 받은 data: " + data);
                // 받은 값을 like_or_dislike에 저장
                if (data != null) {
                    JsonElement jsonElement = data.get("like_or_dislike_info");
                    like_or_dislike = jsonElement.getAsString();
                    Log.d(TAG, "onResponse: like_or_dislike: " + like_or_dislike);
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 좋아요 or 싫어요 표시 저장 응답 실패");

            }
        });
    }

    // 좋아요 또는 싫어요 상태를 가져옴
    private void getLikeOrDislikeStatus(final ImageView likeImageView, final ImageView dislikeImageView) {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .getlikeOrDislikeStatus(getSharedToken(), video_index);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 좋아요 or 싫어요 상태 가져오기 응답 성공");
                JsonObject data = response.body();
                Log.d(TAG, "onResponse: data: " + data);
                if (data != null) {
                    like_or_dislike = data.getAsJsonPrimitive("like_or_dislike_info").getAsString();
                    Log.d(TAG, "onResponse: like_or_dislike: " + like_or_dislike);
                    switch (like_or_dislike) {
                        case "like":
                            // 좋아요 버튼을 빨간색으로 바꿈
                            likeImageView.setColorFilter(Color.parseColor("#E53935"));
                            break;
                        case "dislike":
                            // 싫어요 버튼을 빨간색으로 바꿈
                            dislikeImageView.setColorFilter(Color.parseColor("#E53935"));
                            break;

                    }

                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 좋아요 or 싫어요 상태 가져오기 응답 실패");

            }
        });
    }

    private void setLikeOrDislikeCount(final TextView like_count, final TextView dislike_count, final String like_or_dislike_para, String plus_or_minus) {
        Call<JsonObject> call = RetrofitClient
                .getInstance()
                .getApi()
                .setlikeOrDislikeCount(getSharedToken(), video_index, like_or_dislike_para, plus_or_minus);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                Log.d(TAG, "onResponse: 좋아요 또는 싫어요 숫자 값 바꾸기 응답 성공");
                JsonObject data = response.body();
                // new_count = 새로운 좋아요 or 싫어요 값
                String new_count = data.get("new_" + like_or_dislike_para + "_count").getAsString();
                Log.d(TAG, "onResponse: new_count: " + new_count);
                if (like_or_dislike_para.equals("like")) {
                    like_count.setText(new_count);
                } else {
                    dislike_count.setText(new_count);
                }


            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(TAG, "onResponse: 좋아요 또는 싫어요 숫자 값 바꾸기 응답 실패");


            }
        });
    }
}
