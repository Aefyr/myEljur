package com.af.myeljur;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.grandcentrix.tray.AppPreferences;

import java.util.ArrayList;

public class MarksFragment extends Fragment {

    ListView view;
    boolean launched;
    boolean ready;

    AppPreferences preferences;
    MarksAdapter adapter;

    SwipeRefreshLayout swipeRefreshLayout;

    private  boolean cancelled = false;

    @Override
    public void onDetach() {
        super.onDetach();
        cancelled = true;
        swipeRefreshLayout.setRefreshing(false);
    }

    public MarksFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launched=false;
        ready=false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marks, container, false);
    }



    @Override
    public void onStart() {
        super.onStart();

            if (!launched) {
                preferences = new AppPreferences(getContext().getApplicationContext());
                view = (ListView) getView().findViewById(R.id.subjectsList);
                swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.marksLayout);
                swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorNotSoBlue),getResources().getColor(R.color.colorPrettyOrange),getResources().getColor(R.color.colorLime),getResources().getColor(R.color.colorGreen));
                updateMarks(true,-1);
                updateMarks(EljurApi.offline(),-1);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        updateMarks(EljurApi.offline(),-1);
                    }
                });
                launched = true;

        }


    }


    public void updateMarks(final boolean offline, final int p){
        swipeRefreshLayout.setRefreshing(true);
        MainActivity.someFragmentIsUpdating = true;
        if(!offline){
            EljurApi.getPeriods(new EljurApi.Callback() {
                @Override
                public void onSuccess() {
                    updateMarks2(false,p);
                }

                @Override
                public void onFail() {
                    Utils.failedToaster();
                    swipeRefreshLayout.setRefreshing(false);
                    MainActivity.someFragmentIsUpdating = false;
                }
            });
        }else updateMarks2(true,p);

    }

    void updateMarks2(boolean offline, final int p){
        EljurApi.resolveGrid(Utils.getCurrentPeriod(), new EljurApi.ResolveGridCallback() {
            @Override
            public void onSuccess(ArrayList<MarksGrid> result) {

                if(cancelled)
                    return;

                adapter = new MarksAdapter(getContext(),result);
                view.setAdapter(adapter);
                if(ready){
                    swipeRefreshLayout.setRefreshing(false);
                    MainActivity.someFragmentIsUpdating = false;
                }
                ready=true;
            }

            @Override
            public void onFail() {

                if(cancelled)
                    return;

                Utils.failedToaster();
                swipeRefreshLayout.setRefreshing(false);
                MainActivity.someFragmentIsUpdating = false;
                if(p!=-1){
                    Utils.setPeriodsOffset(p);
                }
            }
        },offline);
    }

}

