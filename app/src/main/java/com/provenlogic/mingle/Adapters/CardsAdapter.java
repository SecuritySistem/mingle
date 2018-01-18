package com.provenlogic.mingle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.R;

import java.util.ArrayList;

/**
 * Created by amal on 21/01/17.
 */
public class CardsAdapter extends ArrayAdapter<userDetail> {

    private final ArrayList<userDetail> userDetailArrayList;
    private final LayoutInflater layoutInflater;
    private Context mContext;

    public CardsAdapter(Context context, ArrayList<userDetail> cards) {
        super(context, -1);
        mContext = context;
        this.userDetailArrayList = cards;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        userDetail card = userDetailArrayList.get(position);
        View view = layoutInflater.inflate(R.layout.single_card, parent, false);
        ImageView user_image = (ImageView) view.findViewById(R.id.user_image);
        TextView user_name = (TextView) view.findViewById(R.id.user_name);
        TextView user_age = (TextView) view.findViewById(R.id.user_age);
        ImageView like = (ImageView) view.findViewById(R.id.like);
        ImageView dislike = (ImageView) view.findViewById(R.id.dislike);

        like.setImageAlpha(0);
        dislike.setImageAlpha(0);
        Glide.with(mContext).load(card.getPicture()).placeholder(R.drawable.profile_placeholder).into(user_image);
        user_name.setText(card.getName() + ",");
        user_age.setText(card.getAge());

        return view;
    }

    @Override
    public userDetail getItem(int position) {
        return userDetailArrayList.get(position);
    }

    @Override
    public int getCount() {
        return userDetailArrayList.size();
    }


}
