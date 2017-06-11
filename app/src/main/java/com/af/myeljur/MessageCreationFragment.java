package com.af.myeljur;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class MessageCreationFragment extends Fragment {

    boolean launched = false;
    public MessageCreationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_creation, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!launched) {
            launched = true;
        }
    }
}
