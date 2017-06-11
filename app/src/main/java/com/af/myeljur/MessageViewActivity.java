package com.af.myeljur;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

public class MessageViewActivity extends AppCompatActivity {

    int id;
    boolean inbox;
    TextView sRs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            id = extras.getInt("id");
            inbox = extras.getBoolean("inbox");
        }else {
            Utils.failedToaster();
            finish();
        }
        final TextView date = (TextView) findViewById(R.id.mvMeta);
        final TextView subject = (TextView) findViewById(R.id.mvSubject);
        sRs = (TextView) findViewById(R.id.mvSenderOrReceivers);
        final TextView text = (TextView) findViewById(R.id.mvText);
        Messages messages = new Messages();
        messages.getMessage(id, new Messages.MessageCallback() {
            @Override
            public void Success(Messages.Message m) {
                findViewById(R.id.mvLoading).setVisibility(View.GONE);
                date.setText(m.date);
                subject.setText(m.subject);
                if(inbox){
                    if(m.receivers.size()==1){
                        sRs.setText(m.sender);
                    }else {
                        sRs.setText("Отправитель: "+m.sender+"\n"+"Коснитесь, чтобы посмотреть список получателей");
                        final String[] sAReceivers = m.receivers.toArray(new String[0]);

                        setSRSOCL(sAReceivers);
                    }

                }else {
                    if(m.receivers.size()==1){
                        sRs.setText("Получатель: "+m.receivers.get(0));
                    }else {
                        final String[] sAReceivers = m.receivers.toArray(new String[0]);
                        setSRSOCL(sAReceivers);
                        sRs.setText("Коснитесь, чтобы посмотреть список получателей");
                    }
                }
                text.setText(m.text);

                for(Messages.File f:m.files){
                    LinearLayout layout = (LinearLayout) findViewById(R.id.mvFiles);
                    Button button = new Button(MessageViewActivity.this);
                    button.setText(f.name);
                    button.setBackground(getDrawable(R.drawable.file_button_bg));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Utils.convertDipToPix(40));
                    params.setMargins(Utils.convertDipToPix(3),Utils.convertDipToPix(3),0,Utils.convertDipToPix(2));
                    button.setLayoutParams(params);
                    button.setOnClickListener(Utils.openLink(f.link));
                    layout.addView(button);
                }


            }

            @Override
            public void Fail() {
                Utils.failedToaster();
                finish();
            }
        });
    }

    void setSRSOCL(final String[] receivers){
        Arrays.sort(receivers);
        sRs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = new AlertDialog.Builder(MessageViewActivity.this).setItems(receivers,null).create();
                dialog.show();
            }
        });

    }

}
