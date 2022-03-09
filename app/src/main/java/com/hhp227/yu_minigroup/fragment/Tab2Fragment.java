package com.hhp227.yu_minigroup.fragment;

import android.os.Bundle;
import android.util.Log;
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
import com.hhp227.yu_minigroup.app.AppController;
import com.hhp227.yu_minigroup.databinding.FragmentTab2Binding;
import com.hhp227.yu_minigroup.databinding.HeaderCalendarBinding;
import com.hhp227.yu_minigroup.databinding.ScheduleItemBinding;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.util.*;

import static com.hhp227.yu_minigroup.app.EndPoint.URL_SCHEDULE;

public class Tab2Fragment extends Fragment {
    private static final int TYPE_CALENDAR = 0;

    private static final int TYPE_ITEM = 1;

    private static final String TAG = "일정";

    private Calendar mCalendar;

    private RecyclerView.Adapter mAdapter;

    private List<Map<String, String>> mList;

    private FragmentTab2Binding mBinding;

    public Tab2Fragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentTab2Binding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList = new ArrayList<>();
        mCalendar = Calendar.getInstance();
        mAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == TYPE_CALENDAR) {
                    return new HeaderHolder(HeaderCalendarBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                } else if (viewType == TYPE_ITEM) {
                    return new ItemHolder(ScheduleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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

        mBinding.rvCal.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvCal.setAdapter(mAdapter);
        fetchDataTask();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private void fetchDataTask() {
        String year = String.valueOf(mCalendar.get(Calendar.YEAR));
        String month = String.format("%02d", mCalendar.get(Calendar.MONTH) + 1);

        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, URL_SCHEDULE.replace("{YEAR}", year), response -> {
            Source source = new Source(response);

            try {
                mList.clear();
                addHeaderView();
                /*Element infoCalendar = source.getFirstElementByClass("info_calendar case");

                for (int i = 0; i < infoCalendar.getAllElements(HTMLElementName.A).size(); i++) {
                    if (infoCalendar.getAllElements(HTMLElementName.A).get(i).getAttributeValue("id").equals("list_" + year + month))
                        infoCalendar.getAllElements(HTMLElementName.UL).get(i).getAllElements(HTMLElementName.LI).forEach(element -> {
                            Map<String, String> map = new HashMap<>();

                            map.put("날짜", element.getFirstElement(HTMLElementName.P).getTextExtractor().toString());
                            map.put("내용", element.getFirstElement(HTMLElementName.STRONG).getTextExtractor().toString());
                            mList.add(map);
                        });
                }*/
                Log.e("TEST", "source: " + source);
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null)
                VolleyLog.e(error.getMessage());
        }));
    }

    public void addHeaderView() {
        mList.add(new HashMap<>());
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {
        private final HeaderCalendarBinding mBinding;

        public HeaderHolder(HeaderCalendarBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

            mBinding.calendar.prev.setOnClickListener(v -> {
                mBinding.calendar.previousMonth();
                if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMinimum(Calendar.MONTH))
                    mCalendar.set((mCalendar.get(Calendar.YEAR) - 1), mCalendar.getActualMaximum(Calendar.MONTH),1);
                else
                    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1);
                fetchDataTask();
            });
            mBinding.calendar.next.setOnClickListener(v -> {
                mBinding.calendar.nextMonth();
                if (mCalendar.get(Calendar.MONTH) == mCalendar.getActualMaximum(Calendar.MONTH))
                    mCalendar.set((mCalendar.get(Calendar.YEAR) + 1), mCalendar.getActualMinimum(Calendar.MONTH),1);
                else
                    mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1);
                fetchDataTask();
            });
        }
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        private final ScheduleItemBinding mBinding;

        public ItemHolder(ScheduleItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(Map<String, String> map) {
            mBinding.date.setText(map.get("날짜"));
            mBinding.content.setText(map.get("내용"));
        }
    }
}
