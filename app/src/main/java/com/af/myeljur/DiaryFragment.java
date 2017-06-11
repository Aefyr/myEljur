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


public class DiaryFragment extends Fragment {

    AppPreferences preferences;
    ListView daysList;
    DaysAdapter adapter;
    boolean launched=false;
    boolean ready=false;
    boolean a;
    float stY;
    UpdateIndicator ue;
    SwipeRefreshLayout swipeRefreshLayout;

    private  boolean cancelled = false;

    @Override
    public void onDetach() {
        super.onDetach();
        cancelled = true;
        //swipeRefreshLayout.setRefreshing(false);
    }

    public DiaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Оффлайн-режим", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.cancel();
            }
        });*/


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();


        if(!launched) {
            daysList = (ListView) getView().findViewById(R.id.Days);
            preferences = new AppPreferences(getContext().getApplicationContext());

            swipeRefreshLayout =(SwipeRefreshLayout) getActivity().findViewById(R.id.diaryLayout);
            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorNotSoBlue),getResources().getColor(R.color.colorPrettyOrange),getResources().getColor(R.color.colorLime),getResources().getColor(R.color.colorGreen));
            updateDiary(true,-1);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    updateDiary(EljurApi.offline(), -1);
                }
            });
            updateDiary(EljurApi.offline(),-1);
            launched = true;
            /*/Experimental
            int x = Utils.getScreenWidth(getActivity());
            ue = new UpdateIndicator(getContext(), null);
            FrameLayout layout = (FrameLayout) getView().findViewById(R.id.diaryLayout);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.width=200;
            params.height=200;
            params.setMargins(x/2-100,10,0,0);
            params.setMargins(x/2-100,0-200,0,0);
            ue.setLayoutParams(params);
            //layout.addView(ue);
            ue.setCallback(new UpdateIndicator.Callback() {
                @Override
                public void onUpdate() {
                    updateDiary(EljurApi.offline());
                }

                @Override
                public void onReady() {
                    ue.setListView(daysList,getActivity());
                    ue.setRevealed(0);
                    updateDiary(true);
                    updateDiary(EljurApi.offline());
                    launched=true;
                }
            });

            //Experimental END*/

        }

    }

    public void updateDiary(final boolean offline, final int weekChanger){


        swipeRefreshLayout.setRefreshing(true);
        MainActivity.someFragmentIsUpdating = true;
        if(!offline){
            EljurApi.getPeriods(new EljurApi.Callback() {
                @Override
                public void onSuccess() {
                    updateDiary2(false, weekChanger);
                }

                @Override
                public void onFail() {
                    swipeRefreshLayout.setRefreshing(false);
                    MainActivity.someFragmentIsUpdating = false;
                    Utils.failedToaster();
                }
            });
        }else {
            updateDiary2(true, weekChanger);
        }


    }

    void updateDiary2(boolean offline, final int i){
        EljurApi.resolveDiary(Utils.getCurrentWeek(), new EljurApi.ResolveDiaryCallback() {
            @Override
            public void onSuccess(ArrayList<Lesson> result) {

                if(cancelled)
                    return;

                adapter = new DaysAdapter(getContext(), result);
                daysList.setAdapter(adapter);
                if(ready){
                    swipeRefreshLayout.setRefreshing(false);
                    MainActivity.someFragmentIsUpdating = false;
                    //dialog.dismiss();
                    //handler.removeCallbacks(r);
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
                //api.fail(getContext()).show();
                if(i!= -1){
                    Utils.setWeeksOffset(i);
                }
            }
        }, offline);
    }

    public interface DiaryCallback{
        void OnLoad();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }


}
