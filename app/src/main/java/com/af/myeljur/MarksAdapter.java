package com.af.myeljur;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Peter on 12.12.2016.
 */

class MarksAdapter extends BaseAdapter {

    private Context c;
    private ArrayList<MarksGrid> g;
    private LayoutInflater inflater;
    private Object[] a;
    private Object[] views;
    private boolean ready;

    MarksAdapter(Context c, ArrayList<MarksGrid> g){
        this.c=c;
        this.g=g;
        inflater=(LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ready=false;
        initialize();

    }

    @Override
    public int getCount() {
        return g.size();
    }

    @Override
    public Object getItem(int i) {
        return g.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View v;
        if (!ready) {
            v=preload(parent, i);
        }else {
            v=(View) views[i];
        }


        return v;
    }

    private void initialize(){
        a = new Object[g.size()];
        views = new Object[g.size()];
        int i=0;
        while(i<g.size()) {
            SingleGridElementAdapter adapter = new SingleGridElementAdapter(c, getG(i).marks, getG(i).comments, getG(i).dates);
            a[i] = adapter;
            i++;
        }
    }

    private View preload(ViewGroup parent, int n){
        int i=0;
        while(i<g.size()) {
            View v = inflater.inflate(R.layout.subject_marks_layout, parent, false);
            ((TextView) v.findViewById(R.id.subjectName)).setText(getG(i).name);
            ((TextView)v.findViewById(R.id.markAverage)).setText(String.valueOf(getG(i).average));
            ((TextView)v.findViewById(R.id.markAverage)).setTextColor(c.getResources().getColor(EljurApi.resolveColor(getG(i).average)));
            ((GridView)v.findViewById(R.id.marksGrid)).setAdapter((SingleGridElementAdapter)a[i]);
            if(i%2!=0){
                v.setBackgroundColor(c.getResources().getColor(R.color.colorPanelAlt));
            }else {
                v.setBackgroundColor(Color.WHITE);
            }
            views[i] = v;
            i++;
        }
        ready = true;
        return (View) views[n];
    }


    private MarksGrid getG(int i){
        return (MarksGrid) getItem(i);
    }
}
