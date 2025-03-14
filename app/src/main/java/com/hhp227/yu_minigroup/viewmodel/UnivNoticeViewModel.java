package com.hhp227.yu_minigroup.viewmodel;

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

public class UnivNoticeViewModel extends ListViewModel<BbsItem> {
    private static final int MAX_PAGE = 100;

    private static final int ITEM_COUNT = 10;

    public UnivNoticeViewModel() {
        fetchNextPage();
    }

    public void fetchNextPage() {
        if (getOffset() < MAX_PAGE) {
            setRequestMore(true);
            fetchDataList(getOffset());
        }
    }

    public void refresh() {
        Executors.newSingleThreadExecutor().execute(() -> {
            setItemList(Collections.emptyList());
            setOffset(0);
            setRequestMore(true);
            setEndReached(false);
            fetchDataList(getOffset());
        });
    }

    public void fetchDataList(int offset) {
        String tag_string_req = "req_yu_news";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.URL_YU_NOTICE.replace("{MODE}", "list") + "&articleLimit={LIMIT}&article.offset={OFFSET}".replace("{LIMIT}", String.valueOf(ITEM_COUNT)).replace("{OFFSET}", String.valueOf(offset)), this::onResponse, this::onErrorResponse);

        setLoading(true);
        setRequestMore(offset > 1);
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
            setLoading(false);
            setItemList(mergedList(getItemList().getValue(), itemList));
            setOffset(getOffset() + ITEM_COUNT);
        } catch (Exception e) {
            setLoading(false);
            setMessage(e.getMessage());
        }
    }

    private void onErrorResponse(VolleyError error) {
        setLoading(false);
        setMessage(error.getMessage());
    }

    private List<BbsItem> mergedList(List<BbsItem> existingList, List<BbsItem> newList) {
        return new ArrayList<BbsItem>() {
            {
                addAll(existingList);
                addAll(newList);
            }
        };
    }
}