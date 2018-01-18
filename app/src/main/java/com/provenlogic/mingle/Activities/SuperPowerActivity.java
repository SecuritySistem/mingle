package com.provenlogic.mingle.Activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.astuetz.PagerSlidingTabStrip;
import com.provenlogic.mingle.Adapters.SuperPowerPagerAdapter;
import com.provenlogic.mingle.Adapters.SuperPowerPaymentFragmentPagerAdapter;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.PaymentResolver;

/**
 * Created by Anurag on 4/10/2017.
 */

public class SuperPowerActivity extends AppCompatActivity{

    private ViewPager highlight;
    private TabLayout dots;

    private ViewPager paymentPager;
    private PagerSlidingTabStrip strip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_activity);

        PaymentResolver.setPaymentMode(PaymentResolver.PAYMENT_MODE.NONE);

        getSupportActionBar().setTitle("Super Power");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        highlight = (ViewPager) findViewById(R.id.dot_pager);
        dots = (TabLayout) findViewById(R.id.tabDots);

        dots.setupWithViewPager(highlight);
        highlight.setAdapter(new SuperPowerPagerAdapter(getSupportFragmentManager()));

        //getSuperPowerPackages();

        paymentPager = (ViewPager) findViewById(R.id.payment_pager);
        strip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        strip.setTextSize(HelperFunctions.ConvertDpToPx(this, 12));
        strip.setIndicatorColor(getResources().getColor(R.color.colorAccent));
        paymentPager.setAdapter(new SuperPowerPaymentFragmentPagerAdapter(getSupportFragmentManager()));
        strip.setViewPager(paymentPager);
        strip.setTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity.
        }
        return super.onOptionsItemSelected(item);
    }
}
