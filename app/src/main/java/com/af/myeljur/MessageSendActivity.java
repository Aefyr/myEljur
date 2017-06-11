package com.af.myeljur;

import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MessageSendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_send);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.msTabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.msViewPager);
        tabLayout.setupWithViewPager(viewPager);

        setTitle("Отправить сообщение");

        final MessageReceiversFragment f1 = new MessageReceiversFragment();
        MessageCreationFragment f2 = new MessageCreationFragment();
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(f1);
        fragments.add(f2);

        ArrayList<String> titles = new ArrayList<>();
        titles.add("Получатели");
        titles.add("Сообщение");

        ViewPagerFragmentAdapter adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(),fragments,titles);
        viewPager.setAdapter(adapter);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab2);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> receivers = f1.getCheckedIds();
                if(receivers.size()==0){
                    Utils.alertDialog(MessageSendActivity.this, "Нельзя отправить сообщение, не выбрав хотя бы одного получателя").show();
                    return;
                }
                String text = ((EditText) findViewById(R.id.msText)).getText().toString();
                String subject = ((EditText) findViewById(R.id.msSubject)).getText().toString();
                if(text.equals("")||subject.equals("")){
                    Utils.alertDialog(MessageSendActivity.this, "Нельзя отправить сообщение без темы или текста").show();
                    return;
                }
                final AlertDialog d = Utils.loadingDialog(MessageSendActivity.this);
                d.show();
                Messages m = new Messages();
                m.sendMessage(receivers, subject, text, new Messages.SendMessageCallback() {
                    @Override
                    public void Success() {
                        d.dismiss();
                        Toast.makeText(MessageSendActivity.this, "Сообщение отправлено", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void Fail() {
                        d.dismiss();
                        Toast.makeText(MessageSendActivity.this, "Ошибка при отправке сообщения", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                //f1.showMeSomeUsers();
            }
        });

    }

    void hideKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
