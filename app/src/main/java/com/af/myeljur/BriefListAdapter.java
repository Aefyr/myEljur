package com.af.myeljur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Peter on 14.03.2017.
 */

public class BriefListAdapter extends BaseAdapter {
    ArrayList<BriefFragment.BriefLesson> lessonsWithHomework;
    Context c;
    LayoutInflater inflater;

    public BriefListAdapter(Context c, ArrayList<BriefFragment.BriefLesson> lessons){
        this.c = c;
        this.lessonsWithHomework = lessons;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return lessonsWithHomework.size();
    }

    @Override
    public Object getItem(int i) {
        return lessonsWithHomework.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            view = inflater.inflate(R.layout.brief_homework_section,viewGroup,false);
        }
        BriefFragment.BriefLesson lesson = lessonsWithHomework.get(i);

        ((TextView)view.findViewById(R.id.briefHomeworkLessonTitle)).setText(lesson.num+". "+lesson.name);

        TextView homeworkTV = ((TextView) view.findViewById(R.id.briefHomeworkHomework));
        if(lesson.homework!=null) {
            homeworkTV.setVisibility(View.VISIBLE);
            StringBuilder b = new StringBuilder();
            for (String homework : lesson.homework) {
                b.append("⌂ " + homework + "\n");
            }
            b.deleteCharAt(b.length() - 1);

            homeworkTV.setText(b.toString());
        }else {
            homeworkTV.setVisibility(View.GONE);
        }
        LinearLayout files = (LinearLayout) view.findViewById(R.id.briefHomeworkFiles);

        if(lesson.files!=null){

            files.setVisibility(View.VISIBLE);
            files.removeAllViews();
            for(final BriefFragment.BriefFile file:lesson.files){
                View v = inflater.inflate(R.layout.brief_homework_file,null);
                Button name = ((Button)v.findViewById(R.id.briefHomeworkFileName));
                name.setText(file.name);
                name.setOnClickListener(Utils.openLink(file.link));
                files.addView(v);
            }
        }else {
            files.setVisibility(View.GONE);
        }
        return view;
    }
}
