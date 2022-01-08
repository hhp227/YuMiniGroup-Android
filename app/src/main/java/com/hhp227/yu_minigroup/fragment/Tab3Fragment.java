package com.hhp227.yu_minigroup.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.adapter.MemberGridAdapter;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import com.hhp227.yu_minigroup.databinding.FragmentTab3Binding;
import com.hhp227.yu_minigroup.dto.MemberItem;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.ArrayList;
import java.util.List;

public class Tab3Fragment extends Fragment {
    private static final int LIMIT = 40;

    private static final String TAG = "멤버목록";

    private boolean mHasRequestedMore;

    private int mOffSet;

    private String mGroupId;

    private List<MemberItem> mMemberItems;

    private MemberGridAdapter mAdapter;

    private FragmentTab3Binding mBinding;

    public Tab3Fragment() {
    }

    public static Tab3Fragment newInstance(String grpId) {
        Tab3Fragment fragment = new Tab3Fragment();
        Bundle args = new Bundle();

        args.putString("grp_id", grpId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mGroupId = getArguments().getString("grp_id");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab3Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mMemberItems = new ArrayList<>();
        mAdapter = new MemberGridAdapter(mMemberItems);
        mOffSet = 1;

        mAdapter.setHasStableIds(true);
        mAdapter.setOnItemClickListener((v, position) -> {
            MemberItem memberItem = mMemberItems.get(position);
            String uid = memberItem.uid;
            String name = memberItem.name;
            String value = memberItem.value;
            Bundle args = new Bundle();
            UserDialogFragment newFragment = UserDialogFragment.newInstance();

            args.putString("uid", uid);
            args.putString("name", name);
            args.putString("value", value);
            newFragment.setArguments(args);
            newFragment.show(getChildFragmentManager(), "dialog");
        });
        mBinding.rvMember.setLayoutManager(layoutManager);
        mBinding.rvMember.setAdapter(mAdapter);
        mBinding.rvMember.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mHasRequestedMore && !recyclerView.canScrollVertically(1)) {
                    mHasRequestedMore = true;
                    mOffSet += LIMIT;

                    fetchMemberList();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mBinding.srlMember.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            mMemberItems.clear();
            mOffSet = 1;

            fetchMemberList();
            mBinding.srlMember.setRefreshing(false);
        }, 1000));
        showProgressBar();
        fetchMemberList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            mAdapter.notifyDataSetChanged();
    }

    private void fetchMemberList() {
        String params = "?CLUB_GRP_ID=" + mGroupId + "&startM=" + mOffSet + "&displayM=" + LIMIT;

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.MEMBER_LIST + params, response -> {
            try {
                Source source = new Source(response);
                Element memberList = source.getElementById("member_list");

                // 페이징 처리
                String page = memberList.getFirstElementByClass("paging").getFirstElement("title", "현재 선택 목록", false).getTextExtractor().toString();
                List<Element> inputElements = memberList.getAllElements("name", "memberIdCheck", false);
                List<Element> imgElements = memberList.getAllElements("title", "프로필", false);
                List<Element> spanElements = memberList.getAllElements(HTMLElementName.SPAN);

                for (int i = 0; i < inputElements.size(); i++) {
                    String name = spanElements.get(i).getContent().toString();
                    String imageUrl = imgElements.get(i).getAttributeValue("src");
                    String value = inputElements.get(i).getAttributeValue("value");

                    mMemberItems.add(new MemberItem(imageUrl.substring(imageUrl.indexOf("id=") + "id=".length(), imageUrl.lastIndexOf("&ext")), name, value));
                }
                mAdapter.notifyDataSetChanged();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mHasRequestedMore = false;

            hideProgressBar();
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            hideProgressBar();
        }));
    }

    private void showProgressBar() {
        if (mBinding.pbMember.getVisibility() == View.INVISIBLE)
            mBinding.pbMember.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mBinding.pbMember.getVisibility() == View.VISIBLE)
            mBinding.pbMember.setVisibility(View.INVISIBLE);
    }
}
