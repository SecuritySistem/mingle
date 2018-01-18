package com.provenlogic.mingle.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.provenlogic.mingle.Fragments.HighlightFragment;

/**
 * This is the adapter class showing the highlighted content in view pager in RefillCreditsActivity class.
 * Created by Anurag on 4/7/2017.
 */

public class HighlightAdapter extends FragmentPagerAdapter{

    public HighlightAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment frag = new HighlightFragment();
        return frag;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
