package com.af.myeljur;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Peter on 12.12.2016.
 */

public class SingleGridElementAdapter extends BaseAdapter {

    Context c;
    String[] marks;
    String[] comments;
    String[] dates;
    LayoutInflater inflater;

    public SingleGridElementAdapter(Context c, String[] marks, String[] comments, String[] dates){
        this.c=c;
        this.marks=marks;
        this.comments=comments;
        this.dates=dates;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return marks.length;
    }

    @Override
    public Object getItem(int i) {
        return marks[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View v = view;
        if (v == null) {
            v = inflater.inflate(R.layout.one_mark, parent, false);
        }
        ((TextView)v.findViewById(R.id.oneMark)).setText(marks[i]);
        ((TextView)v.findViewById(R.id.markDate)).setText(dates[i]);
        ((TextView)v.findViewById(R.id.oneMark)).setTextColor(c.getResources().getColor(EljurApi.resolveColor(marks[i])));
        if(comments[i]!=null){
            v.findViewById(R.id.oneMarkLayout).setBackgroundResource(R.drawable.mark_bg);
            final int a = i;
            ((TextView)v.findViewById(R.id.oneMark)).setTypeface(null, Typeface.BOLD);
            v.findViewById(R.id.oneMark).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   Utils.alertDialog(c, comments[a]).show();
                }
            });
        }
        return v;
    }
}
