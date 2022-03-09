package com.hhp227.yu_minigroup.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.hhp227.yu_minigroup.R;
import com.hhp227.yu_minigroup.helper.TimetableHelper;

// TODO viewBinding 으로 이전할것

public class MockTimeTableFragment extends Fragment {
    // DatabaseFile의 경로를 가져오기위한 변수
    private String dbName = "timetable.db";

    // Database를 생성 관리하는 클래스
    private TimetableHelper helper;

    SQLiteDatabase db;

    Cursor cur;

    LinearLayout[] lay = new LinearLayout[10];

    LinearLayout layTime;

    String[] timeLine = {"1교시\n09:00", "2교시\n10:00", "3교시\n11:00", "4교시\n12:00", "5교시\n13:00", "6교시\n14:00", "7교시\n15:00", "8교시\n16:00", "9교시\n17:00", "10교시\n18:00"};

    String[] dayLine = {"시간", "월", "화", "수", "목", "금"};

    TextView[] time = new TextView[timeLine.length];

    TextView[] day = new TextView[dayLine.length];

    TextView[] data = new TextView[timeLine.length * dayLine.length];

    EditText putSubject;

    EditText putClassroom;

    int dbId;

    String dbClassroom, dbSubject;

    public MockTimeTableFragment() {
    }

    public static MockTimeTableFragment newInstance() {
        return new MockTimeTableFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mock_timetable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String dbPath = getContext().getDatabasePath(dbName).getPath();
        Log.i("my db path=", "" + dbPath);

        // DataBase 관리 클래스의 객체 생성을 해줌으로써 timetable.db파일과 schedule 테이블 생성
        helper = new TimetableHelper(getActivity());
        int counter = helper.getCounter();
        Log.i(TimetableFragment.TAG,"counter = " + counter);

        // 현재 들어있는 데이터를 log창으로 확인함
        // Helper클래스에 search함수에 가보면 자세한 설명있음.
        helper.search_data();

        // 레이아웃을 어떻게 그릴지 설정
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params1.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)
        params1.width = getLcdSizeWidth() / 6;
        params1.height = getLcdSizeHeight() / 14;
        params1.setMargins(1, 1, 1, 1);
        params1.gravity = 1; // 표가 뒤틀리는 것을 방지

        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params2.weight = 1; // 레이아웃의 weight를 동적으로 설정 (칸의 비율)
        params2.width = getLcdSizeWidth() / 6;
        params2.height = getLcdSizeHeight() / 20;
        params2.setMargins(1, 1, 1, 1);

        // 레이아웃 배열로 선언
        layTime = view.findViewById(R.id.lay_time);
        lay[0] = view.findViewById(R.id.lay_0);
        lay[1] = view.findViewById(R.id.lay_1);
        lay[2] = view.findViewById(R.id.lay_2);
        lay[3] = view.findViewById(R.id.lay_3);
        lay[4] = view.findViewById(R.id.lay_4);
        lay[5] = view.findViewById(R.id.lay_5);
        lay[6] = view.findViewById(R.id.lay_6);
        lay[7] = view.findViewById(R.id.lay_7);
        lay[8] = view.findViewById(R.id.lay_8);
        lay[9] = view.findViewById(R.id.lay_9);

        // 요일 생성
        for (int i = 0; i < day.length; i++) {
            day[i] = new TextView(getActivity());
            day[i].setText(dayLine[i]); // 텍스트에 보여줄 내용
            day[i].setGravity(Gravity.CENTER); // 정렬
            day[i].setBackgroundColor(Color.parseColor("#FAF4C0")); // 배경색
            day[i].setTextSize(10); // 글자크기
            layTime.addView(day[i], params2); // 레이아웃에 출력
        }

        // 교시 생성
        for (int i = 0; i < time.length; i++) {
            time[i] = new TextView(getActivity());
            time[i].setText(timeLine[i]);
            time[i].setGravity(Gravity.CENTER);
            time[i].setBackgroundColor(Color.parseColor("#EAEAEA"));
            time[i].setTextSize(10);
            lay[i].addView(time[i],params1);
        }

