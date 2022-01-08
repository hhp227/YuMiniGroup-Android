package com.hhp227.yu_minigroup.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.app.EndPoint;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

// TODO viewBinding 으로 이전할것

public class SemesterTimeTableFragment extends Fragment {
    private static final int ROW = 26;

    private static final int COL = 6;

    private LinearLayout mLayout;

    private ProgressBar mProgressBar;

    public SemesterTimeTableFragment() {
    }

    public static SemesterTimeTableFragment newInstance() {
        return new SemesterTimeTableFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timetable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout cardView = view.findViewById(R.id.ll_timetable);
        mProgressBar = view.findViewById(R.id.pb_group);

        showProgressBar();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.TIMETABLE, response -> {
            Element timeTable = new Source(response).getFirstElementByClass("bbslist");
            List<Element> list = timeTable.getAllElements(HTMLElementName.TR);
            AtomicInteger atomInt = new AtomicInteger();

            list.stream().limit(ROW).forEach(element -> {
                List<Element> schedule = element.getChildElements();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)
                params.width = getLcdSizeWidth() / 6;
                params.height = getLcdSizeHeight() / (atomInt.get() == 0 ? 20 : 14);
                params.setMargins(1, 1, 1, 1);
                params.gravity = 1;
                mLayout = new LinearLayout(getContext());

                mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                mLayout.setGravity(Gravity.LEFT);
                mLayout.setOrientation(LinearLayout.HORIZONTAL);
                cardView.addView(mLayout);
                schedule.stream().limit(COL).forEach(elem -> {
                    TextView textView = new TextView(getActivity());

                    textView.setId(atomInt.get());
                    textView.setTextSize(10);
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundColor(Color.parseColor(atomInt.get() == 0 ? "#FAF4C0" : "#F1F1F1"));
                    textView.setText(elem.getTextExtractor().toString());
                    if (!TextUtils.isEmpty(textView.getText()))
                        textView.setOnClickListener(v -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                            builder.setMessage(elem.getTextExtractor().toString());
                            builder.setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());
                            builder.create().show();
                        });

                    mLayout.addView(textView, params); //시간표 데이터 출력
                });
                atomInt.getAndIncrement();
            });
            hideProgressBar();
        }, error -> {
            VolleyLog.e(TimetableFragment.TAG, error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                headers.put("Cookie", AppController.getInstance().getCookieManager().getCookie(EndPoint.LOGIN_LMS));
                return headers;
            }
        });
    }

    public int getLcdSizeWidth() {
        // TODO Auto-generated method stub
        return  ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public int getLcdSizeHeight() {
        // TODO Auto-generated method stub
        return ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }

    private void showProgressBar() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.GONE)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (mProgressBar != null && mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);
    }
}
