package com.hhp227.yu_minigroup.data;

import static com.hhp227.yu_minigroup.app.EndPoint.GROUP_IMAGE;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.dto.User;
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.viewmodel.FindGroupViewModel;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GroupRepository {
    private boolean mStopRequestMore = false;

    private int mMinId;

    public GroupRepository() {
    }

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setMinId(int minId) {
        this.mMinId = minId;
    }

    public void getJoinedGroupList(String cookie, User user, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Map.Entry<String, Object>> groupItemList = new ArrayList<>();

            try {
                List<Element> listElementA = source.getAllElements(HTMLElementName.A);

                for (Element elementA : listElementA) {
                    try {
                        String id = groupIdExtract(elementA.getAttributeValue("onclick"), 3);
                        boolean isAdmin = adminCheck(elementA.getAttributeValue("onclick"));
                        String image = EndPoint.BASE_URL + elementA.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = elementA.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        GroupItem groupItem = new GroupItem();

                        groupItem.setId(id);
                        groupItem.setAdmin(isAdmin);
                        groupItem.setImage(image);
                        groupItem.setName(name);
                        groupItemList.add(new AbstractMap.SimpleEntry<>(id, groupItem));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                callback.onFailure(e);
            } finally {
                initFirebaseData(user.getUid(), insertAdvertisement(groupItemList), true, callback);
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

                params.put("panel_id", "2");
                params.put("start", "1");
                params.put("display", "10");
                params.put("encoding", "utf-8");
                return params;
            }
        });
    }

    public void getNotJoinedGroupList(String cookie, int offset, int limit, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Element> list = source.getAllElements("id", "accordion", false);
            List<Map.Entry<String, GroupItem>> groupItemList = new ArrayList<>();

            for (Element element : list) {
                try {
                    Element menuList = element.getFirstElementByClass("menu_list");

                    if (element.getAttributeValue("class").equals("accordion")) {
                        int id = groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"));
                        String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        StringBuilder info = new StringBuilder();
                        String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                        String joinType = menuList.getAllElementsByClass("info").get(1).getTextExtractor().toString().trim();
                        GroupItem groupItem = new GroupItem();
                        mMinId = mMinId == 0 ? id : Math.min(mMinId, id);

                        element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info").forEach(span -> {
                            String extractedText = span.getTextExtractor().toString();

                            info.append(extractedText.contains("회원수") ?
                                    extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                    extractedText + "\n");
                        });
                        if (id > mMinId) {
                            mStopRequestMore = true;
                            break;
                        } else
                            mStopRequestMore = false;
                        groupItem.setId(String.valueOf(id));
                        groupItem.setImage(imageUrl);
                        groupItem.setName(name);
                        groupItem.setInfo(info.toString().trim());
                        groupItem.setDescription(description);
                        groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                        groupItemList.add(new AbstractMap.SimpleEntry<>(String.valueOf(id), groupItem));
                    }
                } catch (Exception e) {
                    Log.e(FindGroupViewModel.class.getSimpleName(), e.getMessage());
                }
            }
            initFirebaseData(groupItemList, callback);
        }, callback::onFailure) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "1");
                params.put("gubun", "select_share_total");
                params.put("start", String.valueOf(offset));
                params.put("display", String.valueOf(limit));
                params.put("encoding", "utf-8");
                if (params.size() > 0) {
                    try {
                        return params.entrySet().stream().map(String::valueOf).collect(Collectors.joining("&")).getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                throw new RuntimeException();
            }
        });
    }

    public void getJoinRequestGroupList(String cookie, User user, int offset, int limit, Callback callback) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_LIST, response -> {
            Source source = new Source(response);
            List<Element> list = source.getAllElements("id", "accordion", false);
            List<Map.Entry<String, Object>> groupItemList = new ArrayList<>();

            for (Element element : list) {
                try {
                    Element menuList = element.getFirstElementByClass("menu_list");

                    if (element.getAttributeValue("class").equals("accordion")) {
                        int id = Integer.parseInt(groupIdExtract(menuList.getFirstElementByClass("button").getAttributeValue("onclick"), 1));
                        String imageUrl = EndPoint.BASE_URL + element.getFirstElement(HTMLElementName.IMG).getAttributeValue("src");
                        String name = element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString();
                        StringBuilder info = new StringBuilder();
                        String description = menuList.getAllElementsByClass("info").get(0).getContent().toString();
                        String joinType = menuList.getAllElementsByClass("info").get(1).getContent().toString();
                        GroupItem groupItem = new GroupItem();
                        mMinId = mMinId == 0 ? id : Math.min(mMinId, id);

                        for (Element span : element.getFirstElement(HTMLElementName.A).getAllElementsByClass("info")) {
                            String extractedText = span.getTextExtractor().toString();
                            info.append(extractedText.contains("회원수") ?
                                    extractedText.substring(0, extractedText.lastIndexOf("생성일")).trim() + "\n" :
                                    extractedText + "\n");
                        }
                        if (id > mMinId) {
                            mStopRequestMore = true;
                            break;
                        } else
                            mStopRequestMore = false;
                        groupItem.setId(String.valueOf(id));
                        groupItem.setImage(imageUrl);
                        groupItem.setName(name);
                        groupItem.setInfo(info.toString());
                        groupItem.setDescription(description);
                        groupItem.setJoinType(joinType.equals("가입방식: 자동 승인") ? "0" : "1");
                        groupItemList.add(new AbstractMap.SimpleEntry<>(String.valueOf(id), groupItem));
                    }
                } catch (Exception e) {
                    Log.e(GroupRepository.class.getSimpleName(), e.getMessage());
                }
            }
            initFirebaseData(user.getUid(), groupItemList, false, callback);
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
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("panel_id", "1");
                params.put("gubun", "select_share_total");
                params.put("start", String.valueOf(offset));
                params.put("display", String.valueOf(limit));
                params.put("encoding", "utf-8");
                if (params.size() > 0) {
                    try {
                        return params.entrySet().stream().map(String::valueOf).collect(Collectors.joining("&")).getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return null;
            }
        });
    }

    public void addGroup(String cookie, User user, Bitmap bitmap, String title, String description, String type, Callback callback) {
        callback.onLoading();
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.CREATE_GROUP, null, response -> {
            try {
                if (!response.getBoolean("isError")) {
                    String groupId = response.getString("CLUB_GRP_ID").trim();
                    String groupName = response.getString("GRP_NM");

                    if (bitmap != null)
                        groupImageUpdate(cookie, user, groupId, groupName, description, bitmap, type, callback);
                    else {
                        insertGroupToFirebase(user, groupId, groupName, description, null, type, callback);
                    }
                }
            } catch (JSONException e) {
                callback.onFailure(e);
            }
        }, callback::onFailure) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("GRP_NM", title);
                params.put("TXT", description);
                params.put("JOIN_DIV", type);
                if (params.size() > 0) {
                    try {
                        return params.entrySet().stream().map(String::valueOf).collect(Collectors.joining("&")).getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                return super.getBody();
            }
        });
    }

    public void setGroup() {

    }

    public void removeGroup(String cookie, User user, boolean isAdmin, String groupId, String key, Callback callback) {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, isAdmin ? EndPoint.DELETE_GROUP : EndPoint.WITHDRAWAL_GROUP, null, response -> {
            try {
                if (!response.getBoolean("isError")) {
                    deleteGroupFromFirebase(user, isAdmin, key, callback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure(e);
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
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
            }

            @Override
            public byte[] getBody() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", groupId);
                if (params.size() > 0) {
                    try {
                        return params.entrySet().stream().map(String::valueOf).collect(Collectors.joining("&")).getBytes(getParamsEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                    }
                }
                throw new RuntimeException();
            }
        });
    }

    private void groupImageUpdate(String cookie, User user, String groupId, String groupName, String description, Bitmap bitmap, String type, Callback callback) {
        AppController.getInstance().addToRequestQueue(new MultipartRequest(Request.Method.POST, EndPoint.GROUP_IMAGE_UPDATE, response -> insertGroupToFirebase(user, groupId, groupName, description, bitmap, type, callback), error -> {
            if (error.networkResponse.statusCode == 302) {
                // 임시로 넣은코드, 서버에서 왜 이런 응답을 보내는지 이해가 안된다.
                insertGroupToFirebase(user, groupId, groupName, description, bitmap, type, callback);
            } else {
                callback.onFailure(error);
            }
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

                params.put("CLUB_GRP_ID", groupId);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                if (bitmap != null) {
                    params.put("file", new DataPart(UUID.randomUUID().toString().replace("-", "").concat(".jpg"), getFileDataFromDrawable(bitmap)));
                }
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        });
    }

    private List<Map.Entry<String, Object>> insertAdvertisement(List<Map.Entry<String, Object>> groupItemList) {
        Map<String, String> headerMap = new HashMap<>();

        if (!groupItemList.isEmpty()) {
            headerMap.put("text", "가입중인 그룹");
            groupItemList.add(0, new AbstractMap.SimpleEntry<>("가입중인 그룹", headerMap));
            if (groupItemList.size() % 2 == 0) {
                groupItemList.add(new AbstractMap.SimpleEntry<>("광고", "광고"));
            }
        } else {
            groupItemList.add(new AbstractMap.SimpleEntry<>("없음", "없음"));
            headerMap.put("text", "인기 모임");
            groupItemList.add(new AbstractMap.SimpleEntry<>("인기 모임", headerMap));
            groupItemList.add(new AbstractMap.SimpleEntry<>("뷰페이져", "뷰페이져"));
        }
        return groupItemList;
    }

    private void initFirebaseData(String uid, List<Map.Entry<String, Object>> groupItemList, boolean isTrue, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("UserGroupList");

        fetchDataTaskFromFirebase(databaseReference.child(uid).orderByValue().equalTo(isTrue), false, groupItemList, callback);
    }

    private void initFirebaseData(List<Map.Entry<String, GroupItem>> groupItemList, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        fetchGroupListFromFireBase(databaseReference.orderByKey(), groupItemList, callback);
    }

    private void fetchDataTaskFromFirebase(Query query, final boolean isRecursion, List<Map.Entry<String, Object>> groupItemList, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isRecursion) {
                    try {
                        String key = dataSnapshot.getKey();
                        GroupItem value = dataSnapshot.getValue(GroupItem.class);

                        if (value != null) {
                            int index = groupItemList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(value.getId());

                            if (index > -1) {
                                Map.Entry<String, Object> entry = groupItemList.get(index);

                                groupItemList.set(index, new AbstractMap.SimpleEntry<>(key, entry.getValue()));
                            }
                        }
                    } catch (Exception e) {
                        callback.onFailure(e);
                    } finally {
                        callback.onSuccess(groupItemList);
                    }
                } else {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");
                            String key = snapshot.getKey();

                            if (key != null) {
                                fetchDataTaskFromFirebase(databaseReference.child(key), true, groupItemList, callback);
                            }
                        }
                    } else {
                        callback.onSuccess(groupItemList);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void fetchGroupListFromFireBase(Query query, List<Map.Entry<String, GroupItem>> groupItemList, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    GroupItem value = snapshot.getValue(GroupItem.class);

                    if (value != null) {
                        int index = groupItemList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(value.getId());

                        if (index > -1) {
                            GroupItem groupItem = groupItemList.get(index).getValue();

                            groupItemList.set(index, new AbstractMap.SimpleEntry<>(key, groupItem));
                        }
                    }
                }
                callback.onSuccess(groupItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void insertGroupToFirebase(User user, String groupId, String groupName, String description, Bitmap bitmap, String type, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Boolean> members = new HashMap<>();
        GroupItem groupItem = new GroupItem();
        Map<String, Object> childUpdates = new HashMap<>();
        String key = databaseReference.push().getKey();

        members.put(user.getUid(), true);
        groupItem.setId(groupId);
        groupItem.setTimestamp(System.currentTimeMillis());
        groupItem.setAuthor(user.getName());
        groupItem.setAuthorUid(user.getUid());
        groupItem.setImage(bitmap != null ? GROUP_IMAGE.replace("{FILE}", groupId.concat(".jpg")) : EndPoint.BASE_URL + "/ilos/images/community/share_nophoto.gif");
        groupItem.setName(groupName);
        groupItem.setDescription(description);
        groupItem.setJoinType(type);
        groupItem.setMembers(members);
        groupItem.setMemberCount(members.size());
        childUpdates.put("Groups/" + key, groupItem);
        childUpdates.put("UserGroupList/" + user.getUid() + "/" + key, true);
        databaseReference.updateChildren(childUpdates);
        callback.onSuccess(new AbstractMap.SimpleEntry<>(key, groupItem));
    }

    private void deleteGroupFromFirebase(User user, boolean isAdmin, String key, Callback callback) {
        DatabaseReference userGroupListReference = FirebaseDatabase.getInstance().getReference("UserGroupList");
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("Groups");

        if (isAdmin) {
            groupsReference.child(key).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getChildren().forEach(snapshot -> {
                        if (snapshot.getKey() != null) {
                            userGroupListReference.child(snapshot.getKey()).child(key).removeValue();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.toException());
                }
            });
            articlesReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");

                    dataSnapshot.getChildren().forEach(snapshot -> {
                        if (snapshot.getKey() != null) {
                            replysReference.child(snapshot.getKey()).removeValue();
                        }
                    });
                    articlesReference.child(key).removeValue();
                    groupsReference.child(key).removeValue();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.toException());
                }
            });
        } else {
            groupsReference.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                    if (groupItem != null) {
                        if (groupItem.getMembers() != null && groupItem.getMembers().containsKey(user.getUid())) {
                            Map<String, Boolean> members = groupItem.getMembers();

                            members.remove(user.getUid());
                            groupItem.setMembers(members);
                            groupItem.setMemberCount(members.size());
                        }
                    }
                    groupsReference.child(key).setValue(groupItem);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    callback.onFailure(databaseError.toException());
                }
            });
            userGroupListReference.child(user.getUid()).child(key).removeValue();
        }
        callback.onSuccess(true);
    }

    private String groupIdExtract(String href, int pos) {
        return href.split("'")[pos].trim();
    }

    private int groupIdExtract(String onclick) {
        return Integer.parseInt(onclick.split("[(]|[)]|[,]")[1].trim());
    }

    private boolean adminCheck(String onClick) {
        return onClick.split("'")[1].trim().equals("0");
    }
}
