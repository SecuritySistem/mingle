package com.provenlogic.mingle.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.provenlogic.mingle.Activities.AccountActivity;
import com.provenlogic.mingle.Activities.BasicInfoActivity;
import com.provenlogic.mingle.R;

/**
 * Created by amal on 30/08/16.
 */
public class MySettingListAdapter extends RecyclerView.Adapter<MySettingListAdapter.MyViewHolder> {

    private String[] settingList;
    private Activity mContext;

    public MySettingListAdapter(String[] settingList, Activity mContext) {
        this.settingList = settingList;
        this.mContext = mContext;
    }

    @Override
    public MySettingListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MySettingListAdapter.MyViewHolder holder, final int position) {
        holder.title.setText(settingList[position]);
        holder.setting_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                switch (position){
                    case 0:
                        intent = new Intent(mContext, BasicInfoActivity.class);
                        mContext.startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(mContext, AccountActivity.class);
                        mContext.startActivity(intent);
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return settingList.length;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public LinearLayout setting_layout;


        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            setting_layout = (LinearLayout) view.findViewById(R.id.setting_layout);
        }
    }
}
