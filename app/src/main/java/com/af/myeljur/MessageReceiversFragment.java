package com.af.myeljur;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageReceiversFragment extends Fragment {

    ExpandableListView expandableListView;
    ExpandableListAdapter adapter;
    boolean launched = false;

    public MessageReceiversFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_reveivers, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!launched){
            expandableListView = (ExpandableListView)getActivity().findViewById(R.id.msELV);

            HashMap<String, ArrayList<Messages.Person>> fakeC = new HashMap<>();

            ArrayList<Messages.Person> fakeC1 = new ArrayList<>();
            Messages.Person p = new Messages().new Person("11", "VASYA", "LOH");
            fakeC1.add(p);
            fakeC1.add(p);
            ArrayList<Messages.Person> fakeC2 = new ArrayList<>();
            fakeC2.add(p);

            ArrayList<String> fakeG = new ArrayList<>();
            fakeG.add("fakeG1");
            fakeG.add("fakeG2");

            fakeC.put(fakeG.get(0),fakeC1);
            fakeC.put(fakeG.get(1),fakeC2);

            /*adapter = new ExpandableListAdapter(getContext(),fakeG,fakeC);
            expandableListView.setAdapter(adapter);

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

                    adapter.children[i][i1].checked = !adapter.children[i][i1].checked;
                    ((CheckBox)view.findViewById(R.id.msCheckReceiver)).setChecked(adapter.children[i][i1].checked);
                    return false;
                }
            });*/
            fetchReceivers();
            launched = true;
        }
    }

    void showMeSomeUsers(){
        StringBuilder names = new StringBuilder();
        for (int x = 0;x<adapter.children.length;x++){
            for (int y = 0;y<adapter.children[x].length;y++){
                if(adapter.children[x][y].checked)
                    names.append(" "+adapter.children[x][y].person.name);

            }
        }
        Utils.alertDialog(getContext(),names.toString()).show();
    }

    ArrayList<String> getCheckedIds(){
        ArrayList<String> checked = new ArrayList<>();
        for (int x = 0;x<adapter.children.length;x++){
            for (int y = 0;y<adapter.children[x].length;y++){
                if(adapter.children[x][y].checked)
                    checked.add(adapter.children[x][y].person.id);

            }
        }
        return  checked;
    }

    void fetchReceivers(){
        Messages m = new Messages();
        final AlertDialog d = Utils.loadingDialog(getContext());
        d.show();
        m.getReceiversList(new Messages.ReceiversListCallback() {
            @Override
            public void Success(Messages.ReceiversContainer rc) {
                adapter = new ExpandableListAdapter(getContext(),rc.groups,rc.childrenGroups);
                expandableListView.setAdapter(adapter);
                expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                    @Override
                    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

                        adapter.children[i][i1].checked = !adapter.children[i][i1].checked;
                        ((CheckBox)view.findViewById(R.id.msCheckReceiver)).setChecked(adapter.children[i][i1].checked);
                        return false;
                    }
                });
                d.dismiss();
            }

            @Override
            public void Fail() {
                d.dismiss();
                Toast.makeText(getContext(), "Ошибка при получении списка пользователей", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
    }
}
