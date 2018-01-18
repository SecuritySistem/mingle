package com.provenlogic.mingle.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.provenlogic.mingle.Models.PackageDetail;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.PaymentResolver;

import java.util.ArrayList;

/**
 * Created by Anurag on 4/7/2017.
 */

public class PaymentRecyclerAdapter extends RecyclerView.Adapter<PaymentRecyclerAdapter.MyHolder>{

    private ArrayList<String> packageList;
    private ArrayList<PackageDetail> packageDetailList;
    private int type;
    private int lastChecked;

    /**
     * S
     * @param type 0 for paypal and else for stripe.
     * @param packageList
     * @param packageDetailList
     */
    public PaymentRecyclerAdapter(int type, ArrayList<String> packageList, ArrayList<PackageDetail> packageDetailList){
        this.type = type;
        this.packageDetailList = packageDetailList;
        this.packageList = packageList;
        this.lastChecked = -1;
    }

    @Override
    public PaymentRecyclerAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.payment_row, parent, false);
        return new PaymentRecyclerAdapter.MyHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return packageDetailList.size();
    }

    @Override
    public void onBindViewHolder(PaymentRecyclerAdapter.MyHolder holder, int position) {
        holder.packageDetails.setText(packageList.get(position));
        holder.selected.setChecked( lastChecked == position);
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        public RadioButton selected;
        public LinearLayout main_layout;
        public TextView packageDetails;
        public MyHolder(View view) {
            super(view);
            packageDetails = (TextView) view.findViewById(R.id.package_details);
            selected = (RadioButton) view.findViewById(R.id.payment_selected);
            main_layout = (LinearLayout) view.findViewById(R.id.main_layout);
            main_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastChecked = getAdapterPosition();
                    notifyItemRangeChanged(0, packageDetailList.size());
                    SetupPaymentOptions(packageDetailList.get(getAdapterPosition()));
                }
            });

            selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastChecked = getAdapterPosition();
                    notifyItemRangeChanged(0, packageDetailList.size());
                    SetupPaymentOptions(packageDetailList.get(getAdapterPosition()));
                }
            });
        }
    }

    /**
     * Sets up the payment details for processing either from stripe or paypal, as per user's choice
     * @param details
     */
    private void SetupPaymentOptions(PackageDetail details){
        if(type == 0){
            PaymentResolver.setPaymentMode(PaymentResolver.PAYMENT_MODE.PAYPAL);
        }else{
            PaymentResolver.setPaymentMode(PaymentResolver.PAYMENT_MODE.STRIPE);
        }
        PaymentResolver.setAmount(details.getAmount());
        PaymentResolver.setCurrency(details.getCurrency());
    }
}
