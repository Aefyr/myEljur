package com.af.myeljur;


import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class BriefFragment extends Fragment {

    boolean launched = false;
    SwipeRefreshLayout swipeRefreshLayout;

    private  boolean cancelled = false;

    @Override
    public void onDetach() {
        super.onDetach();
        cancelled = true;
        swipeRefreshLayout.setRefreshing(false);
    }

    public BriefFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brief, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!launched){
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.briefSwipeRefresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getBrief();
                }
            });
            getBrief();
            launched = true;
        }
    }

    class BriefDay{
        ArrayList<BriefLesson> lessons; //Основые уроки
        ArrayList<BriefLesson> afterLesson; //Вторая половина дня
        String date; //Дата yyyyMMdd
        String dayTitle; //По-идеи название дня недели
        public BriefDay(String date, String dayTitle, ArrayList<BriefLesson> lessons, ArrayList<BriefLesson> afterLesson){
            this.date = date;
            this.dayTitle = dayTitle;
            this.lessons = lessons;
            this.afterLesson = afterLesson;
        }
    }

    class BriefFile{
        String name; //Название файла
        String link; // Ссылка на файл
        public BriefFile(String name, String link){
            this.name = name;
            this.link = link;
        }
    }

    class BriefMark{
        String lesson;
        String value;
        String forWhat;
        public BriefMark(String lesson, String value, String forWhat){
            this.lesson = lesson;
            this.value = value;
            this.forWhat = forWhat;
        }
    }

    class BriefLesson{
        String num;
        String name; //Сообственно предмет
        String room; //Кабинет
        String teacherName;//Имя преподавателя
        ArrayList<String> homework; // Вот нафига вообще делить домашку на несколько частей?
        ArrayList<BriefFile> files; //Файлы
        ArrayList<BriefMark> marks;
        public  BriefLesson(String num,String name, String room,String teacherName, ArrayList<String> homework, ArrayList<BriefFile> files, ArrayList<BriefMark> marks){
            this.num = num;
            this.name = name;
            this.room = room;
            this.teacherName = teacherName;
            this.homework = homework;
            this.files = files;
            this.marks = marks;
        }
    }

    void getBrief(){
        swipeRefreshLayout.setRefreshing(true);
        String days = "";
        final String today;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        days += simpleDateFormat.format(c.getTime());
        today = days;
        days +="-";
        c.add(Calendar.DATE, 1);
        days += simpleDateFormat.format(c.getTime());

        final AppPreferences preferences = App.getPreferences();
        boolean offline = EljurApi.offline();
        if(offline){
            if(preferences.getString("brief","null").equals("null")){
                Utils.alertDialog(getContext(),"Необходимо подключение к сети").show();
                getActivity().findViewById(R.id.briefNoSavedWarning).setVisibility(View.VISIBLE);
            }else {
                applyBrief(resolveBrief(preferences.getString("brief","null")),preferences.getString("briefSavedDay","null"), offline);
            }
            swipeRefreshLayout.setRefreshing(false);
            return;
        }


        EljurApiRequest request = new EljurApiRequest(EljurApiRequest.Method.GETDIARY).addParameter("days",days).addParameter("student", Utils.getCurrentStudentId());
        EljurApi.execute(request, new EljurApi.pCallback() {
            @Override
            public void onSuccess(String raw) {

                if(cancelled)
                    return;

                preferences.put("briefSavedDay", today);
                preferences.put("brief",raw);
                applyBrief(resolveBrief(raw), today, false);
            }

            @Override
            public void onFail() {

                if(cancelled)
                    return;

                swipeRefreshLayout.setRefreshing(false);
                Utils.failedToaster();
            }
        });

    }

    ArrayList<BriefDay> resolveBrief(String raw){
        JSONObject main;
        try {
            main = (JSONObject)((JSONObject)((JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) new JSONParser().parse(raw)).get("response")).get("result")).get("students")).get(Utils.getCurrentStudentId())).get("days");
        } catch (ParseException|NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        Object[] daysKeys = main.keySet().toArray();
        Arrays.sort(daysKeys);
        ArrayList<BriefDay> days = new ArrayList<>();
        for(Object dayKey: daysKeys){
            JSONObject day = (JSONObject) main.get(dayKey);
            String date = NNHP.getStringFromJsonObject(day, "name");
            String dayTitle = NNHP.getStringFromJsonObject(day, "title");
            ArrayList<BriefLesson> lessons = new ArrayList<>();
            ArrayList<BriefLesson> afterLessons = new ArrayList<>();

            //Получаем уроки
            if(day.get("items")!=null) {
                JSONObject jLessons = (JSONObject) day.get("items");

                Object[] lessonsKeys = jLessons.keySet().toArray();
                Arrays.sort(lessonsKeys);

                for(Object lessonKey: lessonsKeys){
                    JSONObject jLesson = (JSONObject) jLessons.get(lessonKey);
                    String num = NNHP.getStringFromJsonObject(jLesson,"num");
                    String name = NNHP.getStringFromJsonObject(jLesson, "name");
                    String room = NNHP.getStringFromJsonObject(jLesson, "room");
                    String teacherName = NNHP.getStringFromJsonObject(jLesson, "teacher");
                    ArrayList<String> homework = null;
                    ArrayList<BriefFile> files = null;
                    ArrayList<BriefMark> marks = null;
                    //TODO CHECK FOR MARKS, HOMEWORK AND FILES

                    //Checking for homework
                    if(jLesson.get("homework")!=null){
                        homework = new ArrayList<>();
                        JSONObject jHomework = (JSONObject) jLesson.get("homework");
                        Object[] homeworkKeys = jHomework.keySet().toArray();
                        Arrays.sort(homeworkKeys);
                        for(Object homeworkKey: homeworkKeys){
                            String sHomework = ((JSONObject)jHomework.get(homeworkKey)).get("value").toString();
                            if(!sHomework.equals("")){
                                homework.add(sHomework);
                            }
                        }
                    }

                    //Checking for marks
                    if(jLesson.get("assessments")!=null){
                        marks = new ArrayList<>();
                        Object[] jMarks = ((JSONArray) jLesson.get("assessments")).toArray();
                        for(Object mark: jMarks){
                            JSONObject jMark = (JSONObject) mark;
                            String value = NNHP.getStringFromJsonObject(jMark, "value");
                            String forWhat = "";
                            if(jMark.get("comment")!=null){
                                forWhat = jMark.get("comment").toString();
                            }else if(jMark.get("lesson_comment")!=null){
                                forWhat = jMark.get("lesson_comment").toString();
                            }
                            marks.add(new BriefMark(name, value,forWhat));

                        }

                    }

                    //Checking for files
                    if(jLesson.get("files")!=null){
                        files = new ArrayList<>();
                        Object[] jFiles = ((JSONArray)jLesson.get("files")).toArray();
                        for(Object file:jFiles){
                            JSONObject jFile = (JSONObject) file;
                            String fileName = NNHP.getStringFromJsonObject(jFile, "filename");
                            String link = NNHP.getStringFromJsonObject(jFile, "link");
                            files.add(new BriefFile(fileName,link));
                        }
                    }

                    lessons.add(new BriefLesson(num,name,room,teacherName,homework,files,marks));

                }
            }

            days.add(new BriefDay(date,dayTitle,lessons,afterLessons));


        }

        //TODO REMOVE TEMP NULL RETURN
        return  days;
    }

    void applyBrief(ArrayList<BriefDay> days, String today, boolean offline) {
        if(offline){
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            Date realToday = new Date();
            if(!simpleDateFormat.format(realToday).equals(today)){
                getActivity().findViewById(R.id.briefOutdateWarning);
            }
        }
        System.out.println(days.get(0).date+ " TODAY: "+today);
        if (days.get(0).date.equals(today)) {
            if(days.size()==1){
                getActivity().findViewById(R.id.briefTomorrow).setVisibility(View.GONE);
                getActivity().findViewById(R.id.briefTomorrowDisabled).setVisibility(View.VISIBLE);
            }else {
                updateTomorrow(days);
            }

            ArrayList<BriefMark> marks = new ArrayList<>();

            BriefDay day = days.get(0);

            for (BriefLesson lesson : day.lessons) {
                if(lesson.marks!= null) {
                    for (BriefMark mark : lesson.marks) {
                        marks.add(mark);
                    }
                }
            }
            if(marks.size()!=0) {
                BriefGridAdapter adapter = new BriefGridAdapter(getContext(), marks);
                GridView todayMarksGrid = (GridView) getActivity().findViewById(R.id.briefTodayMarksGrid);
                todayMarksGrid.setAdapter(adapter);
            }else {
                ((TextView)getActivity().findViewById(R.id.briefTodayMarksText)).setText("Оценки на сегодня отсутсвуют");
            }
        }else {
            getActivity().findViewById(R.id.briefToday).setVisibility(View.GONE);
            getActivity().findViewById(R.id.briefTodayDisabled).setVisibility(View.VISIBLE);
            updateTomorrow(days);
        }
        swipeRefreshLayout.setRefreshing(false);
        getActivity().findViewById(R.id.briefNoSavedWarning).setVisibility(View.GONE);
        getActivity().findViewById(R.id.briefMain).setVisibility(View.VISIBLE);
    }

    void updateTomorrow(ArrayList<BriefDay> days){
        ArrayList<BriefLesson> lessonWithHomework = new ArrayList<>();
        for(BriefLesson lesson: days.get(1).lessons){
            lessonWithHomework.add(lesson);
        }
        if(lessonWithHomework.size()!=0) {
            BriefListAdapter adapter = new BriefListAdapter(getContext(), lessonWithHomework);
            BriefMaximumHeightListView listView = (BriefMaximumHeightListView) getActivity().findViewById(R.id.briefTomorrowList);
            listView.setAdapter(adapter);
        }
    }

    void resolveTodayAndTomorrow(ArrayList<Lesson> lessons, String todaysDate){
        if(lessons.size()==0){

        }
        if(lessons.size()==1){

        }
    }
}
