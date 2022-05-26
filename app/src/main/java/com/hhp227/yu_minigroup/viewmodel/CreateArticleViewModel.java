package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateArticleViewModel extends ViewModel {
    public final List<Object> mContents = new ArrayList<>();

    private static final String TAG = CreateArticleViewModel.class.getSimpleName(), STATE = "state", BITMAP = "bitmap";

    private List<String> mImageList;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private StringBuilder mMakeHtmlContents;

    private final String mGrpId, mGrpKey, mArtlNum, mArtlKey;

    private final SavedStateHandle mSavedStateHandle;

    public CreateArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGrpId = savedStateHandle.get("grp_id");
        mGrpKey = savedStateHandle.get("grp_key");
        mArtlNum = savedStateHandle.get("artl_num");
        mArtlKey = savedStateHandle.get("artl_key");
        mImageList = savedStateHandle.get("img");

        if (mImageList != null && mImageList.size() > 0) {
            mContents.addAll(mImageList);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        mSavedStateHandle.set(BITMAP, bitmap);
    }

    public LiveData<Bitmap> getBitmapState() {
        return mSavedStateHandle.getLiveData(BITMAP);
    }

    public void setYoutube(YouTubeItem youtubeItem) {
        mSavedStateHandle.set("vid", youtubeItem);
    }

    public LiveData<YouTubeItem> getYoutubeState() {
        return mSavedStateHandle.getLiveData("vid");
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ArticleFormState> getArticleFormState() {
        return mSavedStateHandle.getLiveData("articleFormState");
    }

    public <T> void addItem(T content) {
        mContents.add(content);
    }

    public <T> void addItem(int position, T content) {
        mContents.add(position, content);
    }

    public void removeItem(int position) {
        mContents.remove(position);
    }

    public void actionSend(Spannable spannableTitle, Spannable spannableContent) {
        String title = spannableTitle.toString();
        String content = Html.toHtml(spannableContent, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);

        if (!title.isEmpty() && !(TextUtils.isEmpty(content) && mContents.size() < 2)) {
            mMakeHtmlContents = new StringBuilder();
            mImageList = new ArrayList<>();

            mSavedStateHandle.set(STATE, new State(0, null, Collections.emptyList(), null));
            if (mContents.size() > 1) {
                int position = 1;

                itemTypeCheck(position, spannableTitle, spannableContent);
            } else {
                typeCheck(title, content);
            }
        } else {
            mSavedStateHandle.set("articleFormState", new ArticleFormState((title.isEmpty() ? "제목" : "내용") + "을 입력하세요."));
        }
    }

    private void typeCheck(String title, String content) {
        if (((int) mSavedStateHandle.get("type")) == 0) {
            actionCreate(title, content);
        } else {
            actionUpdate(title, content);
        }
    }

    private void actionCreate(final String title, final String content) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_ARTICLE, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean error = jsonObject.getBoolean("isError");

                if (!error) {
                    getArticleId(title, Html.fromHtml(content).toString().trim());
                }
            } catch (JSONException e) {
                Log.e(TAG, "에러 : " + e.getMessage());
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", mGrpId);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void actionUpdate(final String title, final String content) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, response -> {
            try {
                initFirebaseData(title, Html.fromHtml(content).toString().trim());
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), e.getMessage()));
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGrpId);
                params.put("ARTL_NUM", mArtlNum);
                params.put("SBJT", title);
                params.put("TXT", content);
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    private void getArticleId(String title, String content) {
        String params = "?CLUB_GRP_ID=" + mGrpId + "&displayL=1";

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response);
            String artlNum = source.getFirstElementByClass("comment_wrap").getAttributeValue("num");

            insertArticleToFirebase(artlNum, title, content);
        }, error -> {
            VolleyLog.e(error.getMessage());
            mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
    }

    private void uploadImage(final Spannable title, final Spannable content, final int position, final Bitmap bitmap) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, response -> {
            String imageSrc = new String(response.data);
            imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles/"), imageSrc.lastIndexOf("\""));

            uploadProcess(title, content, position, imageSrc, false);
        }, error -> {
            VolleyLog.e(error.getMessage());
            mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(System.currentTimeMillis() + position + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    private void uploadProcess(Spannable spannableTitle, Spannable spannableContent, int position, String imageUrl, boolean isYoutube) {
        if (!isYoutube)
            mImageList.add(imageUrl);
        mSavedStateHandle.set(STATE, new State((int) ((double) (position) / (mContents.size() - 1) * 100), null, Collections.emptyList(), null));
        try {
            String test = (isYoutube ? "<p><embed title=\"YouTube video player\" class=\"youtube-player\" autostart=\"true\" src=\"//www.youtube.com/embed/" + imageUrl + "?autoplay=1\"  width=\"488\" height=\"274\"></embed><p>" // 유튜브 태그
                    : ("<p><img src=\"" + imageUrl + "\" width=\"488\"><p>")) + (position < mContents.size() - 1 ? "<br>": "");

            mMakeHtmlContents.append(test);
            if (position < mContents.size() - 1) {
                position++;
                Thread.sleep(isYoutube ? 0 : 700);

                // 분기
                itemTypeCheck(position, spannableTitle, spannableContent);
            } else {
                String title = spannableTitle.toString();
                String content = (!TextUtils.isEmpty(spannableContent) ? Html.toHtml(spannableContent, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL) + "<p><br data-mce-bogus=\"1\"></p>" : "") + mMakeHtmlContents.toString();

                typeCheck(title, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), "이미지 업로드 실패: " + e.getMessage()));
        }
    }

    private void itemTypeCheck(int position, Spannable spannableTitle, Spannable spannableContent) {
        if (mContents.get(position) instanceof String) {
            String image = (String) mContents.get(position);

            uploadProcess(spannableTitle, spannableContent, position, image, false);
        } else if (mContents.get(position) instanceof Bitmap) {////////////// 리팩토링 요망
            Bitmap bitmap = (Bitmap) mContents.get(position);// 수정

            uploadImage(spannableTitle, spannableContent, position, bitmap); // 수정
        } else if (mContents.get(position) instanceof YouTubeItem) {
            YouTubeItem youTubeItem = (YouTubeItem) mContents.get(position);

            uploadProcess(spannableTitle, spannableContent, position, youTubeItem.videoId, true);
        }
    }

    private void insertArticleToFirebase(String artlNum, String title, String content) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Map<String, Object> map = new HashMap<>();

        map.put("id", artlNum);
        map.put("uid", mPreferenceManager.getUser().getUid());
        map.put("name", mPreferenceManager.getUser().getName());
        map.put("title", title);
        map.put("timestamp", System.currentTimeMillis());
        map.put("content", TextUtils.isEmpty(content) ? null : content);
        map.put("images", mImageList);
        map.put("youtube", getYoutubeState().getValue());
        databaseReference.child(mGrpKey).push().setValue(map);
        mSavedStateHandle.set(STATE, new State(-1, artlNum, Collections.emptyList(), "전송완료"));
    }

    private void initFirebaseData(String title, String content) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        updateArticleDataToFirebase(databaseReference.child(mGrpKey).child(mArtlKey), title, content);
    }

    private void updateArticleDataToFirebase(final Query query, final String title, final String content) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);

                if (articleItem != null) {
                    articleItem.setTitle(title);
                    articleItem.setContent(TextUtils.isEmpty(content) ? null : content);
                    articleItem.setImages(mImageList.isEmpty() ? null : mImageList);
                    articleItem.setYoutube(getYoutubeState().getValue());
                    query.getRef().setValue(articleItem);
                    mSavedStateHandle.set(STATE, new State(-1, articleItem.getId(), Collections.emptyList(), "수정완료"));
                } else {
                    mSavedStateHandle.set(STATE, new State(-1, "dummy", Collections.emptyList(), "수정완료"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), databaseError.getMessage()));
            }
        });
    }

    public static final class State implements Parcelable {
        public int progress;

        public String articleId;

        public List<Object> contents; // 현재는 사용안함

        public String message;

        public State(int progress, String articleId, List<Object> contents, String message) {
            this.progress = progress;
            this.articleId = articleId;
            this.contents = contents;
            this.message = message;
        }

        protected State(Parcel in) {
            progress = in.readInt();
            articleId = in.readString();
            message = in.readString();
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(progress);
            parcel.writeString(articleId);
            parcel.writeString(message);
        }
    }

    public static final class ArticleFormState implements Parcelable {
        public String message;

        public ArticleFormState(String message) {
            this.message = message;
        }

        protected ArticleFormState(Parcel in) {
            message = in.readString();
        }

        public static final Creator<ArticleFormState> CREATOR = new Creator<ArticleFormState>() {
            @Override
            public ArticleFormState createFromParcel(Parcel in) {
                return new ArticleFormState(in);
            }

            @Override
            public ArticleFormState[] newArray(int size) {
                return new ArticleFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(message);
        }
    }
}
