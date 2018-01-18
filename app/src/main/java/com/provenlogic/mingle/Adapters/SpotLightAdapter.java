package com.provenlogic.mingle.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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
 * Created by Anurag on 04/04/17.
 */
public class SpotLightAdapter extends RecyclerView.Adapter<SpotLightAdapter.MyViewHolder> {

    private ArrayList<userDetail> userDetailArrayList;
    private Activity mContext;


    public SpotLightAdapter(ArrayList<userDetail> userDetailArrayList, Activity mContext) {
        this.userDetailArrayList = userDetailArrayList;
        this.mContext = mContext;
    }

    @Override
    public SpotLightAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nearby_row_drawer, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SpotLightAdapter.MyViewHolder holder, int position) {
        userDetail user = userDetailArrayList.get(position);
        holder.name.setText(user.getName());
        Glide.with(mContext).load(user.getPicture()).placeholder(R.drawable.profile_placeholder).dontAnimate().into(holder.profile_image);

        if(position == 0){

        }
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
                    if(getAdapterPosition() != 0){
                        userDetail user = userDetailArrayList.get(getAdapterPosition());
                        Intent intent = new Intent(mContext, ProfileViewActivity.class);
                        intent.putExtra("suggestion_id", user.getId());
                        mContext.startActivity(intent);
                    }else{
                        //Do nothing.....
                        //It is handled in the Display Activity.
                    }
                }
            });
        }
    }
}
