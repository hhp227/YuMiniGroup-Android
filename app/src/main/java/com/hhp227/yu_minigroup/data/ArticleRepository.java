package com.hhp227.yu_minigroup.data;

import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

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
import com.hhp227.yu_minigroup.helper.Callback;
import com.hhp227.yu_minigroup.helper.DateUtil;
import com.hhp227.yu_minigroup.volley.util.MultipartRequest;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArticleRepository {
    private final String mGroupId, mGroupKey;

    private boolean mStopRequestMore = false;

    private long mMinId;

    public ArticleRepository(String groupId, String key) {
        this.mGroupId = groupId;
        this.mGroupKey = key;
    }

    public boolean isStopRequestMore() {
        return mStopRequestMore;
    }

    public void setMinId(long minId) {
        this.mMinId = minId;
    }

    public void getArticleList(String cookie, String params, Callback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response);
            List<Map.Entry<String, ArticleItem>> articleItemList = new ArrayList<>();

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
                    articleItemList.add(new AbstractMap.SimpleEntry<>(id, articleItem));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                initFirebaseData(articleItemList, callback);
            }
        }, error -> {
            callback.onFailure(error);
            VolleyLog.e(error.getMessage());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }
        };

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public void getArticleData(String cookie, String articleId, String articleKey, String params, Callback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response.trim());
            ArticleItem articleItem = new ArticleItem();

            try {
                Element element = source.getFirstElementByClass("listbox2");
                Element viewArt = element.getFirstElementByClass("view_art");
                Element commentWrap = element.getFirstElementByClass("comment_wrap");
                Element listCont = viewArt.getFirstElementByClass("list_cont");
                List<Element> commentList = element.getAllElementsByClass("comment-list");
                String listTitle = viewArt.getFirstElementByClass("list_title").getTextExtractor().toString();
                String title = listTitle.substring(0, listTitle.lastIndexOf("-")).trim();
                String name = listTitle.substring(listTitle.lastIndexOf("-") + 1).trim();
                String timeStamp = viewArt.getFirstElement(HTMLElementName.TD).getTextExtractor().toString();
                String content = contentExtractor(listCont);
                List<String> imageList = imageExtract(listCont);
                YouTubeItem youTubeItem = youtubeExtract(listCont);
                String replyCnt = commentWrap.getFirstElementByClass("commentBtn").getTextExtractor().toString();

                articleItem.setId(articleId);
                articleItem.setName(name);
                articleItem.setTitle(title);
                articleItem.setContent(content);
                articleItem.setImages(imageList);
                articleItem.setYoutube(youTubeItem);
                articleItem.setTimestamp(DateUtil.getTimeStamp(timeStamp));
                articleItem.setReplyCount(replyCnt);
                callback.onSuccess(commentList);
            } catch (Exception e) {
                callback.onFailure(e);
            } finally {
                fetchArticleDataFromFirebase(articleItem, articleKey, callback);
            }
        }, callback::onFailure) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", cookie);
                return headers;
            }
        };

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public void addArticle(String cookie, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.WRITE_ARTICLE, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean error = jsonObject.getBoolean("isError");

                if (!error) {
                    getArticleId(cookie, user, title, Html.fromHtml(content).toString().trim(), imageList, youTubeItem, callback);
                }
            } catch (JSONException e) {
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

                params.put("SBJT", title);
                params.put("CLUB_GRP_ID", mGroupId);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    public void setArticle(String cookie, String articleId, String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        String tagStringReq = "req_send";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.MODIFY_ARTICLE, response -> {
            try {
                initFirebaseData(articleKey, title, Html.fromHtml(content).toString().trim(), imageList, youTubeItem, callback);
            } catch (Exception e) {
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
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("ARTL_NUM", articleId);
                params.put("SBJT", title);
                params.put("TXT", content);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tagStringReq);
    }

    public void removeArticle(String cookie, String articleId, String articleKey, Callback callback) {
        String tag_string_req = "req_delete";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoint.DELETE_ARTICLE, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean error = jsonObject.getBoolean("isError");

                if (!error) {
                    deleteArticleFromFirebase(articleKey, callback);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

                params.put("CLUB_GRP_ID", mGroupId);
                params.put("ARTL_NUM", articleId);
                return params;
            }
        };

        callback.onLoading();
        AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
    }

    public void addArticleImage(String cookie, Bitmap bitmap, Callback callback) {
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, EndPoint.IMAGE_UPLOAD, response -> {
            String imageSrc = new String(response.data);
            imageSrc = EndPoint.BASE_URL + imageSrc.substring(imageSrc.lastIndexOf("/ilosfiles/"), imageSrc.lastIndexOf("\""));

            callback.onSuccess(imageSrc);
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
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                params.put("file", new DataPart(System.currentTimeMillis() + ".jpg", getFileDataFromDrawable(bitmap)));
                return params;
            }

            private byte[] getFileDataFromDrawable(Bitmap bitmap) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
                return byteArrayOutputStream.toByteArray();
            }
        };

        AppController.getInstance().addToRequestQueue(multipartRequest);
    }

    private void getArticleId(String cookie, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&displayL=1";

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.GROUP_ARTICLE_LIST + params, response -> {
            Source source = new Source(response);
            String artlNum = source.getFirstElementByClass("comment_wrap").getAttributeValue("num");

            insertArticleToFirebase(artlNum, user, title, content, imageList, youTubeItem, callback);
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
        });
    }

    private void initFirebaseData(List<Map.Entry<String, ArticleItem>> articleItemList, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        fetchArticleListFromFirebase(databaseReference.child(mGroupKey), articleItemList, callback);
    }

    private void initFirebaseData(String articleKey, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        updateArticleDataToFirebase(databaseReference.child(mGroupKey).child(articleKey), title, content, imageList, youTubeItem, callback);
    }

    private void fetchArticleListFromFirebase(Query query, List<Map.Entry<String, ArticleItem>> articleItemList, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    ArticleItem value = snapshot.getValue(ArticleItem.class);

                    if (value != null) {
                        int index = articleItemList.stream().map(Map.Entry::getKey).collect(Collectors.toList()).indexOf(value.getId());

                        if (index > -1) {
                            ArticleItem articleItem = articleItemList.get(index).getValue();

                            articleItem.setUid(value.getUid());
                            articleItemList.set(index, new AbstractMap.SimpleEntry<>(key, articleItem));
                        }
                    }
                }
                callback.onSuccess(articleItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
                Log.e("파이어베이스", databaseError.getMessage());
            }
        });
    }

    private void fetchArticleDataFromFirebase(final ArticleItem articleItem, String articleKey, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");

        databaseReference.child(mGroupKey).child(articleKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem value = dataSnapshot.getValue(ArticleItem.class);

                if (value != null) {
                    articleItem.setUid(value.getUid());
                }
                callback.onSuccess(articleItem);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void insertArticleToFirebase(String artlNum, User user, String title, String content, List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Articles");
        Map<String, Object> map = new HashMap<>();

        map.put("id", artlNum);
        map.put("uid", user.getUid());
        map.put("name", user.getName());
        map.put("title", title);
        map.put("timestamp", System.currentTimeMillis());
        map.put("content", TextUtils.isEmpty(content) ? null : content);
        map.put("images", imageList);
        map.put("youtube", youTubeItem);
        databaseReference.child(mGroupKey).push().setValue(map);
        callback.onSuccess(artlNum);
    }

    private void updateArticleDataToFirebase(final Query query, final String title, final String content, final List<String> imageList, YouTubeItem youTubeItem, Callback callback) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArticleItem articleItem = dataSnapshot.getValue(ArticleItem.class);

                if (articleItem != null) {
                    articleItem.setTitle(title);
                    articleItem.setContent(TextUtils.isEmpty(content) ? null : content);
                    articleItem.setImages(imageList.isEmpty() ? null : imageList);
                    articleItem.setYoutube(youTubeItem);
                    query.getRef().setValue(articleItem);
                    callback.onSuccess(articleItem);
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                callback.onFailure(databaseError.toException());
            }
        });
    }

    private void deleteArticleFromFirebase(String articleKey, Callback callback) {
        DatabaseReference articlesReference = FirebaseDatabase.getInstance().getReference("Articles");
        DatabaseReference replysReference = FirebaseDatabase.getInstance().getReference("Replys");

        articlesReference.child(mGroupKey).child(articleKey).removeValue();
        replysReference.child(articleKey).removeValue();
        callback.onSuccess(null);
    }

    private String contentExtractor(Element listCont) {
        StringBuilder sb = new StringBuilder();

        for (Element childElement : listCont.getChildElements()) {
            sb.append(childElement.getTextExtractor().toString().concat("\n"));
        }
        return sb.toString().trim();
    }

    private List<String> imageExtract(Element listCont) {
        List<String> result = new ArrayList<>();

        for (Element p : listCont.getAllElements(HTMLElementName.P)) {
            try {
                if (p.getFirstElement(HTMLElementName.IMG) != null) {
                    Element image = p.getFirstElement(HTMLElementName.IMG);
                    String imageUrl = !image.getAttributeValue("src").contains("http") ? EndPoint.BASE_URL + image.getAttributeValue("src") : image.getAttributeValue("src");

                    result.add(imageUrl);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private YouTubeItem youtubeExtract(Element listCont) {
        YouTubeItem youTubeItem = null;
        int position = 0;

        for (Element p : listCont.getAllElements(HTMLElementName.P)) {
            try {
                if (p.getFirstElement(HTMLElementName.IMG) != null) {
                    position++;
                } else if (p.getFirstElementByClass("youtube-player") != null) {
                    Element youtube = p.getFirstElementByClass("youtube-player");
                    String youtubeUrl = youtube.getAttributeValue("src");
                    String youtubeId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1, youtubeUrl.lastIndexOf("?"));
                    String thumbnail = "https://i.ytimg.com/vi/" + youtubeId + "/mqdefault.jpg";
                    youTubeItem = new YouTubeItem(youtubeId, null, null, thumbnail, null);
                    youTubeItem.position = position;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return youTubeItem;
    }
}
