package com.hhp227.yu_minigroup.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.BbsItem;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class UnivNoticeViewModel extends ViewModel {
    private final MutableLiveData<State> mState = new MutableLiveData<>(new State(false, Collections.emptyList(), 0, false, null));

    private static final int MAX_PAGE = 100;

    private static final int ITEM_COUNT = 10;

    public UnivNoticeViewModel() {
        fetchNextPage();
    }

    public LiveData<State> getState() {
        return mState;
    }

    public void fetchNextPage() {
        if (mState.getValue() != null && mState.getValue().offset < MAX_PAGE) {
            mState.postValue(new State(false, mState.getValue().bbsItems, mState.getValue().offset, true, null));
        }
    }

    public void refresh() {
        Executors.newSingleThreadExecutor().execute(() -> mState.postValue(new State(false, Collections.emptyList(), 0, true, null)));
    }

    public void fetchDataList(int offset) {
        String tag_string_req = "req_yu_news";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_YU_NOTICE.replace("{MODE}", "list") + "&articleLimit={LIMIT}&article.offset={OFFSET}".replace("{LIMIT}", String.valueOf(ITEM_COUNT)).replace("{OFFSET}", String.valueOf(offset)), this::onResponse, this::onErrorResponse);

        if (mState.getValue() != null) {
            mState.postValue(new State(true, mState.getValue().bbsItems, offset, offset > 0, null));
        }
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    private void onResponse(String response) {
        Source source = new Source(response);
        List<BbsItem> itemList = new ArrayList<>();

        try {
            Element boardList = source.getFirstElementByClass("board-table");

            for (Element tr : boardList.getFirstElement(HTMLElementName.TBODY).getAllElements(HTMLElementName.TR)) {
                BbsItem bbsItem = new BbsItem();
                List<Element> tds = tr.getChildElements();
                String id = tds.get(1).getFirstElement(HTMLElementName.A).getAttributeValue("href").split("=|&")[3];
                String title = tds.get(1).getTextExtractor().toString();
                String writer = tds.get(2).getContent().toString();
                String date = tds.get(3).getContent().toString();

                bbsItem.setId(id);
                bbsItem.setTitle(title);
                bbsItem.setWriter(writer);
                bbsItem.setDate(date);
                itemList.add(bbsItem);
            }
            if (mState.getValue() != null) {
                mState.postValue(new State(false, mergedList(mState.getValue().bbsItems, itemList), mState.getValue().offset + ITEM_COUNT, false, null));
            }
        } catch (Exception e) {
            mState.postValue(new State(false, Collections.emptyList(), 0, false, e.getMessage()));
        }
    }

    private void onErrorResponse(VolleyError error) {
        mState.postValue(new State(false, Collections.emptyList(), 0, false, error.getMessage()));
    }

    private List<BbsItem> mergedList(List<BbsItem> existingList, List<BbsItem> newList) {
        List<BbsItem> result = new ArrayList<>();

        result.addAll(existingList);
        result.addAll(newList);
        return result;
    }

    public static final class State {
        public boolean isLoading;

        public List<BbsItem> bbsItems;

        public int offset;

        public boolean hasRequestedMore;

        public String message;

        public State(boolean isLoading, List<BbsItem> bbsItems, int offset, boolean hasRequestedMore, String message) {
            this.isLoading = isLoading;
            this.bbsItems = bbsItems;
            this.offset = offset;
            this.hasRequestedMore = hasRequestedMore;
            this.message = message;
        }
    }
}
