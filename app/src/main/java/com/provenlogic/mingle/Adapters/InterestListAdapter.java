package com.provenlogic.mingle.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.provenlogic.mingle.Models.InterestInfo;
import com.provenlogic.mingle.R;

import java.util.List;

/**
 * Created by amal on 30/08/16.
 */
public class InterestListAdapter extends RecyclerView.Adapter<InterestListAdapter.MyViewHolder> {

    private List<InterestInfo> interestInfoList;
    private List<InterestInfo> full_interestInfoList;
    private Context mContext;

    public InterestListAdapter(List<InterestInfo> interestInfoList, List<InterestInfo> full_interestInfoList, Context mContext) {
        this.interestInfoList = interestInfoList;
        this.full_interestInfoList = full_interestInfoList;
        this.mContext = mContext;
    }

    @Override
    public InterestListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(InterestListAdapter.MyViewHolder holder, int position) {
        final InterestInfo interestInfo = interestInfoList.get(position);
        if (interestInfo!=null) {
            holder.interest_name.setText(interestInfo.getInterestName());
            if (interestInfo.isCommon()) {
                holder.interest_layout.setBackgroundResource(R.drawable.rounded_rectangle);
            }else holder.interest_layout.setBackgroundResource(R.drawable.border_stroke);
        }else {
            holder.interest_name.setText("\u2022 \u2022 \u2022");
            holder.interest_layout.setBackgroundResource(R.drawable.rounded_rectangle);
        }
        holder.interest_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (interestInfo==null){
                    interestInfoList = full_interestInfoList;
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return interestInfoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView interest_name;
        public LinearLayout interest_layout;

        public MyViewHolder(View view) {
            super(view);
            interest_name = (TextView) view.findViewById(R.id.interest_name);
            interest_layout = (LinearLayout) view.findViewById(R.id.interest_layout);
        }
    }
}
