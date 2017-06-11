package com.af.myeljur;

import net.grandcentrix.tray.AppPreferences;

/**
 * Created by Peter on 12.01.2017.
 */

public class EljurApiRequest {
    String REQUEST = "https://";
    private final String eljur = ".eljur.ru/apiv3/";
    private String CONSTANT = "devkey=6ec0e964a29c22fe5542f748b5143c4e&out_format=json";
    private AppPreferences prefs;
    public enum Method{
        LOGIN,GETDIARY, GETMARKS, GETPERIODS, GETRULES, GETSCHEDULE, GETMESSAGES, GETMESSAGEINFO, GETMESSAGERECEIVERS, SENDMESSAGE
    }
    private String methodByMethod(Method m){
        switch (m){
            case LOGIN:
                return "auth?";
            case GETDIARY:
                return "getdiary?";
            case GETMARKS:
                return  "getmarks?";
            case GETPERIODS:
                return  "getperiods?";
            case GETRULES:
                return "getrules?";
            case GETSCHEDULE:
                return "getschedule?";
            case GETMESSAGES:
                return "getmessages?";
            case GETMESSAGEINFO:
                return "getmessageinfo?";
            case GETMESSAGERECEIVERS:
                return "getmessagereceivers?";
            case SENDMESSAGE:
                return "sendmessage?";
            default:
                return null;
        }
    }
    public EljurApiRequest(Method m){
        prefs = new AppPreferences(App.getAppContext());
        this.REQUEST = this.REQUEST+prefs.getString("domain",null)+eljur+methodByMethod(m)+CONSTANT;
        if(prefs.getBoolean("loggedIn", false)){
            this.REQUEST = this.REQUEST+"&auth_token="+prefs.getString("token", null)+"&vendor="+prefs.getString("domain", null);
        }
    }
    public EljurApiRequest addParameter(String parameter, String value){
        this.REQUEST = this.REQUEST+"&"+parameter+"="+value;
        return this;
    }
}
