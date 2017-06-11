package com.af.myeljur;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    EditText schoolDomain;
    EditText username;
    EditText password;
    Button loginButton;
    ProgressDialog progressDialog;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        schoolDomain = (EditText) findViewById(R.id.editTextSchoolDomain);
        username = (EditText) findViewById(R.id.editTextUsername);
        password = (EditText) findViewById(R.id.editTextPassword);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(schoolDomain.getText().toString(), username.getText().toString(), password.getText().toString());
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Авторизация...");
    }
    boolean login(final String tDomain, String tUsername, String tPassword){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        progressDialog.show();
        App.getPreferences().put("domain", tDomain);
        EljurApiRequest request = new EljurApiRequest(EljurApiRequest.Method.LOGIN).addParameter("vendor", tDomain).addParameter("password", tPassword).addParameter("login", tUsername);
        EljurApi.login(request, new EljurApi.Callback() {
            @Override
            public void onSuccess() {
                progressDialog.setMessage("Синхронизация...");
                Utils.allSync(new EljurApi.Callback() {
                    @Override
                    public void onSuccess() {
                        progressDialog.cancel();
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onFail() {
                        Utils.alertDialog(LoginActivity.this, "Ошибка синхронизации");
                    }
                });
            }

            @Override
            public void onFail() {
                progressDialog.dismiss();
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(LoginActivity.this);
                aBuilder.setMessage("Неверный логин или пароль");
                aBuilder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog = aBuilder.create();
                alertDialog.show();
            }
        });

        return false;
    }

}
