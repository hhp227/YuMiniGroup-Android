package com.hhp227.yu_minigroup.viewmodel;

import static com.hhp227.yu_minigroup.app.EndPoint.URL_SCHEDULE;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.hhp227.yu_minigroup.app.AppController;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Tab2ViewModel extends ViewModel {
    private static final String TAG = Tab2ViewModel.class.getSimpleName();

    private final MutableLiveData<Boolean> mLoading = new MutableLiveData<>(false);

    private final MutableLiveData<List<Map<String, String>>> mItemList = new MutableLiveData<>(Collections.emptyList());

    private final MutableLiveData<String> mMessage = new MutableLiveData<>();

    private final MutableLiveData<Calendar> mCalendar = new MutableLiveData<>(Calendar.getInstance());

    public LiveData<Calendar> getCalendar() {
        return mCalendar;
    }

    public LiveData<List<Map<String, String>>> getItemList() {
        return mItemList;
    }

    public LiveData<String> getMessage() {
        return mMessage;
    }

    public void previousMonth() {
        Calendar calendar = mCalendar.getValue();

        if (calendar != null) {
            if (calendar.get(Calendar.MONTH) == calendar.getActualMinimum(Calendar.MONTH)) {
                calendar.set((calendar.get(Calendar.YEAR) - 1), calendar.getActualMaximum(Calendar.MONTH),1);
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
            }
            mCalendar.postValue(calendar);
        }
    }

    public void nextMonth() {
        Calendar calendar = mCalendar.getValue();

        if (calendar != null) {
            if (calendar.get(Calendar.MONTH) == calendar.getActualMaximum(Calendar.MONTH)) {
                calendar.set((calendar.get(Calendar.YEAR) + 1), calendar.getActualMinimum(Calendar.MONTH),1);
            } else {
                calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
            }
            mCalendar.postValue(calendar);
        }
    }

    public void fetchDataTask(Calendar calendar) {
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);

        mLoading.postValue(true);
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.POST, URL_SCHEDULE, response -> {
            List<Map<String, String>> list = new ArrayList<>();
            //Source source = new Source(response);
            DocumentBuilder documentBuilder;
            Document document;

            try {
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = documentBuilder.parse(new InputSource(new StringReader(response)));
                Element documentElement = document.getDocumentElement();
                NodeList elementsByTagName = documentElement.getElementsByTagName("Items");

                if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                    for (int i = 0; i < elementsByTagName.getLength(); i++) {
                        Element element = (Element) elementsByTagName.item(i);
                        Element element1 = (Element) element.getElementsByTagName("Subject").item(0);
                        Element element2 = (Element) element.getElementsByTagName("Date").item(0);
                        Element element3 = (Element) element.getElementsByTagName("Author").item(0);
                        Element element4 = (Element) element.getElementsByTagName("Text").item(0);
                        Element element5 = (Element) element.getElementsByTagName("Link").item(0);
                        Map<String, String> map = new HashMap<>();
                        String date = getParsing(element2);

                        if (date.substring(0, 4).equals(year) && date.substring(date.indexOf("-") + 1).substring(0, 2).equals(month)) {
                            map.put("날짜", getParsing(element2));
                            map.put("내용", getParsing(element1));
                            list.add(map);
                            Log.e("TEST", "element1: " + getParsing(element1) + ", element2: " + getParsing(element2) + ", element3: " + getParsing(element3) + ", element4: " + getParsing(element4) + ", element5: " + getParsing(element5));
                        }
                    }
                }
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
                mLoading.postValue(false);
                mItemList.postValue(list);
            } catch (Exception e) {
                mLoading.postValue(false);
                mMessage.postValue(e.getMessage());
            }
        }, error -> {
            if (error.getMessage() != null) {
                VolleyLog.e(error.getMessage());
                mLoading.postValue(false);
                mMessage.postValue(error.getMessage());
            }
        }));
    }

    private String getParsing(Element element) {
        if (element != null) {
            try {
                return element.getFirstChild().getNodeValue();
            } catch (Exception unused2) {
                return "error";
            }
        } else {
            return "error";
        }
    }
}