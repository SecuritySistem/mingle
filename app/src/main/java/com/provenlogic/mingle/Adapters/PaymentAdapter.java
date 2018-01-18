package com.provenlogic.mingle.Adapters;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.provenlogic.mingle.Fragments.PaypalPaymentFragment;
import com.provenlogic.mingle.Fragments.StripePaymentFragment;

/**
 * Created by Anurag on 4/7/2017.
 */

public class PaymentAdapter extends FragmentPagerAdapter{

    private String[] Title = {"PAYPAL", "CREDIT CARD/ DEBIT CARD",};
    private PaypalPaymentFragment paypal;
    private StripePaymentFragment stripe;
    public PaymentAdapter(FragmentManager fm){
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 1){
            stripe = new StripePaymentFragment();
            return stripe;
        }else{
            paypal = new PaypalPaymentFragment();
            return paypal;
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
