package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
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
    private static final int LIMIT = 40;

    private static final String TAG = Tab3ViewModel.class.getSimpleName(), LOADING = "loading", OFFSET = "offset", REQUEST_MORE = "requestMore", END_REACHED = "endReached", MESSAGE = "message";

    private final MutableLiveData<List<MemberItem>> mItemList = new MutableLiveData<>(Collections.emptyList());

    private int offset = 1;

    private final String mGroupId;

    private final SavedStateHandle mSavedStateHandle;

    public Tab3ViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupId = savedStateHandle.get("grp_id");

        setLoading(false);
        setOffset(1);
        setRequestMore(false);
        setEndReached(false);
        fetchNextPage();
    }

    public void setLoading(boolean bool) {
        mSavedStateHandle.set(LOADING, bool);
    }

    public LiveData<Boolean> isLoading() {
        return mSavedStateHandle.getLiveData(LOADING);
    }

    public void setItemList(List<MemberItem> list) {
        mItemList.postValue(list);
    }

    public LiveData<List<MemberItem>> getItemList() {
        return mItemList;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int value) {
        offset = value;
    }

    public void setRequestMore(boolean bool) {
        mSavedStateHandle.set(REQUEST_MORE, bool);
    }

    public LiveData<Boolean> hasRequestMore() {
        return mSavedStateHandle.getLiveData(REQUEST_MORE);
    }

    public void setEndReached(boolean bool) {
        mSavedStateHandle.set(END_REACHED, bool);
    }

    public LiveData<Boolean> isEndReached() {
        return mSavedStateHandle.getLiveData(END_REACHED);
    }

    public void setMessage(String message) {
        mSavedStateHandle.set(MESSAGE, message);
    }

    public LiveData<String> getMessage() {
        return mSavedStateHandle.getLiveData(MESSAGE);
    }

    public void fetchMemberList(int offset) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startM=" + offset + "&displayM=" + LIMIT;

        setLoading(true);
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
                setLoading(false);
                setItemList(mergedList(getItemList().getValue(), memberItemList));
                setOffset(getOffset() + LIMIT);
                setEndReached(memberItemList.isEmpty());
            } catch (NullPointerException e) {
                e.printStackTrace();
                setLoading(false);
            }
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            setLoading(false);
            setMessage(error.getMessage());
        }));
    }

    public void fetchNextPage() {
        setRequestMore(true);
        fetchMemberList(getOffset());
    }

    public void refresh() {
        setRequestMore(true);
        setItemList(Collections.emptyList());
        setOffset(1);
        setEndReached(false);
        fetchMemberList(getOffset());
    }

    private List<MemberItem> mergedList(List<MemberItem> existingList, List<MemberItem> newList) {
        return new ArrayList<MemberItem>() {
            {
                addAll(existingList);
                addAll(newList);
            }
        };
    }
}