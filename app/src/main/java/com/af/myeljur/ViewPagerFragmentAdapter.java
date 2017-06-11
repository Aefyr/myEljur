package com.af.myeljur;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Peter on 25.02.2017.
 */

public class ViewPagerFragmentAdapter extends FragmentStatePagerAdapter {

    ArrayList<Fragment> fragments;
    ArrayList<String> titles;

    public  ViewPagerFragmentAdapter(FragmentManager fm,ArrayList<Fragment> fragments,ArrayList<String> titles){
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
