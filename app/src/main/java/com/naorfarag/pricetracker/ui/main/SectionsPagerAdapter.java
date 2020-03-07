package com.naorfarag.pricetracker.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.naorfarag.pricetracker.CartFragment;
import com.naorfarag.pricetracker.R;
import com.naorfarag.pricetracker.SearchFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    //@StringRes
    //private static final int[] TAB_TITLES = new int[]{R.string.tab_search, R.string.tab_tracklist};
    private final Context mContext;
    private final List<String> tabsTitle = new ArrayList<>();
    private final List<Fragment> fragmentList = new ArrayList<>();
    /*private SearchFragment sf = new SearchFragment();
    private CartFragment cf = new CartFragment();*/

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return fragmentList.get(position);
        /*switch(position){
            case 0:
                //SearchFragment sf = new SearchFragment();
                return sf;
            case 1:
                //CartFragment cf = new CartFragment();
                return cf;
        }
        return null;//PlaceholderFragment.newInstance(position + 1);*/
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabsTitle.get(position);//mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return fragmentList.size();
    }

    public void addFragment(Fragment fragment, String title){
        fragmentList.add(fragment);
        tabsTitle.add(title);
    }
}