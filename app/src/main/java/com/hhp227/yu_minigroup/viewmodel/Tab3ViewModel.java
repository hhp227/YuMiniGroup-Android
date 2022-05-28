package com.hhp227.yu_minigroup.viewmodel;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.MemberItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tab3ViewModel extends ViewModel {
    public final List<MemberItem> mMemberItems = new ArrayList<>();

    private static final int LIMIT = 40;

    private static final String TAG = Tab3ViewModel.class.getSimpleName(), STATE = "state";

    private final String mGroupId;

    private final SavedStateHandle mSavedStateHandle;

    public Tab3ViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");

        if (!mSavedStateHandle.contains(STATE)) {
            mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), 1, false, false, null));
            fetchNextPage();
        }
    }

    public LiveData<State> getState() {
        return mSavedStateHandle.getLiveData(STATE);
    }

    public void fetchMemberList(int offset) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startM=" + offset + "&displayM=" + LIMIT;

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MEMBER_LIST + params, response -> {
            List<MemberItem> memberItemList = new ArrayList<>();

            try {
                Source source = new Source(response);
                Element memberList = source.getElementById("member_list");

                // 페이징 처리
                String page = memberList.getFirstElementByClass("paging").getFirstElement("title", "현재 선택 목록", false).getTextExtractor().toString();
                List<Element> inputElements = memberList.getAllElements("name", "memberIdCheck", false);
                List<Element> imgElements = memberList.getAllElements("title", "프로필", false);
                List<Element> spanElements = memberList.getAllElements(HTMLElementName.SPAN);

                for (int i = 0; i < inputElements.size(); i++) {
                    String imageUrl = imgElements.get(i).getAttributeValue("src");
                    String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&ext"));
                    String name = spanElements.get(i).getContent().toString();
                    String value = inputElements.get(i).getAttributeValue("value");

                    memberItemList.add(new MemberItem(uid, name, value));
                }
                mSavedStateHandle.set(STATE, new State(false, memberItemList, ((State) mSavedStateHandle.get(STATE)).offset + LIMIT, false, memberItemList.isEmpty(), null));
            } catch (NullPointerException e) {
                e.printStackTrace();
                mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), ((State) mSavedStateHandle.get(STATE)).offset, false, false, null));
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), ((State) mSavedStateHandle.get(STATE)).offset, false, false, error.getMessage()));
        }));
    }

    public void fetchNextPage() {
        State state = mSavedStateHandle.get(STATE);

        if (state != null) {
            mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), state.offset, true, false, null));
        }
    }

    public void refresh() {
        mMemberItems.clear();
        mSavedStateHandle.set(STATE, new State(false, Collections.emptyList(), 1, true, false, null));
    }

    public void addAll(List<MemberItem> memberItemList) {
        mMemberItems.addAll(memberItemList);
    }

    public static final class State implements Parcelable {
        public boolean isLoading;

        public List<MemberItem> memberItems;

        public int offset;

        public boolean hasRequestedMore;

        public boolean isEndReached;

        public String message;

        public State(boolean isLoading, List<MemberItem> memberItems, int offset, boolean hasRequestedMore, boolean isEndReached, String message) {
            this.isLoading = isLoading;
            this.memberItems = memberItems;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.isEndReached = isEndReached;
            this.message = message;
        }

        protected State(Parcel in) {
            isLoading = in.readByte() != 0;
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
            parcel.writeInt(offset);
            parcel.writeByte((byte) (hasRequestedMore ? 1 : 0));
            parcel.writeByte((byte) (isEndReached ? 1 : 0));
            parcel.writeString(message);
        }
    }
}
