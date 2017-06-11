package com.af.myeljur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Peter on 25.02.2017.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {


    LayoutInflater inflater;
    Context c;
    ArrayList<String> groups;
    HashMap<String,ArrayList<Messages.Person>> childrenGroups;
    boolean[] checkedGroups;
    Child[][] children;
    View[][] views;


    class Child{
        View self;
        View parent;
        boolean checked = false;
        Messages.Person person;
        public Child(View self, View parent, Messages.Person person){
            this.self = self;
            this.parent = parent;
            this.person = person;
        }
    }

    public ExpandableListAdapter(Context c, ArrayList<String> groups, HashMap<String,ArrayList<Messages.Person>> childrenGroups){
        this.c = c;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groups = groups;
        this.childrenGroups = childrenGroups;
        checkedGroups = new boolean[groups.size()];
        children = new Child[groups.size()][];
        views = new View[groups.size()][];
        for (int i = 0;i<groups.size();i++){
            views[i] = new View[childrenGroups.get(groups.get(i)).size()];
            children[i] = new Child[childrenGroups.get(groups.get(i)).size()];
        }

        for (int x = 0;x<children.length;x++){
            for (int y = 0;y<children[x].length;y++){
                children[x][y] = new Child(null,null,(Messages.Person) getChild(x,y));
            }
        }


    }


    int checkedCount(){
        int i = 0;
        for (int x = 0;x<children.length;x++){
            for (int y = 0;y<children[x].length;y++){
                if(children[x][y].checked)
                    i++;
            }
        }
        return i;
    }


    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return childrenGroups.get(groups.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return groups.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return (childrenGroups.get(groups.get(i))).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        View v;
        if(view == null){
            v = inflater.inflate(R.layout.receivers_group,viewGroup,false);
        }else {

            v = view;
        }
        View o = updateGroup(v,i);
        for(Child c:children[i]){
            c.parent = o;
        }
        return o;
    }



    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        return createChild(i,i1);
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


    private View createChild(final int i, final int i1){
        if(views[i][i1]!=null){
            return views[i][i1];
        }
        View v = inflater.inflate(R.layout.receivers_child, null);

        ((TextView)v.findViewById(R.id.receiversChild)).setText(((Messages.Person) getChild(i,i1)).name);
        ((CheckBox)v.findViewById(R.id.msCheckReceiver)).setChecked(children[i][i1].checked);
        ((CheckBox)v.findViewById(R.id.msCheckReceiver)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                children[i][i1].checked = b;
            }
        });

        children[i][i1].self = v;
        views[i][i1] = v;

        return  v;

    }

    View updateGroup(View v, final int i){
        ((TextView)v.findViewById(R.id.receiversGroup)).setText((String)getGroup(i));
        ((CheckBox)v.findViewById(R.id.msCheckAll)).setOnCheckedChangeListener(null);
        ((CheckBox)v.findViewById(R.id.msCheckAll)).setChecked(checkedGroups[i]);
        ((CheckBox)v.findViewById(R.id.msCheckAll)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for(Child c:children[i]){
                    if(c.self!=null){
                        ((CheckBox)c.self.findViewById(R.id.msCheckReceiver)).setChecked(b);
                        c.self.invalidate();
                    }
                    c.checked = b;
                    checkedGroups[i] = b;

                }
            }
        });

        return v;
    }
}
