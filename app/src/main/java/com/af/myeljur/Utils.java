package com.af.myeljur;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ParseException;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Peter on 12.01.2017.
 */

public class Utils {
    static AlertDialog sDialog;
    static AlertDialog wDialog;
    public static String getCurrentWeek(){
        SharedPreferences prefs = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
        Set<String> weeks = prefs.getStringSet("sWeeks", new HashSet<String>());
        Object[] arr = weeks.toArray();
        Arrays.sort(arr);
        return arr[weeks.size()-prefs.getInt("weeksOffset",1)].toString();
    }
    public  static String getCurrentPeriod(){
        SharedPreferences prefs = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
        Set<String> periods = prefs.getStringSet("sPeriods", new HashSet<String>());
        Object[] arr = periods.toArray();
        Arrays.sort(arr);
        return arr[periods.size()-prefs.getInt("periodsOffset",1)].toString();
    }
    public static Snackbar loadingBar(View v, String t){
        return Snackbar.make(v, t, Snackbar.LENGTH_INDEFINITE).setAction("Ок", null);
    }
    static ProgressDialog loadingDialog(Context c){
        ProgressDialog d = new ProgressDialog(c);
        d.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        d.setMessage("Синхронизация...");
        d.setCanceledOnTouchOutside(false);
        return d;
    }
    static String getCurrentStudentId(){
        try {
            SharedPreferences prefs = App.getAppContext().getSharedPreferences("SD", Context.MODE_PRIVATE);
            String[] s = prefs.getString("IDs", null).split("S_");
            return s[prefs.getInt("CSID", 0)];
        }catch (NullPointerException ignore){
            return "null";
        }

    }
    static String getCurrentStudentName(){
        try {
            SharedPreferences prefs = App.getAppContext().getSharedPreferences("SD", Context.MODE_PRIVATE);
            String[] s = prefs.getString("Names", null).split("S_");
            return s[prefs.getInt("CSID", 0)];
         }catch (NullPointerException ignore){
            return "null";
         }

    }

    static AlertDialog.Builder alertDialog(Context c, String m){
        return new AlertDialog.Builder(c).setMessage(m).setPositiveButton("Ок",null);
    }

    static AlertDialog studentChoice(final MainActivity c, final Callback callback){
        final SharedPreferences prefs = App.getAppContext().getSharedPreferences("SD", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        final int CSID = prefs.getInt("CSID", 0);
        String[] sNames= prefs.getString("Names", null).split("S_");
        sDialog = new AlertDialog.Builder(c).setTitle("Ученик").setSingleChoiceItems(sNames,CSID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sDialog.dismiss();
                sDialog=null;
                if(i!=CSID){
                    editor.putInt("CSID", i).apply();
                    final AlertDialog d = loadingDialog(c);
                    d.show();
                    allSync(new EljurApi.Callback() {
                        @Override
                        public void onSuccess() {
                            d.dismiss();
                            callback.onClick(0);

                        }

                        @Override
                        public void onFail() {
                            d.dismiss();
                            alertDialog(c, "Ошибка синхронизации");
                        }
                    });
                }

            }
        }).create();
        return sDialog;
    }
    interface Callback{
        void onClick(int offset);
    }
    static AlertDialog weekChoice(Context c, final Callback callback){
        final SharedPreferences prefs = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        final String[] weeks = prefs.getString("weeksNames", null).split("S");
        final int cw = weeks.length - prefs.getInt("weeksOffset", 1);
        wDialog = new AlertDialog.Builder(c).setSingleChoiceItems(weeks, cw, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                wDialog.dismiss();
                if(i!=cw){
                    edit.putInt("weeksOffset", weeks.length-i).apply();
                    callback.onClick(cw);
                }


            }
        }).setTitle("Выбор недели").create();
        return wDialog;
    }

    static void setWeeksOffset(int offset){
        final SharedPreferences prefs = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("weeksOffset", offset).apply();
    }

    static AlertDialog periodChoice(Context c, final Callback callback){
        final SharedPreferences prefs = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        final String[] periods = prefs.getString("periodsNames", null).split("S");
        final int cp = periods.length - prefs.getInt("periodsOffset", 1); //Current Period Offset
        wDialog = new AlertDialog.Builder(c).setSingleChoiceItems(periods, cp, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                wDialog.dismiss();
                if(i!=cp){
                    edit.putInt("periodsOffset", periods.length-i).apply();
                    callback.onClick(cp);
                }


            }
        }).setTitle("Выбор периода").create();
        return wDialog;
    }

    static void setPeriodsOffset(int offset){
        final SharedPreferences prefs = App.getAppContext().getSharedPreferences("WP", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("periodsOffset", offset).apply();
    }


    static void eAllSync(final EljurApi.Callback callback){
        EljurApi.getPeriods(new EljurApi.Callback() {
            @Override
            public void onSuccess() {
                AppPreferences prefs = new AppPreferences(App.getAppContext());
                prefs.put("uD",true);
                prefs.put("uM",true);
            }

            @Override
            public void onFail() {
                callback.onFail();
            }
        });
    }

    static void allSync(final EljurApi.Callback callback){
        EljurApi.getRules(new EljurApi.Callback() {
            @Override
            public void onSuccess() {
                EljurApi.getPeriods(new EljurApi.Callback() {
                    @Override
                    public void onSuccess() {
                        EljurApi.resolveDiary(Utils.getCurrentWeek(), new EljurApi.ResolveDiaryCallback() {
                            @Override
                            public void onSuccess(ArrayList<Lesson> result) {
                                EljurApi.resolveGrid(Utils.getCurrentPeriod(), new EljurApi.ResolveGridCallback() {
                                    @Override
                                    public void onSuccess(ArrayList<MarksGrid> result) {
                                        EljurApi.resolveSchedule(new EljurApi.ResolveScheduleCallback() {
                                            @Override
                                            public void onSuccess(ArrayList<Day> result) {
                                                callback.onSuccess();
                                            }

                                            @Override
                                            public void onFail() {
                                               callback.onFail();
                                            }
                                        },false);

                                    }

                                    @Override
                                    public void onFail() {
                                        callback.onFail();
                                    }
                                }, false);
                            }

                            @Override
                            public void onFail() {
                                callback.onFail();
                            }
                        },false);
                    }

                    @Override
                    public void onFail() {
                        callback.onFail();
                    }
                });
            }

            @Override
            public void onFail() {
                callback.onFail();
            }
        });

    }

    static void failedToaster(){
        Toast.makeText(App.getAppContext(), "Ошибка синхронизации", Toast.LENGTH_SHORT).show();
    }

    static View.OnClickListener openLink(final String url){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri;
                try {
                    uri = Uri.parse(url);
                } catch (ParseException | NullPointerException e){
                    Utils.failedToaster();
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(uri);
                App.getAppContext().startActivity(intent);
            }
        };

    }

    public static float getDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }

    public static int convertDipToPix(int dip){
        float scale = getDensity(App.getAppContext());
        return (int) (dip * scale + 0.5f);
    }

    static int getScreenWidth(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    static int getScreenHeight(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    interface onUpdateListener{
        void onUpdate();
    }
    public View.OnTouchListener updateListener(View view){
        return null;
    }
}
