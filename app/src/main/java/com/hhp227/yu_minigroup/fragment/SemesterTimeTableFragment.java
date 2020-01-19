package com.hhp227.yu_minigroup.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class SemesterTimeTableFragment extends Fragment {
    private static final int ROW = 26;
    private static final int COL = 6;
    private static final String TAG = "시간표";
    private LinearLayout mLayout;
    private ProgressBar mProgressBar;

    public SemesterTimeTableFragment() {
    }

    public static SemesterTimeTableFragment newInstance() {
        SemesterTimeTableFragment fragment = new SemesterTimeTableFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timetable, container, false);
        LinearLayout cardView = rootView.findViewById(R.id.ll_timetable);
        mProgressBar = rootView.findViewById(R.id.pb_group);

        showProgressBar();
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, EndPoint.TIMETABLE, response -> {
            Element timeTable = new Source(response).getFirstElementByClass("bbslist");
            List<Element> list = timeTable.getAllElements(HTMLElementName.TR);
            AtomicInteger atomicInteger = new AtomicInteger();

            list.stream().limit(ROW).forEach(element -> {
                List<Element> schedule = element.getChildElements();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)
                params.width = getLcdSizeWidth() / 6;
                params.height = getLcdSizeHeight() / (atomicInteger.get() == 0 ? 20 : 14);
                params.setMargins(1, 1, 1, 1);
                params.gravity = 1;

                mLayout = new LinearLayout(getContext());
                mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                mLayout.setGravity(Gravity.LEFT);
                mLayout.setOrientation(LinearLayout.HORIZONTAL);
                cardView.addView(mLayout);

                schedule.stream().limit(COL).forEach(elem -> {
                    TextView textView = new TextView(getActivity());
                    textView.setId(atomicInteger.get());
                    textView.setTextSize(10);
                    textView.setGravity(Gravity.CENTER);
                    textView.setBackgroundColor(Color.parseColor(atomicInteger.get() == 0 ? "#FAF4C0" : "#EAEAEA"));
                    textView.setText(elem.getTextExtractor().toString());

                    mLayout.addView(textView, params); //시간표 데이터 출력
                });
                atomicInteger.getAndIncrement();
            });
            hideProgressBar();
        }, error -> {
            VolleyLog.e(TAG, error.getMessage());
            hideProgressBar();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", AppController.getInstance().getPreferenceManager().getCookie());
                return headers;
            }
        });
        return rootView;
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
