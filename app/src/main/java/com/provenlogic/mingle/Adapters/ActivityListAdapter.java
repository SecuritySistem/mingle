package com.provenlogic.mingle.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.enrique.stackblur.StackBlurManager;
import com.provenlogic.mingle.Activities.ProfileViewActivity;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.R;

import java.util.ArrayList;

/**
 * Created by amal on 30/08/16.
 */
public class ActivityListAdapter extends RecyclerView.Adapter<ActivityListAdapter.MyViewHolder> {

    private ArrayList<userDetail> userDetailArrayList;
    private Activity mContext;

    public ActivityListAdapter(ArrayList<userDetail> userDetailArrayList, Activity mContext) {
        this.userDetailArrayList = userDetailArrayList;
        this.mContext = mContext;
    }

    @Override
    public ActivityListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ActivityListAdapter.MyViewHolder holder, int position) {
        final userDetail user = userDetailArrayList.get(position);

        // holder.nearby_ppl_layout.setPadding(HelperMethods.getPixelInDp(8,mContext),HelperMethods.getPixelInDp(8,mContext),HelperMethods.getPixelInDp(8,mContext),HelperMethods.getPixelInDp(14,mContext));
        Glide.with(mContext)
                .load(user.getPicture())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        // Bitmap bluredBitmap = HelperMethods.blurBitmap(resource,mContext);
                        if (!user.isShould_show()) {
                            StackBlurManager _stackBlurManager = new StackBlurManager(resource);
                            _stackBlurManager.process(50);
                            holder.profile_image.setImageBitmap(_stackBlurManager.returnBlurredImage());
                            holder.nearby_ppl_layout.setOnClickListener(null);
                        } else {
                            holder.profile_image.setImageBitmap(resource);
                            holder.name.setText(user.getName());
                        }
                    }
                });

        //  Glide.with(mContext).load(user.getPicture()).placeholder(R.drawable.profile_placeholder).dontAnimate().into(holder.profile_image);
    }

    @Override
    public int getItemCount() {
        return userDetailArrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView profile_image;
        public TextView name;
        public RelativeLayout nearby_ppl_layout;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            profile_image = (ImageView) view.findViewById(R.id.profile_image);
            nearby_ppl_layout = (RelativeLayout) view.findViewById(R.id.nearby_ppl_layout);
            nearby_ppl_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userDetail user = userDetailArrayList.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, ProfileViewActivity.class);
                    intent.putExtra("suggestion_id", user.getId());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
