package com.hhp227.yu_minigroup.viewmodel;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.ArticleRepository;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateArticleViewModel extends ViewModel {
    public final List<Object> mContents = new ArrayList<>();

    private static final String STATE = "state", BITMAP = "bitmap";

    private List<String> mImageList;

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private StringBuilder mMakeHtmlContents;

    private final String mGrpId, mGrpKey, mArtlNum, mArtlKey;

    private final SavedStateHandle mSavedStateHandle;

    private final ArticleRepository articleRepository;

    public CreateArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGrpId = savedStateHandle.get("grp_id");
        mGrpKey = savedStateHandle.get("grp_key");
        mArtlNum = savedStateHandle.get("artl_num");
        mArtlKey = savedStateHandle.get("artl_key");
        mImageList = savedStateHandle.get("img");
        articleRepository = new ArticleRepository(mGrpId, mGrpKey);

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
        articleRepository.addArticle(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mPreferenceManager.getUser(), title, content, mImageList, getYoutubeState().getValue(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                mSavedStateHandle.set(STATE, new State(-1, (String) data, Collections.emptyList(), "전송완료"));
            }

            @Override
            public void onFailure(Throwable throwable) {
                mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mSavedStateHandle.set(STATE, new State(0, null, Collections.emptyList(), null));
            }
        });
    }

    private void actionUpdate(final String title, final String content) {
        articleRepository.setArticle(mCookieManager.getCookie(EndPoint.LOGIN_LMS), mArtlNum, mArtlKey, title, content, mImageList, getYoutubeState().getValue(), new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                if (data != null) {
                    ArticleItem articleItem = (ArticleItem) data;

                    mSavedStateHandle.set(STATE, new State(-1, articleItem.getId(), Collections.emptyList(), "수정완료"));
                } else {
                    mSavedStateHandle.set(STATE, new State(-1, "dummy", Collections.emptyList(), "수정완료"));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                mSavedStateHandle.set(STATE, new State(-1, null, Collections.emptyList(), throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                mSavedStateHandle.set(STATE, new State(0, null, Collections.emptyList(), null));
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

        private State(Parcel in) {
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

        private ArticleFormState(Parcel in) {
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
