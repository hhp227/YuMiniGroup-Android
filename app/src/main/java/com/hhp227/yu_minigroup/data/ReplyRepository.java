package com.hhp227.yu_minigroup.data;

import android.text.Html;

import androidx.annotation.NonNull;

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
import com.hhp227.yu_minigroup.dto.ReplyItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReplyRepository {
    private final String mGroupId, mArticleId, mArticleKey;

    public ReplyRepository(String groupId, String articleId, String articleKey) {
        this.mGroupId = groupId;
        this.mArticleId = articleId;
        this.mArticleKey = articleKey;
    }

    public void getReplyList(List<Element> commentList, Callback callback) {
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
            e.printStackTrace();
            callback.onFailure(e);
        } finally {
            fetchReplyListFromFirebase(replyItemList, callback);
        }
    }

    public void addReply(String cookie, User user, String text, Callback callback) {
        String tag_string_req = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.INSERT_REPLY, response -> {
            Source source = new Source(response);
            List<Element> commentList = source.getAllElementsByClass("comment-list");

            try {
                insertReplyToFirebase(commentList, text, user, callback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            callback.onFailure(error);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
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

        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public void setReply(String cookie) {
        // TODO
    }

    public void removeReply(String cookie, String replyId, String replyKey, Callback callback) {
        String tag_string_req = "req_delete";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_REPLY, response -> {
            Source source = new Source(response);

            try {
                if (!response.contains("처리를 실패했습니다")) {
                    List<Element> commentList = source.getAllElementsByClass("comment-list");

                    deleteReplyFromFirebase(replyKey, commentList, callback);
                }
            } catch (Exception e) {
                callback.onFailure(e);
                e.printStackTrace();
            }
        }, error -> {
            VolleyLog.e(error.getMessage());
            callback.onFailure(error);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
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

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);

    }

    private void fetchReplyListFromFirebase(List<Map.Entry<String, ReplyItem>> replyItemList, Callback callback) {
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
                callback.onSuccess(replyItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void insertReplyToFirebase(List<Element> commentList, String text, User user, Callback callback) {
        String replyId = commentList.get(commentList.size() - 1).getFirstElementByClass("comment-addr").getAttributeValue("id").replace("cmt_txt_", "");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");
        ReplyItem replyItem = new ReplyItem();

        replyItem.setId(replyId);
        replyItem.setUid(user.getUid());
        replyItem.setName(user.getName());
        replyItem.setTimestamp(System.currentTimeMillis());
        replyItem.setReply(text);
        databaseReference.child(mArticleKey).push().setValue(replyItem);
        callback.onSuccess(commentList);
    }

    private void deleteReplyFromFirebase(String replyKey, List<Element> commentList, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Replys");

        databaseReference.child(mArticleKey).child(replyKey).removeValue();
        callback.onSuccess(commentList);
    }
}
