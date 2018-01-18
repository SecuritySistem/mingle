package com.provenlogic.mingle.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.provenlogic.mingle.R;

/**
 * Created by amal on 15/02/17.
 */
public class PurposeListAdapter extends BaseAdapter {
    private static LayoutInflater inflater = null;
    String[] result;
    Context context;
    int[] imageId;

    public PurposeListAdapter(Context context, String[] prgmNameList, int[] prgmImages) {
        // TODO Auto-generated constructor stub
        result = prgmNameList;
        this.context = context;
        imageId = prgmImages;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.pupose_list_list, null);
        holder.purpose_text = (TextView) rowView.findViewById(R.id.purpose_text);
        holder.purpose_image = (ImageView) rowView.findViewById(R.id.purpose_image);
        holder.purpose_text.setText(result[position]);
        holder.purpose_image.setImageResource(imageId[position]);

        return rowView;
    }

    public class Holder {
        TextView purpose_text;
        ImageView purpose_image;
    }

}
