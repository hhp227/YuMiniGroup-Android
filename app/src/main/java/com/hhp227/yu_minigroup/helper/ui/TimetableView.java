package com.hhp227.yu_minigroup.helper.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.hhp227.yu_minigroup.dto.TimetableItem;

import java.util.List;

public class TimetableView extends LinearLayout {
    private static final String COLOR_DAY_CELL = "#FAF4C0";

    private static final String COLOR_TIME_CELL = "#EAEAEA";

    private static final String COLOR_DATA_CELL = "#EAEAEA";

    private TextView[] mDataViews;

    private List<TimetableItem> mTimetableItems;

    private OnCellClickListener mOnCellClickListener;

    public TimetableView(Context context) {
        this(context, null);
    }

    public TimetableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setOnCellClickListener(OnCellClickListener onCellClickListener) {
        mOnCellClickListener = onCellClickListener;
    }

    /*요일 셀과 교시 셀, 시간표 데이터 셀을 생성하여 시간표를 그려준다*/
    public void setTimetable(String[] dayLine, String[] timeLine) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        LayoutParams dayParams = createCellLayoutParams(displayMetrics.widthPixels / 6, displayMetrics.heightPixels / 20);
        LayoutParams dataParams = createCellLayoutParams(displayMetrics.widthPixels / 6, displayMetrics.heightPixels / 14);
        dataParams.gravity = 1; // 표가 뒤틀리는 것을 방지
        LinearLayout headerRow = createRowView();
        mDataViews = new TextView[timeLine.length * (dayLine.length - 1)];

        removeAllViews();
        // 요일 생성
        for (String day : dayLine) {
            headerRow.addView(createCellView(day, COLOR_DAY_CELL), dayParams);
        }
        addView(headerRow);
        // 교시 및 시간표 데이터 셀 생성
        for (int i = 0, id = 0; i < timeLine.length; i++) {
            LinearLayout row = createRowView();

            row.addView(createCellView(timeLine[i], COLOR_TIME_CELL), dataParams);
            for (int j = 1; j < dayLine.length; j++) {
                final int cellId = id;
                TextView dataView = createCellView(null, COLOR_DATA_CELL);

                dataView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnCellClickListener != null) {
                            mOnCellClickListener.onCellClick(cellId);
                        }
                    }
                });
                mDataViews[id] = dataView;

                row.addView(dataView, dataParams);
                id++;
            }
            addView(row);
        }
        if (mTimetableItems != null) {
            submitList(mTimetableItems);
        }
    }

    /*시간표 데이터 셀에 강의명과 강의실명을 출력해준다*/
    public void submitList(List<TimetableItem> timetableItems) {
        mTimetableItems = timetableItems;

        if (mDataViews == null) {
            return;
        }
        for (TextView dataView : mDataViews) {
            dataView.setText(null);
        }
        for (TimetableItem timetableItem : timetableItems) {
            if (timetableItem.id >= 0 && timetableItem.id < mDataViews.length) {
                mDataViews[timetableItem.id].setText(timetableItem.subject + "\n" + timetableItem.classroom);
            }
        }
    }

    private LinearLayout createRowView() {
        LinearLayout linearLayout = new LinearLayout(getContext());

        linearLayout.setOrientation(HORIZONTAL);
        return linearLayout;
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

    public interface OnCellClickListener {
        void onCellClick(int id);
    }
}

