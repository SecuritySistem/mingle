package com.provenlogic.mingle.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.provenlogic.mingle.Models.PackageDetail;
import com.provenlogic.mingle.R;

import java.util.ArrayList;

/**
 * Created by Anurag on 4/10/2017.
 */

public class SuperPowerPremiumPackageAdapter  extends BaseAdapter{

    private ArrayList<String> packageList;
    private ArrayList<PackageDetail> packageDetailList;
    private Activity activity;

    public SuperPowerPremiumPackageAdapter(Activity activity, ArrayList<String> packageList,
                                           ArrayList<PackageDetail> packageDetailList){
        this.activity = activity;
        this.packageDetailList = packageDetailList;
        this.packageList = packageList;
    }

    @Override
    public int getCount() {
        return packageDetailList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        view = View.inflate(activity, R.layout.super_power_package_list_item, null );
        TextView day = (TextView) view.findViewById(R.id.days);
        TextView amount = (TextView) view.findViewById(R.id.money);
        day.setText(packageDetailList.get(position).getPackname_name());
        amount.setText(packageDetailList.get(position).getAmount() + " " + packageDetailList.get(position).getCurrency());
        return view;
    }
}
