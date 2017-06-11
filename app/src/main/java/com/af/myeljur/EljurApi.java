package com.af.myeljur;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Peter on 12.01.2017.
 */

public class EljurApi {
    private static AppPreferences prefs;
    public static void initialize(){
        prefs = App.getPreferences();
    }
    static void execute(EljurApiRequest request, final pCallback callback){
        RequestQueue queue = Volley.newRequestQueue(App.getAppContext());
        String url = request.REQUEST;
        System.out.println(request.REQUEST);
        //Toast.makeText(c,"Making api request: "+url,Toast.LENGTH_LONG).show();

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFail();
            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    interface pCallback{
        void onSuccess(String raw);
        void onFail();
    }
    interface Callback{
        void onSuccess();
        void onFail();
    }
    interface ResolveDiaryCallback{
        void onSuccess(ArrayList<Lesson> result);
        void onFail();
    }
    interface ResolveGridCallback{
        void onSuccess(ArrayList<MarksGrid> result);
        void onFail();
    }
    interface ResolveScheduleCallback{
        void onSuccess(ArrayList<Day> result);
        void onFail();
    }
    static void login(EljurApiRequest request, final Callback callback){
        execute(request, new pCallback() {
            @Override
            public void onSuccess(String raw) {
                JSONParser parser = new JSONParser();
                JSONObject response;
                try {
                    response = (JSONObject) parser.parse(raw);
                    String token = ((JSONObject)((JSONObject) response.get("response")).get("result")).get("token").toString();
                    prefs.put("token", token);
                    prefs.put("loggedIn", true);
                    callback.onSuccess();
                }catch(ParseException e){
                    callback.onFail();
                }
            }

            @Override
            public void onFail() {
                callback.onFail();
            }
        });
    }

    static void resolveDiary(String days, final ResolveDiaryCallback callback, boolean offline){
        if(!offline) {
            EljurApiRequest getLessons = new EljurApiRequest(EljurApiRequest.Method.GETDIARY).addParameter("days", days).addParameter("student", Utils.getCurrentStudentId());
            execute(getLessons, new pCallback() {
                @Override
                public void onSuccess(String raw) {
                    prefs.put("cachedDiary", raw);
                    ArrayList<Lesson> lessons = parseDiary(raw);
                    if(lessons == null) {
                        callback.onFail();
                        return;
                    }
                    callback.onSuccess(lessons);
                }

                @Override
                public void onFail() {
                    callback.onFail();
                    return;
                }
            });
        }else {
            //Toast.makeText(c, "Оффлайн режим", Toast.LENGTH_SHORT).show();
            callback.onSuccess(parseDiary(prefs.getString("cachedDiary", null)));
        }
    }

    static ArrayList<Lesson> parseDiary(String result){
        ArrayList<Lesson> lessons = new ArrayList<Lesson>();
            JSONParser parser = new JSONParser();
            JSONObject raw;
            try {
                raw = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) parser.parse(result)).get("response")).get("result")).get("students");
            } catch (ParseException ignored) {
                return null;
            }
            String s = Utils.getCurrentStudentId();
            //TODO
            System.out.println(s);
            JSONObject days = (JSONObject) raw.get(s);
            if(days == null)
                return null;
            days = (JSONObject) days.get("days");
             if(days == null)
                 return  null;

            Object[] day = days.keySet().toArray();
            Arrays.sort(day);
            for (Object d : day) {
                JSONObject oneDay = (JSONObject) days.get(d);
                Date date;
                String aDate = "НЕИЗВЕСТНО";
                try {
                    date = new SimpleDateFormat("yyyyMMdd").parse(oneDay.get("name").toString());
                    aDate = new SimpleDateFormat("dd.MM").format(date)+" - ";
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }

                if(oneDay.get("alert")!=null&&oneDay.get("alert").equals("vacation")){
                    Lesson header = new Lesson();
                    header.headerMode = true;
                    header.name = aDate+NNHP.getStringFromJsonObject(oneDay, "title")+" - Выходной";
                    lessons.add(header);
                    continue;
                }
                Lesson header = new Lesson();
                header.name =aDate+NNHP.getStringFromJsonObject(oneDay, "title");
                header.headerMode = true;
                lessons.add(header);
                oneDay = (JSONObject) oneDay.get("items");

                if(oneDay == null)
                    continue;

                Object[] rawLessons = oneDay.keySet().toArray();
                Arrays.sort(rawLessons);
                for (Object rawL : rawLessons) {
                    Lesson actualLesson = new Lesson();
                    actualLesson.headerMode = false;
                    actualLesson.number = rawL.toString();
                    JSONObject realLesson = (JSONObject) oneDay.get(rawL);

                    if(realLesson == null)
                        continue;

                    actualLesson.setName(NNHP.getStringFromJsonObject(realLesson, "name"));

                    HOMEWORK:
                    {
                        if (realLesson.containsKey("homework")) {
                            JSONObject t = (JSONObject) realLesson.get("homework");
                            if (t == null)
                                break HOMEWORK;
                            Object[] homeworkArr = t.keySet().toArray();
                            Arrays.sort(homeworkArr);
                            int i = 0;
                            for (Object homeworkSection : homeworkArr) {

                                JSONObject h = (JSONObject) t.get(homeworkSection);

                                if (i > 0) {
                                    actualLesson.homework = actualLesson.homework + "\n" + "⌂ " + NNHP.getStringFromJsonObject(h,"value");
                                } else {
                                    actualLesson.homework = "⌂ " + NNHP.getStringFromJsonObject(h, "value");
                                }

                                i++;
                            }
                        }
                    }

                    ASSESSMENTS:
                    {
                        if (realLesson.containsKey("assessments")) {
                            JSONArray marks = (JSONArray) realLesson.get("assessments");

                            if(marks == null)
                                break ASSESSMENTS;

                            String[] Amarks = new String[marks.size()];
                            ArrayList<String> comments = new ArrayList<>(0);
                            int i = 0;
                            while (i < marks.size()) {
                                Amarks[i] = NNHP.getMarkValueFromJson((JSONObject) marks.get(i));
                                String c = NNHP.getLessonOverriddenCommentFromJson((JSONObject) marks.get(i));
                                if(c!=null)
                                    comments.add(c);
                                i++;
                            }
                            actualLesson.marks = Amarks;
                            if (comments.size() > 0) {
                                actualLesson.comments = comments;
                            }
                        }
                    }

                    FILES:
                    {
                        if (realLesson.containsKey("files")) {
                            if(realLesson.get("files")==null)
                                break FILES;

                            Object[] arr = ((JSONArray) realLesson.get("files")).toArray();
                            String[][] files = new String[2][arr.length];
                            int i = 0;
                            for (Object o : arr) {
                                JSONObject jFile = (JSONObject) o;
                                files[0][i] = NNHP.getStringFromJsonObject(jFile, "filename");
                                files[1][i] = NNHP.getLinkFromJson(jFile, "link");
                                i++;
                            }
                            actualLesson.addFiles(files);

                        }
                    }
                    //TODO add marks checker right here!
                    lessons.add(actualLesson);
                }

            }

        return lessons;

    }

    static void resolveGrid(String days, final ResolveGridCallback callback, boolean offline){
        if(!offline) {
            EljurApiRequest getMarks = new EljurApiRequest(EljurApiRequest.Method.GETMARKS).addParameter("student", Utils.getCurrentStudentId()).addParameter("days",days);
            execute(getMarks, new pCallback() {
                @Override
                public void onSuccess(String raw) {
                    prefs.put("cachedMarks", raw);
                    ArrayList<MarksGrid> marks = parseGrid(raw);
                    if(marks!=null){
                        callback.onSuccess(marks);
                    }else {
                        callback.onFail();
                    }

                }

                @Override
                public void onFail() {
                    callback.onFail();
                }
            });
        }else {
            ArrayList<MarksGrid> marks = parseGrid(prefs.getString("cachedMarks", null));
            if(marks!=null){
                callback.onSuccess(marks);
            }else {
                callback.onFail();
            }
        }
    }

    static ArrayList<MarksGrid> parseGrid(String result){
        ArrayList<MarksGrid> grids = new ArrayList<>();
            JSONObject raw;
            try {
                raw = (JSONObject) new JSONParser().parse(result);
                raw = (JSONObject) ((JSONObject) ((JSONObject) ((JSONObject) raw.get("response")).get("result")).get("students")).get(Utils.getCurrentStudentId());
            } catch (ParseException |ClassCastException e) {
                return null;
            }

            Object[] lessons = ((JSONArray) raw.get("lessons")).toArray();
            for (Object lesson : lessons) {
                MarksGrid g;
                String name = ((JSONObject) lesson).get("name").toString();
                String average = ((JSONObject) lesson).get("average").toString();

                Object[] rawMarks = ((JSONArray) ((JSONObject) lesson).get("marks")).toArray();

                //Well not the best efficiency, I guess
                ArrayList<Object> filteredFromNullsMarks = new ArrayList<>();
                for(Object e:rawMarks){
                    JSONObject rMark = (JSONObject) e;
                    if(!rMark.get("value").toString().equals("")){
                        filteredFromNullsMarks.add(e);
                    }
                }
                String[] marks = new String[filteredFromNullsMarks.size()];
                String[] comments = new String[filteredFromNullsMarks.size()];
                String[] dates = new String[filteredFromNullsMarks.size()];

                int i = 0;
                for (Object aMark : filteredFromNullsMarks) {
                    JSONObject rMark = (JSONObject) aMark;
                        marks[i] = rMark.get("value").toString().toUpperCase();
                        String[] datesRaw = rMark.get("date").toString().split("-");
                        dates[i] = datesRaw[2] + "." + datesRaw[1];
                        if (rMark.get("lesson_comment") != null) {
                            comments[i] = rMark.get("lesson_comment").toString();
                        }
                        i++;
                }
                g = new MarksGrid(name, marks, comments, average, dates);
                grids.add(g);
            }

        return grids;
    }

    static boolean offline(){
        if(prefs.getBoolean("alwaysOffline", false)||prefs.getBoolean("tempOffline", false)){
            return true;
        }
        ConnectivityManager connectivityManager
                = (ConnectivityManager) App.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo == null;
    }

    public static int resolveColor(String mark){
        if(mark.length()>0) {
            mark = mark.substring(0, 1);
        }
        switch (mark){
            case "1":
            case "2":
                return R.color.colorPrettyOrange;
            case "3":
                return R.color.colorOrange;
            case "4":
                return R.color.colorLime;
            case "5":
                return R.color.colorGreen;
            default:
                return R.color.colorLesson;
        }
    }

    public static void getPeriods(final Callback callback){
        final EljurApiRequest request = new EljurApiRequest(EljurApiRequest.Method.GETPERIODS).addParameter("weeks", "__yes").addParameter("student", Utils.getCurrentStudentId());
        execute(request, new pCallback() {
            @Override
            public void onSuccess(String raw) {
                JSONObject response;
                SharedPreferences settings = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                Set<String> sPeriods = new HashSet<String>();
                Set<String> sWeeks = new HashSet<String>();
                String nWeeks ="";
                String nPeriods="";

                try {
                    response=(JSONObject)((JSONObject)((JSONObject) new JSONParser().parse(raw)).get("response")).get("result");
                } catch (ParseException e) {
                    callback.onFail();
                    return;
                }
                    Object[] periods = ((JSONArray)((JSONObject)((JSONArray)response.get("students")).get(0)).get("periods")).toArray(); //TODO
                    for (Object p: periods){
                        if(!(boolean)(((JSONObject)p).get("ambigious"))) {
                            sPeriods.add((((JSONObject) p).get("start")).toString() + "-" + (((JSONObject) p).get("end")).toString());
                            nPeriods=nPeriods+(((JSONObject) p).get("fullname")).toString()+"S";
                            Object[] weeks = ((JSONArray)((JSONObject) p).get("weeks")).toArray();
                            for (Object w : weeks) {
                                sWeeks.add((((JSONObject) w).get("start")).toString() + "-" + (((JSONObject) w).get("end")).toString());
                                nWeeks=nWeeks+(((JSONObject) w).get("title")).toString()+"S";
                            }

                        }
                    }
                    nWeeks=nWeeks.substring(0,nWeeks.length()-1).replaceAll(Pattern.quote("&mdash;"), "—");
                    nPeriods=nPeriods.substring(0,nPeriods.length()-1);
                    editor.putString("periodsNames", nPeriods);
                    editor.putString("weeksNames", nWeeks);
                    editor.putStringSet("sPeriods", sPeriods);
                    editor.putStringSet("sWeeks", sWeeks);
                    editor.apply();
                    callback.onSuccess();


            }

            @Override
            public void onFail() {

            }
        });
    }

    public static void getRules (final Callback callback){
        final EljurApiRequest request = new EljurApiRequest(EljurApiRequest.Method.GETRULES);
        execute(request, new pCallback() {
            @Override
            public void onSuccess(String raw) {
                JSONObject result;
                try {
                    result = ((JSONObject)((JSONObject)((JSONObject)((JSONObject)((JSONObject) new JSONParser().parse(raw)).get("response")).get("result")).get("relations")).get("students"));
                } catch (ParseException e) {
                    callback.onFail();
                    return;
                }
                    Object[] keys = result.keySet().toArray();
                    SharedPreferences SD = App.getAppContext().getSharedPreferences("SD", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = SD.edit();
                    ArrayList<String> studentsIds = new ArrayList<>();
                    ArrayList<String> studentsAlias =new ArrayList<>();
                    for(Object key: keys){
                            JSONObject student = (JSONObject) result.get(key);
                            studentsIds.add(student.get("name").toString());
                            studentsAlias.add(student.get("title").toString());
                    }
                    StringBuilder SI= new StringBuilder();
                    StringBuilder SA= new StringBuilder();
                    for(String s: studentsAlias){
                        SA.append(s+"S_");
                    }
                    SA.substring(0, SA.length()-2);
                    for (String s: studentsIds){
                        SI.append(s+"S_");
                    }
                    SI.substring(0, SI.length()-2);
                    edit.putString("IDs", SI.toString());
                    edit.putString("Names", SA.toString());
                    edit.putBoolean("ready", true);
                    edit.apply();
                    callback.onSuccess();

            }

            @Override
            public void onFail() {
                callback.onFail();
            }
        });
    }

    public static void resolveSchedule(final ResolveScheduleCallback callback, boolean offline){
        if(!offline) {
            EljurApiRequest request = new EljurApiRequest(EljurApiRequest.Method.GETSCHEDULE).addParameter("student", Utils.getCurrentStudentId()).addParameter("days", Utils.getCurrentWeek());
            execute(request, new pCallback() {
                @Override
                public void onSuccess(String raw) {
                    prefs.put("cachedSchedule", raw);
                    ArrayList<Day> days = parseSchedule(raw);
                    if(days!=null) {
                        callback.onSuccess(days);
                    }else {
                        callback.onFail();
                    }
                }

                @Override
                public void onFail() {
                    callback.onFail();
                }
            });
        }else {
            callback.onSuccess(parseSchedule(prefs.getString("cachedSchedule", null)));
        }
    }

    static ArrayList<Day> parseSchedule(String raw){
        ArrayList<Day> days = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONObject result;
        try {
            result = (JSONObject)((JSONObject)((JSONObject)((JSONObject)((JSONObject)((JSONObject) parser.parse(raw)).get("response")).get("result")).get("students")).get(Utils.getCurrentStudentId())).get("days");
        }catch (ParseException e){
            return null;
        }
            Object[] keys = result.keySet().toArray();
            Arrays.sort(keys);
            for(Object key:keys){
                JSONObject day = (JSONObject) result.get(key);
                String name = day.get("title").toString();
                day=(JSONObject) day.get("items");
                Object[] lessons = day.keySet().toArray();
                Arrays.sort(lessons);
                String[] aLessons = new String[lessons.length];
                int i = 0;
                for(Object lesson:lessons){
                    aLessons[i]=lesson.toString()+". "+((JSONObject)day.get(lesson)).get("name").toString()+" в "+((JSONObject)day.get(lesson)).get("room").toString();
                    i++;
                }
                Day aDay = new Day(name, aLessons);
                days.add(aDay);

            }


        return days;
    }



}
