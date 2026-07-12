package com.hhp227.yu_minigroup.helper.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;

import java.util.List;

public class SemesterTimetableView extends LinearLayout {
    private static final String COLOR_HEADER_CELL = "#FAF4C0";

    private static final String COLOR_DATA_CELL = "#EAEAEA";

    public SemesterTimetableView(Context context) {
        this(context, null);
    }

    public SemesterTimetableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    /*행 단위의 텍스트 리스트로 학기 시간표를 그려준다. 첫번째 행은 요일 헤더*/
    public void submitTable(List<List<String>> table) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        LayoutParams headerParams = createCellLayoutParams(displayMetrics.widthPixels / 6, displayMetrics.heightPixels / 20);
        LayoutParams dataParams = createCellLayoutParams(displayMetrics.widthPixels / 6, displayMetrics.heightPixels / 14);
        dataParams.gravity = 1; // 표가 뒤틀리는 것을 방지

        removeAllViews();
        for (int i = 0; i < table.size(); i++) {
            LinearLayout row = new LinearLayout(getContext());

            row.setOrientation(HORIZONTAL);
            for (String text : table.get(i)) {
                row.addView(createCellView(text, i == 0 ? COLOR_HEADER_CELL : COLOR_DATA_CELL), i == 0 ? headerParams : dataParams);
            }
            addView(row);
        }
    }

    private TextView createCellView(String text, String backgroundColor) {
        TextView textView = new TextView(getContext());

        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.parseColor(backgroundColor));
        textView.setTextSize(10);
        return textView;
    }

    private LayoutParams createCellLayoutParams(int width, int height) {
        LayoutParams params = new LayoutParams(width, height);
        params.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)

        params.setMargins(1, 1, 1, 1);
        return params;
    }
}
