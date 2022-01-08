package com.hhp227.yu_minigroup.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.webkit.CookieManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.*;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentDefaultSettingBinding;
import com.hhp227.yu_minigroup.dto.GroupItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.hhp227.yu_minigroup.CreateActivity.CAMERA_CAPTURE_IMAGE_REQUEST_CODE;
import static com.hhp227.yu_minigroup.CreateActivity.CAMERA_PICK_IMAGE_REQUEST_CODE;

public class DefaultSettingFragment extends Fragment {
    private static String mGroupId, mGroupImage, mGroupKey;

    private boolean mJoinTypeCheck;

    private CookieManager mCookieManager;

    private TextWatcher mTextWatcher;

    private FragmentDefaultSettingBinding mBinding;

    public DefaultSettingFragment() {
    }

    public static DefaultSettingFragment newInstance(String grpId, String grpImg, String key) {
        DefaultSettingFragment fragment = new DefaultSettingFragment();
        Bundle args = new Bundle();

        args.putString("grp_id", grpId);
        args.putString("grp_img", grpImg);
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mGroupId = getArguments().getString("grp_id");
            mGroupImage = getArguments().getString("grp_img");
            mGroupKey = getArguments().getString("key");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentDefaultSettingBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCookieManager = AppController.getInstance().getCookieManager();
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.ivReset.setImageResource(s.length() > 0 ? R.drawable.ic_clear_black_24dp : R.drawable.ic_clear_gray_24dp);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        mBinding.ivGroupImage.setOnClickListener(v -> {
            registerForContextMenu(v);
            getActivity().openContextMenu(v);
            unregisterForContextMenu(v);
        });
        mBinding.etTitle.addTextChangedListener(mTextWatcher);
        mBinding.rgJointype.setOnCheckedChangeListener((group, checkedId) -> mJoinTypeCheck = checkedId != R.id.rb_auto);
        mBinding.ivReset.setOnClickListener(v -> mBinding.etTitle.setText(""));
        Glide.with(this)
                .load(mGroupImage)
                .apply(RequestOptions.errorOf(R.drawable.ic_launcher_background))
                .transition(DrawableTransitionOptions.withCrossFade(150))
                .into(mBinding.ivGroupImage);
        String params = "?CLUB_GRP_ID=" + mGroupId;

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MODIFY_GROUP + params, response -> {
            Source source = new Source(response);
            String title = source.getElementById("wrtGroup").getAttributeValue("value");
            String desc = source.getElementById("wrtExplain").getContent().toString();

            for (Element rbElement : source.getFirstElementByClass("radiobox").getAllElementsByClass("chktype"))
                if (rbElement.toString().contains("checked"))
                    mJoinTypeCheck = !rbElement.getAttributeValue("value").equals("0");
            mBinding.etTitle.setText(title);
            mBinding.etDescription.setText(desc);
            mBinding.rgJointype.check(!mJoinTypeCheck ? R.id.rb_auto : R.id.rb_check);
        }, error -> VolleyLog.e(error.getMessage())) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.etTitle.removeTextChangedListener(mTextWatcher);
        mBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modify, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            final String groupName = mBinding.etTitle.getText().toString();
            final String groupDescription = mBinding.etDescription.getText().toString();

            if (!TextUtils.isEmpty(groupName) && !TextUtils.isEmpty(groupDescription)) {
                String tagJsonReq = "req_send";

                AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, EndPoint.UPDATE_GROUP, null, response -> {
                    try {
                        if (!response.getBoolean("isError")) {
                            Intent intent = new Intent(getContext(), Tab4Fragment.class);

                            intent.putExtra("grp_nm", response.getString("GRP_NM"));
                            intent.putExtra("grp_desc", groupDescription);
                            intent.putExtra("join_div", !mJoinTypeCheck ? "0" : "1");
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                            Toast.makeText(getContext(), "소모임 변경 완료", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        initFirebaseData();
                    }
                }, error -> {
                    VolleyLog.e(error.getMessage());
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
                    }

                    @Override
                    public byte[] getBody() {
                        Map<String, String> params = new HashMap<>();

                        params.put("CLUB_GRP_ID", mGroupId);
                        params.put("GRP_NM", groupName);
                        params.put("TXT", groupDescription);
                        params.put("JOIN_DIV", !mJoinTypeCheck ? "0" : "1");
                        if (params.size() > 0) {
                            StringBuilder encodedParams = new StringBuilder();

                            try {
                                for (Map.Entry<String, String> entry : params.entrySet()) {
                                    encodedParams.append(URLEncoder.encode(entry.getKey(), getParamsEncoding()));
                                    encodedParams.append('=');
                                    encodedParams.append(URLEncoder.encode(entry.getValue(), getParamsEncoding()));
                                    encodedParams.append('&');
                                }
                                return encodedParams.toString().getBytes(getParamsEncoding());
                            } catch (UnsupportedEncodingException uee) {
                                throw new RuntimeException("Encoding not supported: " + getParamsEncoding(), uee);
                            }
                        }
                        return null;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();

                        headers.put("Cookie", mCookieManager.getCookie(EndPoint.LOGIN_LMS));
                        return headers;
                    }
                }, tagJsonReq);
            } else {
                mBinding.etTitle.setError(groupName.isEmpty() ? "그룹이름을 입력하세요." : null);
                mBinding.etDescription.setError(groupDescription.isEmpty() ? "그룹설명을 입력하세요." : null);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("이미지 선택");
        menu.add("카메라");
        menu.add("갤러리");
        menu.add("이미지 없음");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getTitle().toString()) {
            case "카메라":
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                break;
            case "갤러리":
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);

                galleryIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, CAMERA_PICK_IMAGE_REQUEST_CODE);
                break;
            case "이미지 없음":
                mBinding.ivGroupImage.setImageResource(R.drawable.add_photo);
                Bitmap bitmap = null;

                Toast.makeText(getContext(), "이미지 없음 선택", Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void initFirebaseData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Groups");

        updateGroupDataToFirebase(databaseReference.child(mGroupKey));
    }

    private void updateGroupDataToFirebase(final Query query) {
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GroupItem groupItem = dataSnapshot.getValue(GroupItem.class);

                    groupItem.setName(mBinding.etTitle.getText().toString());
                    groupItem.setDescription(mBinding.etDescription.getText().toString());
                    groupItem.setJoinType(!mJoinTypeCheck ? "0" : "1");
                    query.getRef().setValue(groupItem);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("파이어베이스", "파이어베이스 데이터 불러오기 실패", databaseError.toException());
            }
        });
    }
}
