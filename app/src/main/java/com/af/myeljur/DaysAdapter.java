package com.af.myeljur;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Peter on 11.12.2016.
 */

class DaysAdapter extends BaseAdapter {
    private Context c;
    private ArrayList l;
    private LayoutInflater inflater;
    private Object[] views;
    private boolean ready;
    int e;
    DaysAdapter(Context context, ArrayList<Lesson> lessons){
        c=context;
        l=lessons;
        inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return l.size();
    }

    @Override
    public Object getItem(int i) {
        return l.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View converView, ViewGroup parent) {
        if(!ready){
            return initialize(parent, i);
        }else {
            return (View) views[i];
        }
        }
    private Lesson getLesson(int p){
        return ((Lesson)getItem(p));
    }

    private View initialize(ViewGroup parent, int a){
        views = new Object[l.size()];
        View view;
        int i = 0;
        while (i<l.size()) {
            final Lesson lesson = getLesson(i);

            if (lesson.headerMode) {
                view = inflater.inflate(R.layout.day_header, parent, false);
                ((TextView) view.findViewById(R.id.dayHeaderTV)).setText(lesson.name);
            } else {
                view = inflater.inflate(R.layout.lesson_layout, parent, false);
                ((TextView) view.findViewById(R.id.lesson)).setText(lesson.name);
                ((TextView) view.findViewById(R.id.homework)).setText(lesson.homework);
                if (lesson.marks != null) {
                    String marks = "";
                    for (String m : lesson.marks) {
                        marks = marks + " " + m;
                    }
                    ((TextView) view.findViewById(R.id.marks)).setText(marks);
                    ((TextView) view.findViewById(R.id.marks)).setTextColor(c.getResources().getColor(EljurApi.resolveColor(marks.substring(1))));
                    if(lesson.comments!=null){
                        final StringBuilder builder = new StringBuilder();
                        String[] marks_ = getLesson(i).marks;
                        int b=0;
                        for(String comment:getLesson(i).comments){
                            if(marks_[b]!=null){
                                builder.append(marks_[b]+" - "+comment+"\n");
                            }
                            b++;
                        }
                        view.findViewById(R.id.marks).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Utils.alertDialog(c, builder.toString()).show();
                            }
                        });
                        View minifl = view.findViewById(R.id.miniFL);
                        minifl.setBackgroundResource(R.drawable.mark_bg);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)minifl.getLayoutParams();
                        int f = Math.max(params.height, params.width);
                        params.height = f;
                        params.width = f;
                        minifl.setLayoutParams(params);
                        ((TextView) view.findViewById(R.id.marks)).setTypeface(null, Typeface.BOLD);
                    }
                }
                if(lesson.containsFiles){
                    e = 0;
                    for(String name:lesson.files[0]){
                        LinearLayout layout = (LinearLayout) view.findViewById(R.id.files);
                        Button button = new Button(c);
                        button.setText(name);
                        button.setBackground(c.getDrawable(R.drawable.file_button_bg));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Utils.convertDipToPix(40));
                        params.setMargins(Utils.convertDipToPix(3),Utils.convertDipToPix(3),0,Utils.convertDipToPix(2));
                        button.setLayoutParams(params);
                        button.setOnClickListener(Utils.openLink(lesson.files[1][e]));
                        layout.addView(button);
                        e++;

                    }
                }

            }
            views[i] = view;
            i++;
        }
        ready=true;
        return (View) views[a];
    }
}
