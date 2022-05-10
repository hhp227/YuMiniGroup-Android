package com.hhp227.yu_minigroup.viewmodel;

import static com.hhp227.yu_minigroup.app.EndPoint.GROUP_IMAGE;

import android.graphics.Bitmap;
import android.webkit.CookieManager;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.GroupItem;
import com.hhp227.yu_minigroup.helper.PreferenceManager;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateGroupViewModel extends ViewModel {
    public final MutableLiveData<State> mState = new MutableLiveData<>();

    public final MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

    private final PreferenceManager mPreferenceManager = AppController.getInstance().getPreferenceManager();

    private final CookieManager mCookieManager = AppController.getInstance().getCookieManager();

    private String mType;

    public void setBitmap(Bitmap bitmap) {
        mBitmap.postValue(bitmap);
    }

    public void setJoinType(boolean joinType) {
        this.mType = !joinType ? "0" : "1";
    }

    public void createGroup(String title, String description) {
        if (!title.isEmpty() && !description.isEmpty()) {
            mState.postValue(new State(true, null, null, null));
            AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.CREATE_GROUP, null, response -> {
                try {
                    if (!response.getBoolean("isError")) {
                        String groupId = response.getString("CLUB_GRP_ID").trim();
                        String groupName = response.getString("GRP_NM");
                        Bitmap bitmap = mBitmap.getValue();

                        if (bitmap != null)
                            groupImageUpdate(groupId, groupName, description, bitmap);
                        else {
                            insertGroupToFirebase(groupId, groupName, description, null);
                        }
                    }
                } catch (JSONException e) {
                    mState.postValue(new State(false, null, null, e.getMessage()));
                }
            }, error -> mState.postValue(new State(false, null, null, error.getMessage()))) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();

                    headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
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
                    params.put("JOIN_DIV", mType);
                    if (params.size() > 0) {
                        StringBuilder encodedParams = new StringBuilder();

                        try {
                            params.forEach((k, v) -> {
                                try {
                                    encodedParams.append(URLEncoder.encode(k, getParamsEncoding()));
                                    encodedParams.append('=');
                                    encodedParams.append(URLEncoder.encode(v, getParamsEncoding()));
                                    encodedParams.append("&");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            });
                            return encodedParams.toString().getBytes(getParamsEncoding());
                        } catch (UnsupportedEncodingException uee) {
                            throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                        }
                    }
                    return super.getBody();
                }
            });
        } else {
            mState.postValue(new State(false, null, new CreateGroupFormState(title.isEmpty() ? "그룹명을 입력하세요." : null, description.isEmpty() ? "그룹설명을 입력하세요." : null), null));
        }
    }

    private void groupImageUpdate(final String clubGrpId, final String grpNm, final String txt, final Bitmap bitmap) {
        AppController.getInstance().addToRequestQueue(new MultipartRequest(Request.Method.POST, EndPoint.GROUP_IMAGE_UPDATE, response -> insertGroupToFirebase(clubGrpId, grpNm, txt, bitmap), error -> mState.postValue(new State(false, null, null, error.getMessage()))) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", clubGrpId);
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

    private void insertGroupToFirebase(String groupId, String groupName, String description, Bitmap bitmap) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Boolean> members = new HashMap<>();
        GroupItem groupItem = new GroupItem();
        Map<String, Object> childUpdates = new HashMap<>();
        String key = databaseReference.push().getKey();

        members.put(mPreferenceManager.getUser().getUid(), true);
        groupItem.setId(groupId);
        groupItem.setTimestamp(System.currentTimeMillis());
        groupItem.setAuthor(mPreferenceManager.getUser().getName());
        groupItem.setAuthorUid(mPreferenceManager.getUser().getUid());
        groupItem.setImage(bitmap != null ? GROUP_IMAGE.replace("{FILE}", groupId.concat(".jpg")) : EndPoint.BASE_URL + "/ilos/images/community/share_nophoto.gif");
        groupItem.setName(groupName);
        groupItem.setDescription(description);
        groupItem.setJoinType(mType);
        groupItem.setMembers(members);
        groupItem.setMemberCount(members.size());
        childUpdates.put("Groups/" + key, groupItem);
        childUpdates.put("UserGroupList/" + mPreferenceManager.getUser().getUid() + "/" + key, true);
        databaseReference.updateChildren(childUpdates);
        mState.postValue(new State(false, new AbstractMap.SimpleEntry<>(key, groupItem), null, null));
    }

    public static final class State {
        public boolean isLoading;

        public Map.Entry<String, GroupItem> groupItemEntry;

        public CreateGroupFormState createGroupFormState;

        public String message;

        public State(boolean isLoading, Map.Entry<String, GroupItem> groupItemEntry, CreateGroupFormState createGroupFormState, String message) {
            this.isLoading = isLoading;
            this.groupItemEntry = groupItemEntry;
            this.createGroupFormState = createGroupFormState;
            this.message = message;
        }
    }

    public static final class CreateGroupFormState {
        public String titleError;

        public String descriptionError;

        public CreateGroupFormState(String titleError, String descriptionError) {
            this.titleError = titleError;
            this.descriptionError = descriptionError;
        }
    }
}
