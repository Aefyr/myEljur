package com.af.myeljur;

import android.app.Application;
import android.content.Context;

import net.grandcentrix.tray.AppPreferences;

/**
 * Created by Peter on 12.01.2017.
 */

public class App extends Application {

    static Context context;
    private static AppPreferences preferences;

    public void onCreate() {
        super.onCreate();
        //App.context = getApplicationContext();
        //preferences = new AppPreferences(getApplicationContext());
    }

    public static void initialize(Context c){
        context = c;
        preferences = new AppPreferences(c);
    }

    public static Context getAppContext() {
        return App.context;
    }

    public static AppPreferences getPreferences(){
        return App.preferences;
    }
}