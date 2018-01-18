package com.provenlogic.mingle.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.provenlogic.mingle.Models.ChatMessage;
import com.provenlogic.mingle.R;

import java.util.List;

/**
 * Created by himanshusoni on 06/09/15.
 */
public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
    private final int MY_MESSAGE = 0, OTHER_MESSAGE = 1, MY_IMAGE = 2, OTHER_IMAGE = 3;
    private int MY_GIFT = 4, OTHER_GIFT = 5;
    public ChatMessageAdapter(Context context, List<ChatMessage> data) {
        super(context, R.layout.item_mine_message, data);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage item = getItem(position);

        if (item.isMine() && !item.isImage() && !item.isGift()) return MY_MESSAGE;
        else if (!item.isMine() && !item.isImage() && !item.isGift()) return OTHER_MESSAGE;
        else if (item.isMine() && item.isImage() && !item.isGift()) return MY_IMAGE;
        else if(item.isMine() && item.isGift() && !item.isImage()) return MY_GIFT;
        else if(!item.isMine() && item.isGift() && !item.isImage()) return OTHER_GIFT;
        else return OTHER_IMAGE;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        if (viewType == MY_MESSAGE) {
            /**
             * Sending a text message
             */
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_message, parent, false);

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());

        } else if (viewType == OTHER_MESSAGE) {
            /**
             * Receiving a text message
             */
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_message, parent, false);

            TextView textView = (TextView) convertView.findViewById(R.id.text);
            textView.setText(getItem(position).getContent());
        } else if (viewType == MY_IMAGE) {
            /**
             * Sending an image
             */
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_image, parent, false);
            ImageView image = (ImageView) convertView.findViewById(R.id.image_holder);
            if(getItem(position).getImageBitmap() != null){
                image.setImageBitmap(getItem(position).getImageBitmap());
            }else{
                String url = getItem(position).getImageUrl();
                Glide.with(getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_launcher)
                        .dontAnimate()
                        .into(image);
            }
        } else if(viewType == OTHER_IMAGE){
            /**
             * Receiving an image
             */
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_image, parent, false);
            ImageView image = (ImageView) convertView.findViewById(R.id.image_holder);
            String url = getItem(position).getImageUrl();
            if(url != null) {
                Log.d("URL", url);
                Glide.with(getContext())
                        .load(url)
                        .placeholder(R.drawable.ic_launcher)
                        .dontAnimate()
                        .into(image);
            }
        }else if(viewType == MY_GIFT){
            /**
             * Sending the gift.
             */
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_mine_gift, parent, false);
            ImageView giftImage = (ImageView) convertView.findViewById(R.id.image_holder);
            String gift_url = getItem(position).getGiftUrl();
            if(gift_url != null){
                Glide.with(getContext())
                        .load(getItem(position).getGiftUrl())
                        .placeholder(R.drawable.ic_launcher)
                        .dontAnimate()
                        .into(giftImage);
            }
        }else if(viewType == OTHER_GIFT){
            /**
             * Receiving the gift.
             */
            Log.d("OTHERS GIFT", getItem(position).getGiftUrl());
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_other_gift, parent, false);
            ImageView giftImage = (ImageView) convertView.findViewById(R.id.image_holder);
            String gift_url = getItem(position).getGiftUrl();
            if(gift_url != null){
                Glide.with(getContext())
                        .load(getItem(position).getGiftUrl())
                        .placeholder(R.drawable.ic_launcher)
                        .dontAnimate()
                        .into(giftImage);
            }
        }

        return convertView;
    }
}
