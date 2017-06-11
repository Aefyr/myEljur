package com.af.myeljur;

import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import net.grandcentrix.tray.AppPreferences;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    AppPreferences prefs;
    TextView studentName;
    NavigationView navigationView;
    TextView bar;
    int cId = -1;
    ImageButton timePeriodSwitchButton;
    ImageButton inboxOrSent;
    FloatingActionButton fab;
    FragmentManager fragmentManager;
    public static boolean someFragmentIsUpdating = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //App.initialize(getApplicationContext());
        App.initialize(getApplicationContext());
        EljurApi.initialize();
        prefs = App.getPreferences();
        if(!prefs.getBoolean("loggedIn", false)){
            Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        fragmentManager = getSupportFragmentManager();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_camera);
        studentName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.studentName);
        navigationView.getHeaderView(0).findViewById(R.id.buttonSelectStudent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!EljurApi.offline()){
                Utils.studentChoice(MainActivity.this, new Utils.Callback() {
                    @Override
                    public void onClick(int i) {
                        drawer.closeDrawers();
                        updateInfo();
                        switch (cId){
                            case 0:
                                ((DiaryFragment)fragmentManager.findFragmentByTag("D")).updateDiary(false,-1);
                                break;
                            case 1:
                                ((MarksFragment)fragmentManager.findFragmentByTag("M")).updateMarks(false, -1);
                                break;
                            case 2:
                                ((ScheduleFragment)fragmentManager.findFragmentByTag("S")).updateSchedule(false);
                                break;
                            case 3:
                                ((MessagesListFragment)fragmentManager.findFragmentByTag("mLF")).fetchMessages();
                                break;
                            case 4:
                                ((BriefFragment)fragmentManager.findFragmentByTag("B")).getBrief();
                        }
                    }
                }).show();
                }else {
                    Utils.alertDialog(MainActivity.this, "Невозможно сменить ученика в оффлайн режиме").show();
                }
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);

        bar = (TextView)findViewById(R.id.toolbarTitle);
        timePeriodSwitchButton = (ImageButton) findViewById(R.id.weekButton);
        inboxOrSent = (ImageButton) findViewById(R.id.inboxOrSent);
        timePeriodSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TransitionDrawable transition = (TransitionDrawable) timePeriodSwitchButton.getBackground();
                transition.startTransition(100);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        transition.reverseTransition(100);
                    }
                }, 100);
                if(!EljurApi.offline()&&cId==0) {
                    Utils.weekChoice(MainActivity.this, new Utils.Callback() {
                        @Override
                        public void onClick(int i) {
                            ((DiaryFragment)fragmentManager.findFragmentByTag("D")).updateDiary(false, i);
                        }
                    }).show();
                }else if(!EljurApi.offline()&&cId==1) {
                    Utils.periodChoice(MainActivity.this, new Utils.Callback() {
                        @Override
                        public void onClick(int i) {
                            ((MarksFragment)fragmentManager.findFragmentByTag("M")).updateMarks(false, i);
                        }
                    }).show();
                } else {
                    Utils.alertDialog(MainActivity.this, "Невозможно сменить неделю/период в оффлайн режиме").show();
                }
            }
        });
        if(savedInstanceState!=null){
            swicthFragment(savedInstanceState.getInt("cId"));
        }else {
            swicthFragment(0);
        }
        updateInfo();


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("cId", cId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        /*if(someFragmentIsUpdating){
            navigationView.setCheckedItem(cId);
            return true;
        }*/
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            if(cId!=0)
                swicthFragment(0);
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            if(cId!=1)
                swicthFragment(1);
        } else if (id == R.id.nav_slideshow) {
            if(cId!=2)
                swicthFragment(2);
        } else if (id == R.id.nav_manage) {
            if(cId!=3)
                swicthFragment(3);

        } else if (id == R.id.nav_brief) {
            if(cId!=4){
                swicthFragment(4);
            }

        }else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            prefs.clear();
            getSharedPreferences("WP", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("SD", MODE_PRIVATE).edit().clear().apply();
            Intent loginActivity = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //0=Diary, 1=Marks, 2=schedule
    void swicthFragment(int id){
        //timePeriodSwitchButton.setVisibility(View.GONE);

        inboxOrSent.setVisibility(View.GONE);
        switch (id){
            case 0:
                timePeriodSwitchButton.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
                DiaryFragment dF = new DiaryFragment();
                DiaryFragment currentD = (DiaryFragment) fragmentManager.findFragmentByTag("D");
                if(currentD==null){
                    fragmentManager.beginTransaction().replace(R.id.content_main, dF, "D").commit();
                }else {
                    //Utils.failedToaster();
                }


                bar.setText("Дневник");
                navigationView.setCheckedItem(R.id.nav_camera);
                cId = 0;
                break;
            case 1:
                timePeriodSwitchButton.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);
                MarksFragment mF = new MarksFragment();
                MarksFragment currentM = (MarksFragment) fragmentManager.findFragmentByTag("M");
                if(currentM==null){
                    fragmentManager.beginTransaction().replace(R.id.content_main, mF, "M").commit();
                }else {
                    //Utils.failedToaster();
                }
                bar.setText("Текущие оценки");
                navigationView.setCheckedItem(R.id.nav_gallery);
                cId = 1;
                break;
            case 2:
                timePeriodSwitchButton.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                ScheduleFragment sF = new ScheduleFragment();
                ScheduleFragment currentS = (ScheduleFragment) fragmentManager.findFragmentByTag("S");
                if(currentS==null){
                    fragmentManager.beginTransaction().replace(R.id.content_main, sF, "S").commit();
                }else {
                    //Utils.failedToaster();
                }
                bar.setText("Расписание");
                navigationView.setCheckedItem(R.id.nav_slideshow);
                cId = 2;
                break;
            case 3:
                inboxOrSent.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
                timePeriodSwitchButton.setVisibility(View.GONE);
                MessagesListFragment mLF = new MessagesListFragment();
                MessagesListFragment currentMl = (MessagesListFragment) fragmentManager.findFragmentByTag("ML");
                if(currentMl==null){
                    fragmentManager.beginTransaction().replace(R.id.content_main, mLF, "ML").commit();
                }else {
                    //Utils.failedToaster();
                }
                bar.setText("Входящие");
                navigationView.setCheckedItem(R.id.nav_manage);
                cId = 3;
                break;
            case 4:
                timePeriodSwitchButton.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
                BriefFragment bF = new BriefFragment ();
                BriefFragment currentB = (BriefFragment) fragmentManager.findFragmentByTag("B");
                if(currentB==null){
                    fragmentManager.beginTransaction().replace(R.id.content_main, bF, "B").commit();
                }else {
                    //Utils.failedToaster();
                }
                bar.setText("Brief");
                navigationView.setCheckedItem(R.id.nav_brief);
                cId = 4;
                break;
        }
    }

    void updateInfo(){
        studentName.setText(Utils.getCurrentStudentName());

    }




}
