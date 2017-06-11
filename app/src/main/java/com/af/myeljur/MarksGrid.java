package com.af.myeljur;

/**
 * Created by Peter on 12.12.2016.
 */

public class MarksGrid {
    String name;
    String[] marks;
    String average;
    String[] comments;
    String[] dates;
    public MarksGrid(String name, String[] marks, String[] comments, String average, String[] dates){
        this.name=name;
        this.marks=marks;
        this.comments=comments;
        this.average=average;
        this.dates=dates;
    }
}
