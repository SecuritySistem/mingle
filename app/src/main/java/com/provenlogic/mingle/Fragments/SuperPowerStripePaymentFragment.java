package com.provenlogic.mingle.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.provenlogic.mingle.Activities.StripePaymentActivity;
import com.provenlogic.mingle.Adapters.SuperPowerPremiumPackageAdapter;
import com.provenlogic.mingle.Models.PackageDetail;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.PaymentResolver;

import java.util.ArrayList;

/**
 * Created by Anurag on 4/10/2017.
 */

public class SuperPowerStripePaymentFragment extends SuperPowerPaymentFragment implements SuperPowerPaymentFragment.SuperPowerDetailsFetchedListener{

    private View view;
    private ListView ls;

    public SuperPowerStripePaymentFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        GetSuperPowerPackages(this);
        view = inflater.inflate(R.layout.super_power_payment_fragment, null, false);
        ls = (ListView) view.findViewById(R.id.list_view);
        return view;
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onResultAvailable(ArrayList<String> packageList, final ArrayList<PackageDetail> packageDetailList) {
        ls.setAdapter(new SuperPowerPremiumPackageAdapter(getActivity(), packageList, packageDetailList));
        LinearLayout progress = (LinearLayout) view.findViewById(R.id.loading_layout);
        progress.setVisibility(View.GONE);

        ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PaymentResolver.setPaymentMode(PaymentResolver.PAYMENT_MODE.STRIPE);
                PaymentResolver.setCurrency(packageDetailList.get(position).getCurrency());
                PaymentResolver.setAmount(packageDetailList.get(position).getAmount());
                PaymentResolver.setPackageId(packageDetailList.get(position).getId());
                startActivity(new Intent(getActivity(), StripePaymentActivity.class));
            }
        });

        ls.setVisibility(View.VISIBLE);
    }
}
