package com.provenlogic.mingle.Adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.provenlogic.mingle.Activities.RefillCreditsActivity;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.Gifts;
import com.provenlogic.mingle.R;

import java.util.ArrayList;

/**
 * Created by Anurag on 4/6/2017.
 */

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.MyViewHolder> {

    private ArrayList<Gifts> Gifts;
    private Activity mContext;

    public GiftAdapter(ArrayList<Gifts> Gifts, Activity mContext) {
        this.Gifts = Gifts;
        this.mContext = mContext;
    }

    @Override
    public GiftAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gift_row, parent, false);
        return new GiftAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GiftAdapter.MyViewHolder holder, int position) {
        com.provenlogic.mingle.Models.Gifts gift = Gifts.get(position);
        //holder.name.setText(user.getName());
        Glide.with(mContext).load(gift.getUrl()).placeholder(R.drawable.profile_placeholder).dontAnimate().into(holder.gift_image);

        if(position == 0){

        }
    }

    @Override
    public int getItemCount() {
        return Gifts.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView gift_image;
        public LinearLayout gift_layout;

        public MyViewHolder(View view) {
            super(view);
            gift_image = (ImageView) view.findViewById(R.id.gift_image);
            gift_layout = (LinearLayout) view.findViewById(R.id.gift_layout);
            gift_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    com.provenlogic.mingle.Models.Gifts gift = Gifts.get(getAdapterPosition());
                    if(gift.getPrice() < ApplicationSingleTon.Credits){
                        Intent data = new Intent();
                        data.putExtra("gift_id", gift.getId());
                        data.putExtra("gift_icon_name", gift.getIconName());
                        data.putExtra("gift_name", gift.getName());
                        data.putExtra("gift_price", gift.getPrice());
                        data.putExtra("gift_url", gift.getUrl());
                        mContext.setResult(Activity.RESULT_OK, data);
                        mContext.finish();
                    }else{
                        outOfCredits();
                    }
                }
            });
        }
    }

    /**
     *
     */
    private void outOfCredits(){
        new AlertDialog.Builder(mContext)
                .setTitle("Out pf credits")
                .setMessage("You don't have enough credits fo buy this gift.")
                .setPositiveButton("Buy Credits", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                        mContext.finish();
                        mContext.startActivity(new Intent(mContext, RefillCreditsActivity.class));
                        //DisplayActivity.getInstance().UpdateCreditsCount(ApplicationSingleTon.Credits);
                    }})
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mContext.finish();
                    }
                })
                .show();
    }

}
