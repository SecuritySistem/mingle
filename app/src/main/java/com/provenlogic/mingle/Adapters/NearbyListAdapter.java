package com.provenlogic.mingle.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.provenlogic.mingle.Activities.ProfileViewActivity;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.R;

import java.util.ArrayList;

/**
 * Created by amal on 30/08/16.
 */
public class NearbyListAdapter extends RecyclerView.Adapter<NearbyListAdapter.MyViewHolder> {

    private ArrayList<userDetail> userDetailArrayList;
    private Activity mContext;

    /**
     * This variable count tells the element position present in center of the grid layout.
     * At start CurrentPosition = 1 is the center.
     */
    private int CenterPosition = 1;
    private boolean GiveMargin = true;

    public NearbyListAdapter(ArrayList<userDetail> userDetailArrayList, Activity mContext) {
        this.userDetailArrayList = userDetailArrayList;
        this.mContext = mContext;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public NearbyListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nearby_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NearbyListAdapter.MyViewHolder holder,  int position) {
        userDetail user = userDetailArrayList.get(position);
        holder.name.setText(user.getName());
        Log.d("POS", "" + position);

        /**
         * This piece of code makes the central circle to be margined a bit.
         */
        /*
        if(CenterPosition == position){
            if(GiveMargin){
                LinearLayout.LayoutParams params =  (LinearLayout.LayoutParams) holder.profile_image.getLayoutParams();
                params.setMargins(0, 170, 0 , 0);
                holder.profile_image.setLayoutParams(params);
                GiveMargin = false;
            }else{
                //GiveMargin = true;
            }
            CenterPosition += 3;
        }else if(position > 2 ){
            int originalPos[] = new int[2];
            holder.profile_image.getLocationOnScreen( originalPos );
            TranslateAnimation anim = new TranslateAnimation( 0, originalPos[0] , 0, originalPos[1] -170 );
            anim.setDuration(1);
            anim.setFillAfter( true );
            holder.nearby_ppl_layout.startAnimation(anim);
        }*/

       // holder.nearby_ppl_layout.setPadding(HelperMethods.getPixelInDp(8,mContext),HelperMethods.getPixelInDp(8,mContext),HelperMethods.getPixelInDp(8,mContext),HelperMethods.getPixelInDp(14,mContext));
        Glide.with(mContext).load(user.getPicture()).placeholder(R.drawable.profile_placeholder).dontAnimate().into(holder.profile_image);
    }

    @Override
    public int getItemCount() {
        return userDetailArrayList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView profile_image;
        public TextView name;
        public LinearLayout nearby_ppl_layout;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            profile_image = (ImageView) view.findViewById(R.id.profile_image);
            nearby_ppl_layout = (LinearLayout) view.findViewById(R.id.nearby_ppl_layout);
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