        cur =  helper.getAll();
        cur.moveToFirst();
        // data값 생성
        for (int i = 0, id = 0; i < lay.length; i++) { // 10개
            for (int j = 1; j < dayLine.length; j++) { // 6개
                data[id] = new TextView(getActivity());
                data[id].setId(id);//data[0]  =  0
                data[id].setTextSize(10);
                // 시간표를 입력하기 위한 곳을 클릭하면 클릭이벤트를 처리하기위해 동작처리함수
                data[id].setOnClickListener(v -> {
                    Cursor cursor;
                    cursor = helper.getAll(); // 테이블의 모든 데이터를 커서로 리턴.
                    int get[] = new int[50];
                    if (cursor != null) {
                        Log.i(TimetableFragment.TAG, "cursor is not null");
                        cursor.moveToFirst();
                        for (int i1 = 0; i1 < 50; i1++)
                            get[i1] = 0; // 배열 초기화

                        // 커서가 데이터의 마지막일때 까지 커서가 이동할 수있도록 해준다.
                        while (!cursor.isAfterLast()) {
                            // 정수배열의 테이블의 id값의 배열에 id값을 넣어준다.(get[3]=3)
                            get[cursor.getInt(0)] = cursor.getInt(0);
                            Log.i(TimetableFragment.TAG, "get "+get[cursor.getInt(0)]);
                            cursor.moveToNext();// 커서를 이동시켜준다.
                        }
                        for (int i1 = 0; i1 < 50; i1++) { // 배열의 길이만큼
                            Log.i(TimetableFragment.TAG, "get[i] ="+get[i1] + "   view.getid =" + view.getId() + "   data[i].getId() =" + data[i1].getId());
                            // 배열에 데이터가 있고,클릭한곳에 데이터가 있을시
                            if ((get[i1] != 0) && (get[i1] == view.getId())) {
                                // 클릭한곳의 아이디 값을 업데이트 다이얼로그로 넣어 불러준다.
                                update_timetable_dig(view.getId());
                                break;
                            }
                            // 배열에 데이터가 없고,클릭한곳이 데이터가 없을때
                            else if ((get[i1] == 0) && (view.getId() == data[i1].getId())) {
                                add_timetable_dig(view.getId());// 해당 다이얼로그를 불러줌
                                break;
                            }
                        }
                    }
                });
                data[id].setGravity(Gravity.CENTER);
                data[id].setBackgroundColor(Color.parseColor("#EAEAEA"));
                if ((cur != null) && (!cur.isAfterLast())) {
                    dbId = cur.getInt(0);
                    dbSubject = cur.getString(1);
                    dbClassroom = cur.getString(2);
                    if (data[id].getId() == dbId) {
                        data[id].setText(dbSubject + "\n" + dbClassroom);
                        cur.moveToNext();
                    }
                } else if (cur.isAfterLast())
                    cur.close();

                lay[i].addView(data[id], params1); //시간표 데이터 출력
                id++;
            }
        }
    }

    /*데이터가 없는 곳을 클릭했을 때 띄어주는 다이얼로그*/
    public void add_timetable_dig(final int id) {

        // inflate메서드는 컴파일된 리소스정보를 순서대고 해석해 뷰를생성하고 '루트'뷰를 리턴
        // 쉽게말해 inflate는 xml과 activiy를 연결해 동작을 처리하고 그 결과를 xml을 통해 화면에 보여줌
        final LinearLayout lay = (LinearLayout)View.inflate(getActivity(), R.layout.timetable_input_dig, null);
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());// 빌더 객체 생성
        ad.setTitle("시간표"); // 다이얼로그의 제목
        //ad.setIcon(R.drawable.timetable);// 다이얼로그의 아이콘
        ad.setView(lay); // 화면에 보여줄 대상을 설정
        /*저장 버튼이 눌렸을 때 처리*/
        ad.setPositiveButton("저장"/*버튼에 보여질 text*/, (dialog, which) -> {
            EditText put_subject = lay.findViewById(R.id.input_subject);
            EditText put_classroom = lay.findViewById(R.id.input_classroom);

            /*저장버튼일때 DB_table에 데이터 쓰기*/
            // 데이터의 아이디 값을 가지고와 add함수에 넘겨준다.
            // 이 아이디 값은 저장되어있는 데이터를 출력해 줄 때 저장되있던 곳에 위치시키기 위해 아이디값을 사용
            int get_id = data[id].getId();
            // EditText창으로 입력한 값을 가져오기 위해서는 EditText창 아이디값.getText().toString()으로 해주어야한다.
            // EditText창으로 입력한 값을 공백없이 가져오려면 EditText창 아이디값.getText().toString().trim() 으로 해주어야한다.
            helper.add(get_id,put_subject.getText().toString(),put_classroom.getText().toString());
            // editText를 통해 사용자에게 입력 받았던 데이터를 해당 텍스트뷰에 출력
            data[id].setText("" + put_subject.getText() + "\n" + put_classroom.getText());
        });

        // 취소버튼이 눌렸을 경우
        ad.setNegativeButton("취소"/*버튼에 보여질 text*/, (dialog, which) -> dialog.cancel());
        ad.show();
    }

    /*클릭한 곳에 데이터가 있을 경우 띄어주는 수정가능한 다이얼로그*/
    public void update_timetable_dig(final int id) {
        final LinearLayout lay = (LinearLayout) View.inflate(getActivity(), R.layout.timetable_input_dig, null);
        AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        ad.setTitle("TimeTable");
        //ad.setIcon(R.drawable.timetable);// 다이얼로그의 아이콘
        ad.setView(lay);
        putSubject = lay.findViewById(R.id.input_subject);
        putClassroom = lay.findViewById(R.id.input_classroom);
        /*데이터를 수정, 삭제 하기 위한 다이얼로그*/
        Cursor c; // 해당 뷰에 데이터가 있으면 다이얼로그 텍스트창에 출력해주기 위해 커서 사용.
        c = helper.getAll(); // 커서에 데이터베이스 테이블의 모든 데이터를 리턴해줌.
        if (c != null) { // 커서의 데이터가 있으면
            c.moveToFirst(); // 커서를 테이블 제일 처음, 즉 테이블의 제 1행을 가리키도록 한다.
            while (!c.isAfterLast()) { // 커서가 데이터의 마지막일때 까지 커서가 이동할 수있도록 해준다.
                // 커서가 가리키는 곳의 제 1열, id가 저장되어있는 열의 id값과 사용자가 누른곳의 id값이 같으면,
                // 사용자가 클릭한 곳에 데이터가 있을시 실행하고 반복문 종료
                if (c.getInt(0) == id) {
                    // 2열 3열, 강의명,강의실명을 가져와 텍스트에 보여주도록 설정
                    putSubject.setText(c.getString(1));
                    putClassroom.setText(c.getString(2));
                    break;
                }
                c.moveToNext();// 커서를 다음 행으로 이동시켜주는 역할
            }
        }
        // 강의명, 강의실을 적는 창을 각각 클릭했을 때 출력된 데이터를 지워준다.
        putSubject.setOnClickListener(v -> putSubject.setText(null));
        putClassroom.setOnClickListener(v -> putClassroom.setText(null));
        // 수정 버튼이 눌렸을때 처리하는 명령어
        ad.setPositiveButton("수정"/*버튼에 보여질 text*/, (dialog, which) -> {
            int get_id = data[id].getId();
            helper.update(get_id,putSubject.getText().toString(),putClassroom.getText().toString());
            data[id].setText("" + putSubject.getText() + "\n" + putClassroom.getText());
        });
        ad.setNegativeButton("삭제"/*버튼에 보여질 text*/, (dialog, which) -> {
            helper.delete(id);
            data[id].setText(null);
        });
        ad.show();
    }

    public void onDestroy() {
        super.onDestroy();
        helper.close();
    }

    public int getLcdSizeWidth() {
        // TODO Auto-generated method stub
        return  ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    public int getLcdSizeHeight() {
        // TODO Auto-generated method stub
        return ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }
}
