package com.af.myeljur;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesListFragment extends Fragment {

    boolean launched;
    ListView listView;
    MessagesListAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton inboxOrSent;
    boolean inbox = true;
    TextView bar;

    private  boolean cancelled = false;

    @Override
    public void onDetach() {
        super.onDetach();
        cancelled = true;
        swipeRefreshLayout.setRefreshing(false);
    }

    public MessagesListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        if(!launched){
            getActivity().findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Utils.alertDialog(getContext(),"Тут должно было запуститься активити отправки сообщения, но его пока не существует :/").show();
                    Intent i = new Intent(getActivity(),MessageSendActivity.class);
                    getActivity().startActivity(i);
                }
            });
            inboxOrSent = (ImageButton) getActivity().findViewById(R.id.inboxOrSent);
            inboxOrSent.setImageResource(R.drawable.sent_icon);
            bar = (TextView) getActivity().findViewById(R.id.toolbarTitle);
            inboxOrSent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(inbox){
                        inbox = false;
                        inboxOrSent.setImageResource(R.drawable.inbox_icon);
                        bar.setText("Отправленные");
                    }else {
                        inbox = true;
                        inboxOrSent.setImageResource(R.drawable.sent_icon);
                        bar.setText("Входящие");
                    }
                    fetchMessages();
                }
            });
            swipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.messagesListLayout);
            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorNotSoBlue),getResources().getColor(R.color.colorPrettyOrange),getResources().getColor(R.color.colorLime),getResources().getColor(R.color.colorGreen));
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    fetchMessages();
                }
            });
            listView = (ListView) getView().findViewById(R.id.messagesListList);
            fetchMessages();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Messages.ShortMessage m = adapter.getMessage(i);
                    Intent messageView = new Intent(getContext(),MessageViewActivity.class);
                    messageView.putExtra("id",m.id);
                    messageView.putExtra("inbox", m.inbox);
                    getContext().startActivity(messageView);
                }
            });
            launched = true;
        }
        super.onStart();
    }

    void fetchMessages(){
        MainActivity.someFragmentIsUpdating = true;
        swipeRefreshLayout.setRefreshing(true);
        Messages messages = new Messages();
        messages.getShortMessages(inbox, false, new Messages.ShortMessagesCallback() {
            @Override
            public void returnMessages(ArrayList<Messages.ShortMessage> messages) {

                if(cancelled)
                    return;

                adapter = new MessagesListAdapter(messages,getContext(), getActivity());
                listView.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false);
                MainActivity.someFragmentIsUpdating = false;

            }

            @Override
            public void failedToReturnMessages() {

                if(cancelled)
                    return;

                swipeRefreshLayout.setRefreshing(false);
                Utils.failedToaster();
                MainActivity.someFragmentIsUpdating = false;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages_list, container, false);
    }

}
