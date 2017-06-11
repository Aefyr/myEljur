package com.af.myeljur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Peter on 21.01.2017.
 */

public class ScheduleAdapter extends BaseAdapter {
    Object[] views;
    ArrayList<Day> days;
    Context c;
    LayoutInflater inflater;
    boolean ready = false;
    public ScheduleAdapter(Context c,ArrayList<Day> days){
        this.days=days;
        this.c = c;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public void setDays(ArrayList<Day> days){
        this.days=days;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public Object getItem(int i) {
        return days.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    Day getDay(int i){
        return days.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(!ready){
            return initialize(viewGroup,i);
        }else {
            return (View)views[i];
        }
    }

    View initialize(ViewGroup parent, int p){
        views = new Object[getCount()];
        for(int i=0;i<getCount();i++){
            View v = inflater.inflate(R.layout.schedule_day , parent, false);
            ((TextView)v.findViewById(R.id.scheduleDayTitle)).setText(getDay(i).name);
            StringBuilder list = new StringBuilder();
            for(String l:getDay(i).lessons){
                list.append(l+"\n");
            }
            ((TextView)v.findViewById(R.id.scheduleDayLessons)).setText(list.toString());
            views[i]=v;
        }
        ready = true;
        return (View)views[p];
    }


}
