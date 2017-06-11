package com.af.myeljur;

import java.util.ArrayList;

/**
 * Created by Peter on 12.01.2017.
 */

public class Lesson {
    String number;
    String name;
    String homework;
    Boolean headerMode;
    String[] marks;
    ArrayList<String> comments;
    String[][] files;
    boolean containsFiles=false;
    public Lesson(String number, String name, String homework, String[] marks){
        this.number=number;
        this.name=""+number+". "+name;
        this.homework=homework;
        this.marks=marks;
    }
    public Lesson(){
    }

    public  void addFiles(String[][] files){
        this.files=files;
        containsFiles=true;
    }
    public void setName(String name){
        this.name=" "+number+". "+name;
    }
}
