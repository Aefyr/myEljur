package com.af.myeljur;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Peter on 13.03.2017.
 */

public class BriefGridAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Context c;
    ArrayList<BriefFragment.BriefMark> marks;

    public BriefGridAdapter(Context c, ArrayList<BriefFragment.BriefMark> marks){
        this.c = c;
        this.marks = marks;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return marks.size();
    }

    @Override
    public Object getItem(int i) {
        return marks.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private BriefFragment.BriefMark getMark(int i){
        return marks.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view = inflater.inflate(R.layout.brief_single_mark, viewGroup, false);
        }

        final BriefFragment.BriefMark mark = getMark(i);
        ((TextView)view.findViewById(R.id.briefSMLesson)).setText(mark.lesson);
        ((TextView)view.findViewById(R.id.briefSMValue)).setText(mark.value);

        if(!mark.forWhat.equals("")){
            view.setBackgroundResource(R.drawable.mark_bg);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.alertDialog(c,mark.forWhat).show();
                }
            });
        }

        return view;
    }
}
