package com.hhp227.yu_minigroup.viewmodel;

import static java.util.Objects.requireNonNull;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
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
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.data.ArticleRepository;
import com.hhp227.yu_minigroup.dto.ArticleItem;
import com.hhp227.yu_minigroup.dto.ReplyItem;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArticleViewModel extends ViewModel {
    public final Boolean mIsAuthorized;

    public final Integer mPosition;

    public final String mGroupId, mArticleId, mGroupName, mGroupImage, mGroupKey, mArticleKey;

    private static final String TAG = ArticleViewModel.class.getSimpleName(), STATE = "state", REPLY_FORM_STATE = "replyFormState", UPDATE_ARTICLE_STATE = "updateArticleState", ARTICLE = "article";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    private final ArticleRepository articleRepository;

    public ArticleViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");
        mGroupName = savedStateHandle.get("grp_nm");
        mGroupImage = savedStateHandle.get("grp_img");
        mArticleId = savedStateHandle.get("artl_num");
        mGroupKey = savedStateHandle.get("grp_key");
        mArticleKey = savedStateHandle.get("artl_key");
        mPosition = savedStateHandle.get("position");
        mIsAuthorized = savedStateHandle.get("auth");
        articleRepository = new ArticleRepository(mGroupId, mGroupKey);

        if (!mSavedStateHandle.contains(STATE)) {
            mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, null));
            fetchArticleData(mArticleId);
        }
    }

    public String getCookie() {
        return mCookieManager.getCookie(EndPoint.LOGIN_LMS);
    }

    public void setState(State state) {
        mSavedStateHandle.set(STATE, state);
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public LiveData<ReplyFormState> getReplyFormState() {
        return mSavedStateHandle.getLiveData(REPLY_FORM_STATE);
    }

    public void setUpdateArticleState(boolean bool) {
        mSavedStateHandle.set(UPDATE_ARTICLE_STATE, bool);
    }

    // TODO ArticleState가 업데이트되면 변경되는걸로 변경하기
    public LiveData<Boolean> getUpdateArticleState() {
        return mSavedStateHandle.getLiveData(UPDATE_ARTICLE_STATE);
    }

    public void setArticleState(ArticleItem articleItem) {
        mSavedStateHandle.set(ARTICLE, articleItem);
    }

    public LiveData<ArticleItem> getArticleState() {
        return mSavedStateHandle.getLiveData(ARTICLE);
    }

    public void setScrollToLastState(boolean bool) {
        mSavedStateHandle.set("isbottom", bool);
    }

    public LiveData<Boolean> getScrollToLastState() {
        return mSavedStateHandle.getLiveData("isbottom");
    }

    public void actionSend(String text) {
        if (text.length() > 0) {
            String tag_string_req = "req_send";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.INSERT_REPLY, response -> {
                Source source = new Source(response);
                List<Element> commentList = source.getAllElementsByClass("comment-list");

                try {
                    refreshReply(commentList);

                    // 전송할때마다 리스트뷰 아래로
                    setScrollToLastState(true);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    insertReplyToFirebase(commentList.get(commentList.size() - 1).getFirstElementByClass("comment-addr").getAttributeValue("id").replace("cmt_txt_", ""), text);
                }
            }, error -> {
                VolleyLog.e(TAG, error.getMessage());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, error.getMessage()));
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", getCookie());
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("CLUB_GRP_ID", mGroupId);
                    params.put("ARTL_NUM", mArticleId);
                    params.put("CMT", text);
                    return params;
                }
            };

            mSavedStateHandle.set(STATE, new State(true, ((State) requireNonNull(mSavedStateHandle.get(STATE))).articleItem, Collections.emptyList(), false, null));
            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        } else {
            mSavedStateHandle.set(REPLY_FORM_STATE, new ReplyFormState("댓글을 입력하세요."));
        }
    }

    public void deleteArticle() {
        articleRepository.removeArticle(getCookie(), mArticleId, mArticleKey, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                setState(new State(false, null, Collections.emptyList(), true, "삭제완료"));
            }

            @Override
            public void onFailure(Throwable throwable) {
                setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                setState(new State(true, null, Collections.emptyList(), false, null));
            }
        });
    }

    public void deleteReply(String replyId, String replyKey) {
        String tag_string_req = "req_delete";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_REPLY, response -> {
            Source source = new Source(response);

            try {
                if (!response.contains("처리를 실패했습니다")) {
                    List<Element> commentList = source.getAllElementsByClass("comment-list");

                    refreshReply(commentList);
                }
            } catch (Exception e) {
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, e.getMessage()));
                Log.e(TAG, e.getMessage());
            } finally {
                deleteReplyFromFirebase(replyKey);
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", getCookie());
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("CMMT_NUM", replyId);
                params.put("ARTL_NUM", mArticleId);
                return params;
            }
        };

        mSavedStateHandle.set(STATE, new State(true, ((State) requireNonNull(mSavedStateHandle.get(STATE))).articleItem, ((State) requireNonNull(mSavedStateHandle.get(STATE))).replyItemList, false, null));
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public void refresh() {
        fetchArticleData(mArticleId);
    }

    public void refreshReply(List<Element> commentList) {
        fetchReplyData(commentList);
    }

    private void fetchArticleData(String articleId) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + mPosition + "&displayL=1";

        articleRepository.getArticleData(getCookie(), articleId, mArticleKey, params, new Callback() {
            @Override
            public <T> void onSuccess(T data) {
                State state = mSavedStateHandle.get(STATE);

                if (data instanceof ArticleItem) {
                    ArticleItem articleItem = (ArticleItem) data;

                    if (state != null) {
                        setState(new State(false, articleItem, state.replyItemList, false, null));
                    }
                } else {
                    refreshReply((List<Element>) data);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                setState(new State(false, null, Collections.emptyList(), false, throwable.getMessage()));
            }

            @Override
            public void onLoading() {
                State state = mSavedStateHandle.get(STATE);

                if (state != null) {
                    setState(new State(true, null, state.replyItemList, false, null));
                }
            }
        });
    }

    private void fetchReplyData(List<Element> commentList) {
        List<Map.Entry<String, ReplyItem>> replyItemList = new ArrayList<>();

        try {
            for (Element comment : commentList) {
                Element commentName = comment.getFirstElementByClass("comment-name");
                Element commentAddr = comment.getFirstElementByClass("comment-addr");
                String replyId = commentAddr.getAttributeValue("id").replace("cmt_txt_", "");
                String name = commentName.getTextExtractor().toString().trim();
                String timeStamp = commentName.getFirstElement(HTMLElementName.SPAN).getContent().toString().trim();
                String replyContent = commentAddr.getContent().toString().trim();
                boolean authorization = commentName.getAllElements(HTMLElementName.INPUT).size() > 0;
                ReplyItem replyItem = new ReplyItem();

                replyItem.setId(replyId);
                replyItem.setName(name.substring(0, name.lastIndexOf("(")));
                replyItem.setReply(Html.fromHtml(replyContent).toString());
                replyItem.setDate(timeStamp.replaceAll("[(]|[)]", ""));
                replyItem.setAuth(authorization);
                replyItemList.add(new AbstractMap.SimpleEntry<>(replyId, replyItem));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, e.getMessage()));
        } finally {
            fetchReplyListFromFirebase(replyItemList);
        }
    }

    private void fetchReplyListFromFirebase(List<Map.Entry<String, ReplyItem>> replyItemList) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        databaseReference.child(mArticleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ReplyItem value = snapshot.getValue(ReplyItem.class);

                    if (value != null) {
                        int index = replyItemList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(value.getId());

                        if (index > -1) {
                            Map.Entry<String, ReplyItem> entry = replyItemList.get(index);
                            ReplyItem replyItem = entry.getValue();

                            replyItem.setUid(value.getUid());
                            replyItemList.set(index, new AbstractMap.SimpleEntry<>(key, replyItem));
                        }
                    }
                }
                mSavedStateHandle.set(STATE, new State(false, null, replyItemList, false, null));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "파이어베이스 데이터 불러오기 실패", databaseError.toException());
                mSavedStateHandle.set(STATE, new State(false, null, Collections.emptyList(), false, databaseError.getMessage()));
            }
        });
    }

    private void insertReplyToFirebase(String replyId, String text) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        ReplyItem replyItem = new ReplyItem();

        replyItem.setId(replyId);
        replyItem.setUid(mPreferenceManager.getUser().getUid());
        replyItem.setName(mPreferenceManager.getUser().getName());
        replyItem.setTimestamp(System.currentTimeMillis());
        replyItem.setReply(text);
        databaseReference.child(mArticleKey).push().setValue(replyItem);
    }

    private void deleteReplyFromFirebase(String replyKey) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        databaseReference.child(mArticleKey).child(replyKey).removeValue();
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public ArticleItem articleItem;

        public List<Map.Entry<String, ReplyItem>> replyItemList;

        public boolean isSetResultOK;

        public String message;

        public State(boolean isLoading, ArticleItem articleItem, List<Map.Entry<String, ReplyItem>> replyItemList, boolean isSetResultOK, String message) {
            this.isLoading = isLoading;
            this.articleItem = articleItem;
            this.replyItemList = replyItemList;
            this.isSetResultOK = isSetResultOK;
            this.message = message;
        }

        private State(Parcel in) {
            isLoading = in.readByte() != 0;
            isSetResultOK = in.readByte() != 0;
            message = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (isLoading ? 1 : 0));
            dest.writeByte((byte) (isSetResultOK ? 1 : 0));
            dest.writeString(message);
        }

        @Override
        public int describeContents() {
            return 0;
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
    }

    public static final class ReplyFormState implements Parcelable {
        public String replyError;

        public ReplyFormState(String replyError) {
            this.replyError = replyError;
        }

        private ReplyFormState(Parcel in) {
            replyError = in.readString();
        }

        public static final Creator<ReplyFormState> CREATOR = new Creator<ReplyFormState>() {
            @Override
            public ReplyFormState createFromParcel(Parcel in) {
                return new ReplyFormState(in);
            }

            @Override
            public ReplyFormState[] newArray(int size) {
                return new ReplyFormState[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(replyError);
        }
    }
}
