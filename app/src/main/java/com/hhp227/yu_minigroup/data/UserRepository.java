package com.hhp227.yu_minigroup.data;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.dto.MemberItem;
import com.hhp227.yu_minigroup.helper.Callback;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {
    private String mGroupKey;

    private String mLastKey = null; // 마지막으로 가져온 데이터의 키

    private boolean mStopRequestMore = false;


    public UserRepository() {
    }

    public UserRepository(String mGroupKey) {
        this.mGroupKey = mGroupKey;
    }

    public void getManagedMemberList(String cookie, String groupId, Callback callback) {
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, EndPoint.GROUP_MEMBER_LIST, response -> {
            Source source = new Source(response);
            List<Element> listZone = source.getElementById("listZone").getChildElements();
            List<MemberItem> memberItemList = new ArrayList<>();

            for (Element element : listZone) {
                List<Element> tdList = element.getAllElements(HTMLElementName.TD);
                String studentNumber = tdList.get(0).getContent().getFirstElement().getAttributeValue("value");
                String imageUrl = tdList.get(1).getContent().getFirstElement().getAttributeValue("src");
                String uid = imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&ext"));
                String name = tdList.get(2).getContent().toString();
                String deptName = tdList.get(3).getTextExtractor().toString();
                String division = tdList.get(5).getContent().toString();
                String date = tdList.get(6).getContent().toString();
                MemberItem memberItem = new MemberItem(uid, name, null, studentNumber, deptName, division, date);

                memberItemList.add(memberItem);
            }
            callback.onSuccess(memberItemList);
        }, callback::onFailure) {
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
        });
    }

    public void getUserList(int limit, Callback callback) {

    }
}
