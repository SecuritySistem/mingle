package com.provenlogic.mingle.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.provenlogic.mingle.Activities.ChatActivity;
import com.provenlogic.mingle.Models.MessageThread;
import com.provenlogic.mingle.R;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Created by amal on 30/08/16.
 */
public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MyViewHolder> {

    DateTimeFormatter dtf = DateTimeFormat.forPattern("dd MMM yy, h:mm a");
    private List<MessageThread> messageThreadList;
    private Context mContext;

    public MessageListAdapter(List<MessageThread> messageThreadList, Context mContext) {
        this.messageThreadList = messageThreadList;
        this.mContext = mContext;
    }

    @Override
    public MessageListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MessageListAdapter.MyViewHolder holder, int position) {
        final MessageThread messageThread = messageThreadList.get(position);
        holder.name.setText(messageThread.getName());
        holder.last_msg.setText(messageThread.getLastMessage());
        Glide.with(mContext).load(messageThread.getPicture()).placeholder(R.drawable.user_profile).dontAnimate().into(holder.picture);

        holder.message_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("receiverId", messageThread.getUserId());
                intent.putExtra("receiverImageUrl", messageThread.getPicture());
                intent.putExtra("receiverName", messageThread.getName());
                intent.putExtra("contact_id",messageThread.getContactId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageThreadList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView last_msg;
        public TextView name;
        private LinearLayout message_layout;
        private ImageView picture;

        public MyViewHolder(View view) {
            super(view);
            last_msg = (TextView) view.findViewById(R.id.last_msg);
            name = (TextView) view.findViewById(R.id.name);
            message_layout = (LinearLayout) view.findViewById(R.id.message_layout);
            picture = (ImageView) view.findViewById(R.id.picture);
        }
    }
}
