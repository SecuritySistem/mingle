package com.provenlogic.mingle.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.provenlogic.mingle.Fragments.SuperPowerPaypalPaymentFragment;
import com.provenlogic.mingle.Fragments.SuperPowerStripePaymentFragment;

/**
 * Created by Anurag on 4/10/2017.
 */

public class SuperPowerPaymentFragmentPagerAdapter extends FragmentPagerAdapter{

    private String[] Title = {"PAYPAL", "CREDIT CARD/ DEBIT CARD",};


    public SuperPowerPaymentFragmentPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 1){
            return new SuperPowerStripePaymentFragment();
        }else{
            return new SuperPowerPaypalPaymentFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position){
        return Title[position];
    }

}
