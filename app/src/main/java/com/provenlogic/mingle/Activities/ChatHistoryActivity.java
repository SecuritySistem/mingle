package com.provenlogic.mingle.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.MessageListAdapter;
import com.provenlogic.mingle.Models.MessageThread;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatHistoryActivity extends AppCompatActivity {

    String id, token;
    private ArrayList<MessageThread> messageThreadArrayList;
    private LinearLayout no_message_layout;
    private RecyclerView chat_list;
    private MessageListAdapter messageListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        messageThreadArrayList = new ArrayList<>();
        chat_list = (RecyclerView) findViewById(R.id.chat_list);
        no_message_layout = (LinearLayout) findViewById(R.id.no_message_layout);
        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        setUpRecyclerView();
        getAllMessages();
    }

    private void getAllMessages() {

        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID,id);
        json.addProperty(Const.Params.TOKEN,token);

        Ion.with(this)
                .load(Endpoints.getMessageUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ChatHistoryActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONArray messageArray = jsonObject.optJSONArray("chat_users");
                                if (messageArray != null) {
                                    for (int i = 0; i < messageArray.length(); i++) {
                                        JSONObject messageObject = messageArray.getJSONObject(i);
                                        JSONObject userObject = messageObject.getJSONObject("user");
                                        MessageThread messageThread = new MessageThread();
                                        messageThread.setUserId(userObject.optString("id"));
                                        messageThread.setName(userObject.optString("name"));
                                        messageThread.setPicture(userObject.optString("profile_picture"));
                                        messageThread.setLastMessage(userObject.optString("last_msg"));
                                        messageThread.setContactId(messageObject.optString("contact_id"));
                                        messageThreadArrayList.add(messageThread);
                                    }
                                }

                                messageListAdapter.notifyDataSetChanged();


                                if (messageThreadArrayList.isEmpty()) {
                                    no_message_layout.setVisibility(View.VISIBLE);

                                } else {
                                    no_message_layout.setVisibility(View.GONE);

                                }

                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

    }

    private void setUpRecyclerView() {
        messageListAdapter = new MessageListAdapter(messageThreadArrayList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        chat_list.setLayoutManager(mLayoutManager);
        chat_list.setItemAnimator(new DefaultItemAnimator());
        chat_list.setAdapter(messageListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
