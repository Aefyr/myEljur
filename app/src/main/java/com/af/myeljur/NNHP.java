package com.af.myeljur;

import org.json.simple.JSONObject;

/**
 * Created by Peter on 19.02.2017.
 */

public class NNHP {
    static String getStringFromJsonObject(JSONObject jO, String stringName){
        if(jO.get(stringName)!=null){
            return jO.get(stringName).toString();
        }else {
            return "ошибка";
        }
    }
    static  Boolean getBoolFromJsonObject(JSONObject jO, String boolName, boolean onParseFail){
        if(jO.get(boolName).toString().equals("true")){
            return true;
        }else if(jO.get(boolName).toString().equals("false")){
            return  false;
        }else {
            return  onParseFail;
        }
    }

    static String getNameFromJson(JSONObject user){
        String firstname,middlename,lastname;
        firstname = middlename = lastname = "";
        if(user.get("firstname")!=null){
            firstname = user.get("firstname").toString()+" ";
        }
        if(user.get("middlename")!=null){
            middlename = user.get("middlename").toString()+" ";
        }
        if(user.get("lastname")!=null){
            middlename = user.get("lastname").toString();
        }

        return firstname+middlename+lastname;
    }

    static String getLinkFromJson(JSONObject o, String name){
        if(o.get(name)==null)
            return "https://api.eljur.ru";
        return o.get(name).toString();

    }

    static String getLessonOverriddenCommentFromJson(JSONObject mark){
        if(mark.get("lesson_comment")!=null && !mark.get("lesson_comment").toString().equals(""))
            return mark.get("lesson_comment").toString();
        if(mark.get("comment")!=null && !mark.get("comment").toString().equals(""))
            return mark.get("comment").toString();
        return null;
    }

    static  String getMarkValueFromJson(JSONObject mark){
        if(mark.get("value")!=null)
            return mark.get("value").toString().toUpperCase();
        return "?";
    }
}
