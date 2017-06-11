package com.af.myeljur;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleFragment extends Fragment {

    boolean launched = false;
    ScheduleAdapter adapter;
    ListView view;
    UpdateIndicator ue;
    boolean ready = false;
    SwipeRefreshLayout swipeRefreshLayout;


    private  boolean cancelled = false;

    @Override
    public void onDetach() {
        super.onDetach();
        cancelled = true;
        swipeRefreshLayout.setRefreshing(false);
    }

    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //setScheduleT(null);
        if(!launched){
            view = (ListView) getView().findViewById(R.id.scheduleGrid);
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.scheduleLayout);
            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorNotSoBlue),getResources().getColor(R.color.colorPrettyOrange),getResources().getColor(R.color.colorLime),getResources().getColor(R.color.colorGreen));
            updateSchedule(true);
            updateSchedule(EljurApi.offline());
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    updateSchedule(EljurApi.offline());
                }
            });
            launched = true;
        }


    }
    public void updateSchedule(boolean offline){
        swipeRefreshLayout.setRefreshing(true);
        MainActivity.someFragmentIsUpdating = true;
        EljurApi.resolveSchedule(new EljurApi.ResolveScheduleCallback() {
            @Override
            public void onSuccess(ArrayList<Day> result) {

                if(cancelled)
                    return;

                adapter = new ScheduleAdapter(getContext(), result);
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
            }
        },offline);

    }


}
