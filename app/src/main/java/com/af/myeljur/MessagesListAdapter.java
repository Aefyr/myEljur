package com.af.myeljur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Peter on 19.02.2017.
 */

public class MessagesListAdapter extends BaseAdapter {

    ArrayList<Messages.ShortMessage> shortMessages;
    Context c;
    LayoutInflater inflater;
    Activity a;
    public MessagesListAdapter(ArrayList<Messages.ShortMessage> shortMessages, Context c, Activity a){
        this.shortMessages = shortMessages;
        this.c = c;
        this.a = a;
        inflater= (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    Messages.ShortMessage getMessage(int i){
        return (Messages.ShortMessage) getItem(i);
    }

    @Override
    public int getCount() {
        return shortMessages.size();
    }

    @Override
    public Object getItem(int i) {
        return shortMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return shortMessages.get(i).id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null){
            View v = inflater.inflate(R.layout.short_message, viewGroup,false);

            return  updateView(v,i);
        }else {
            return updateView(view,i);
        }

    }

    View updateView(View v, int i){
        final Messages.ShortMessage m = getMessage(i);
        if(i%2 != 0){
            v.findViewById(R.id.smLayout).setBackgroundResource(R.drawable.lit_sm_bg_selector);
        }else {
            v.findViewById(R.id.smLayout).setBackgroundResource(R.drawable.dark_sm_bg_selector);
        }
        String senderOrReceivers;
        if(m.senderOrReveivers.size()==1){
            senderOrReceivers = m.senderOrReveivers.get(0);
        }else {
            senderOrReceivers = "Получателей: "+m.senderOrReveivers.size();
        }
        ((TextView)v.findViewById(R.id.smName)).setText(senderOrReceivers);
        ((TextView)v.findViewById(R.id.smDate)).setText(m.date);
        ((TextView)v.findViewById(R.id.smPreviewText)).setText(m.shortText);
        ((TextView)v.findViewById(R.id.smSubject)).setText(m.subject);
        if(m.unread){
            v.findViewById(R.id.expCheck).setBackgroundResource(R.drawable.email);
        }else {
            v.findViewById(R.id.expCheck).setBackgroundResource(R.drawable.email_download);
        }
        if(m.withFiles){
            v.findViewById(R.id.smFiles).setVisibility(View.VISIBLE);
        }else {
            v.findViewById(R.id.smFiles).setVisibility(View.GONE);
        }

        return  v;
    }
}
