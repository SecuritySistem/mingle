package com.provenlogic.mingle.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.GiftAdapter;
import com.provenlogic.mingle.Models.Gifts;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

/**
 * This activity shows the gifts available.
 * Created by Anurag on 4/6/2017.
 */

public class GiftActivity extends AppCompatActivity{

    private ArrayList<Gifts> Gifts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift);
        getSupportActionBar().setTitle("Choose a gift");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        GetAllAvailableGifts();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
           finish(); // close this activity and return to preview activity.
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function gets the gifts available from the server.
     */
    private void GetAllAvailableGifts(){
        JsonObject json = new JsonObject();
        String id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        Ion.with(this)
                .load(Endpoints.allGifts)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        if(result == null){
                            Log.d("GIFTS" , "Result is null");
                            return;
                        }

                        try{
                            Log.d("GIFTS" , result.toString());
                            JSONObject obj = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(obj, GiftActivity.this)){
                                return;
                            }

                            if(obj.getString("success").equals("true")){
                                JSONArray gifts = obj.getJSONArray("gifts");
                                Gifts = new ArrayList<com.provenlogic.mingle.Models.Gifts>();
                                for(int i = 0 ; i < gifts.length() ; ++i){
                                    JSONObject oneGift = gifts.getJSONObject(i);
                                    String id = oneGift.getString("id");
                                    String name = oneGift.getString("name");
                                    String  icon_name = oneGift.getString("icon_name");
                                    String url = oneGift.getString("icon_url");
                                    int price = oneGift.getInt("price");
                                    Gifts.add(new Gifts(id, name, icon_name, url, price));
                                }

                                LinearLayout progressLayout = (LinearLayout) findViewById(R.id.loading_layout);
                                progressLayout.setVisibility(View.GONE);

                                RecyclerView gfts = (RecyclerView) findViewById(R.id.gifts);
                                GridLayoutManager linearLayoutManager = new GridLayoutManager(GiftActivity.this, 4);
                                gfts.setLayoutManager(linearLayoutManager);
                                gfts.setItemAnimator(new DefaultItemAnimator());

                                gfts.setAdapter(new GiftAdapter(Gifts, GiftActivity.this));
                            }
                        }catch(JSONException eq){
                            Log.d("GIFT ERROR", eq.toString());
                        }
                    }
                });
    }
}
