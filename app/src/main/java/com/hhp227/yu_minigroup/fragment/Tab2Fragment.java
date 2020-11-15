package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.calendar.ExtendedCalendarView;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.*;

public class Tab2Fragment extends Fragment {
    private static final int TYPE_CALENDAR = 0;

    private static final int TYPE_ITEM = 1;

    private static final String TAG = "일정";

    private Calendar mCalendar;

    private RecyclerView.Adapter mAdapter;

    private List<Map<String, String>> mList;

    public Tab2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.rv_cal);
        mList = new ArrayList<>();
        mCalendar = Calendar.getInstance();
        mAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == TYPE_CALENDAR) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.header_calendar, parent, false);
                    return new HeaderHolder(view);
                } else if (viewType == TYPE_ITEM) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.schedule_item, parent, false);
                    return new ItemHolder(view);
                }
                throw new RuntimeException();
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof ItemHolder) {
                    ((ItemHolder) holder).bind(mList.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return mList.size();
            }

            @Override
            public int getItemViewType(int position) {
                return position == 0 ? TYPE_CALENDAR : TYPE_ITEM;
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        fetchDataTask();
    }

    private void fetchDataTask() {
        String year = String.valueOf(mCalendar.get(Calendar.YEAR));
        String month = String.format("%02d", mCalendar.get(Calendar.MONTH) + 1);

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, "http://m.yu.ac.kr/_mobile/info/?c=info_01_01&year=" + year, response -> {
            Source source = new Source(response);

            try {
                mList.clear();
                addHeaderView();
                Element infoCalendar = source.getFirstElementByClass("info_calendar case");

                for (int i = 0; i < infoCalendar.getAllElements(HTMLElementName.A).size(); i++) {
                    if (infoCalendar.getAllElements(HTMLElementName.A).get(i).getAttributeValue("id").equals("list_" + year + month))
                        infoCalendar.getAllElements(HTMLElementName.UL).get(i).getAllElements(HTMLElementName.LI).forEach(element -> {
                            Map<String, String> map = new HashMap<>();

                            map.put("날짜", element.getFirstElement(HTMLElementName.P).getTextExtractor().toString());
                            map.put("내용", element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString());
                            mList.add(map);
                        });
                }
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }, error -> VolleyLog.e(error.getMessage())));
    }

    public void addHeaderView() {
        mList.add(new HashMap<>());
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {
        public ExtendedCalendarView extendedCalendarView;

        public HeaderHolder(View itemView) {
            super(itemView);
            extendedCalendarView = itemView.findViewById(R.id.calendar);

            extendedCalendarView.prev.setOnClickListener(v -> {
                extendedCalendarView.previousMonth();
                if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMinimum(Calendar.MONTH))
                    mCalendar.set((mCalendar.get(Calendar.YEAR) - 1), mCalendar.getActualMaximum(Calendar.MONTH),1);
                else
                    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
                fetchDataTask();
            });
            extendedCalendarView.next.setOnClickListener(v -> {
                extendedCalendarView.nextMonth();
                if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMaximum(Calendar.MONTH))
                    mCalendar.set((mCalendar.get(Calendar.YEAR) + 1), mCalendar.getActualMinimum(Calendar.MONTH),1);
                else
                    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
                fetchDataTask();
            });
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private final TextView date, content;

        public ItemHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            content = itemView.findViewById(R.id.content);
        }

        public void bind(Map<String, String> map) {
            date.setText(map.get("날짜"));
            content.setText(map.get("내용"));
        }
    }
}
