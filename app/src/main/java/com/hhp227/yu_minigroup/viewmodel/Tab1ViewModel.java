package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
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
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.dto.YouTubeItem;
import com.hhp227.yu_minigroup.helper.DateUtil;
import com.hhp227.yu_minigroup.helper.PreferenceManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tab1ViewModel extends ViewModel {
    public static boolean mIsAdmin;

    public static String mGroupId, mGroupName, mGroupImage, mKey;

    public final List<String> mArticleItemKeys = new ArrayList<>(Collections.singletonList(""));

    public final List<ArticleItem> mArticleItemValues = new ArrayList<>(Collections.singletonList(null));

    private static final int LIMIT = 10;

    private static final String STATE = "state";

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final SavedStateHandle mSavedStateHandle;

    private boolean mStopRequestMore = false;

    private long mMinId;

    public Tab1ViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mIsAdmin = savedStateHandle.get("admin");
        mGroupId = savedStateHandle.get("grp_id");
        mGroupName = savedStateHandle.get("grp_nm");
        mGroupImage = savedStateHandle.get("grp_img");
        mKey = savedStateHandle.get("key");

        mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), Collections.emptyList(), 1, false, false, null));
        fetchNextPage();
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public User getUser() {
        return mPreferenceManager.getUser();
    }

    public void fetchArticleList(int offset) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startL=" + offset + "&displayL=" + LIMIT;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response);
            List<String> articleItemKeys = new ArrayList<>();
            List<ArticleItem> articleItemValues = new ArrayList<>();

            try {
                List<Element> list = source.getAllElementsByClass("listbox2");

                for (Element element : list) {
                    Element viewArt = element.getFirstElementByClass("view_art");
                    Element commentWrap = element.getFirstElementByClass("comment_wrap");
                    boolean auth = viewArt.getAllElementsByClass("btn-small-gray").size() > 0;
                    String id = commentWrap.getAttributeValue("num");
                    String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                    String title = listTitle.substring(0, listTitle.lastIndexOf("-"));
                    String name = listTitle.substring(listTitle.lastIndexOf("-") + 1);
                    String date = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                    List<Element> images = viewArt.getAllElements(HTMLElementName.IMG);
                    StringBuilder content = new StringBuilder();
                    List<String> imageList = new ArrayList<>();
                    String replyCnt = commentWrap.getFirstElementByClass("commentBtn").getTextExtractor().toString(); // 댓글 + commentWrap.getFirstElementByClass("comment_cnt").getTextExtractor();
                    ArticleItem articleItem = new ArticleItem();
                    mMinId = mMinId == 0 ? Long.parseLong(id) : Math.min(mMinId, Long.parseLong(id));

                    if (images.size() > 0)
                        images.forEach(image -> imageList.add(!image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src")));
                    viewArt.getFirstElementByClass("list_cont").getChildElements().forEach(childElement -> content.append(childElement.getTextExtractor().toString().concat("\n")));
                    if (Long.parseLong(id) > mMinId) {
                        mStopRequestMore = true;
                        break;
                    } else
                        mStopRequestMore = false;
                    articleItem.setId(id);
                    articleItem.setTitle(title.trim());
                    articleItem.setName(name.trim());
                    articleItem.setTimestamp(DateUtil.getTimeStamp(date));
                    articleItem.setContent(content.toString().trim());
                    articleItem.setImages(imageList);
                    articleItem.setReplyCount(replyCnt);
                    articleItem.setAuth(auth);
                    if (viewArt.getFirstElementByClass("youtube-player") != null) {
                        String youtubeUrl = viewArt.getFirstElementByClass("youtube-player").getAttributeValue("src");
                        String youtubeId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1, youtubeUrl.lastIndexOf("?"));
                        String thumbnail = "https://i.ytimg.com/vi/" + youtubeId + "/mqdefault.jpg";
                        YouTubeItem youTubeItem = new YouTubeItem(youtubeId, null, null, thumbnail, null);

                        articleItem.setYoutube(youTubeItem);
                    }
                    articleItemKeys.add(id);
                    articleItemValues.add(articleItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                initFirebaseData(articleItemKeys, articleItemValues);
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), Collections.emptyList(), 1, false, false, error.getMessage()));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        };

        mSavedStateHandle.set(STATE, new State(true, Collections.emptyList(), Collections.emptyList(), offset, offset > 1, false, null));
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public void fetchNextPage() {
        State state = mSavedStateHandle.get(STATE);

        if (state != null && !mStopRequestMore) {
            mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), Collections.emptyList(), state.offset, true, false, null));
        }
    }

    public void refresh() {
        mMinId = 0;

        mArticleItemKeys.clear();
        mArticleItemValues.clear();
        mArticleItemKeys.add("");
        mArticleItemValues.add(null);
        mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), Collections.emptyList(), 1, true, false, null));
    }

    public void addAll(List<String> articleItemKeys, List<ArticleItem> articleItemValues) {
        if (articleItemKeys.size() == articleItemValues.size()) {
            mArticleItemKeys.addAll(mArticleItemKeys.size() - 1, articleItemKeys);
            mArticleItemValues.addAll(mArticleItemValues.size() - 1, articleItemValues);
        }
    }

    private void initFirebaseData(List<String> articleItemKeys, List<ArticleItem> articleItemValues) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        fetchArticleListFromFirebase(databaseReference.child(mKey), articleItemKeys, articleItemValues);
    }

    private void fetchArticleListFromFirebase(Query query, List<String> articleItemKeys, List<ArticleItem> articleItemValues) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);

                    if (value != null) {
                        int index = articleItemKeys.indexOf(value.getId());

                        if (index > -1) {
                            ArticleItem articleItem = articleItemValues.get(index);

                            articleItem.setUid(value.getUid());
                            articleItemKeys.set(index, key);
                            articleItemValues.set(index, articleItem);
                        }
                    }
                }
                if (mSavedStateHandle.get(STATE) != null) {
                    mSavedStateHandle.set(STATE, new State(false, articleItemKeys, articleItemValues, ((State) mSavedStateHandle.get(STATE)).offset + LIMIT, false, articleItemKeys.isEmpty() && articleItemValues.isEmpty(), null));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), Collections.emptyList(), 1, false, false, databaseError.getMessage()));
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public List<String> articleItemKeys;

        public List<ArticleItem> articleItemValues;

        public int offset;

        public boolean hasRequestedMore;

        public boolean isEndReached;

        public String message;

        public State(boolean isLoading, List<String> articleItemKeys, List<ArticleItem> articleItemValues, int offset, boolean hasRequestedMore, boolean isEndReached, String message) {
            this.isLoading = isLoading;
            this.articleItemKeys = articleItemKeys;
            this.articleItemValues = articleItemValues;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.isEndReached = isEndReached;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
            articleItemKeys = in.createStringArrayList();
            offset = in.readInt();
            hasRequestedMore = in.readByte() != 0;
            isEndReached = in.readByte() != 0;
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
            parcel.writeByte((byte) (isLoading ? 1 : 0));
            parcel.writeStringList(articleItemKeys);
            parcel.writeInt(offset);
            parcel.writeByte((byte) (hasRequestedMore ? 1 : 0));
            parcel.writeByte((byte) (isEndReached ? 1 : 0));
            parcel.writeString(message);
        }
    }
}
