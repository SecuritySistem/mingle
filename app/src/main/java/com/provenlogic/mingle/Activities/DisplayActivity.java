package com.provenlogic.mingle.Activities;

import android.app.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.SpotLightAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Fragments.DisplayFragment;
import com.provenlogic.mingle.Fragments.PeopleNearbyFragment;
import com.provenlogic.mingle.Models.NotifyFilterChange;
import com.provenlogic.mingle.Models.NotifyProfileRecieved;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Services.PushNotificationService;
import com.provenlogic.mingle.Utils.BusProvider;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;
import com.provenlogic.mingle.Utils.SpotlightHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DisplayActivity extends AppCompatActivity
        implements View.OnClickListener {


    private int SUPERPOWER_REQUEST = 101;
    private final int FILTER_CODE = 2745;
    private String id, token;
    private TextView user_name, superPowerActive, credit;
    private ImageView user_image, user_settings, popularity_level;

    private static DisplayActivity _Activity;

    /**
     * The singleton class for the default activity
     * @return the activity instance
     */
    public static DisplayActivity getInstance(){
        return _Activity;
    }

    /**
     * The button representing me on the navbar view in the spotlight members list
     */
    private Button Me;

    private boolean PlayRippleEffect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        //Singleton instance
        _Activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.app_name);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        Log.d("USERID", id);
        Log.d("TOKEN", token);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Do whatever you want here

                //Since the drawer is closed so no need to play the ripple effect at all.
                PlayRippleEffect = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Do whatever you want here

                //As drawer is opened start the ripple effect on the Me button
                PlayRippleEffect = true;
                ForceRippleAnimation(Me);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //navigationView.setNavigationItemSelectedListener(this);
        View HeaderView = navigationView.getHeaderView(0);
        user_image = (ImageView) HeaderView.findViewById(R.id.user_image);
        user_name = (TextView) HeaderView.findViewById(R.id.user_name);
        user_settings = (ImageView) HeaderView.findViewById(R.id.user_settings);
        popularity_level = (ImageView) HeaderView.findViewById(R.id.popularity_level);
        credit = (TextView) HeaderView.findViewById(R.id.credits);
        superPowerActive = (TextView) HeaderView.findViewById(R.id.super_active);

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DisplayActivity.this, ProfileActivity.class));
            }
        });
        user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DisplayActivity.this, ProfileActivity.class));
            }
        });
        user_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DisplayActivity.this, SettingsActivity.class));
            }
        });

        popularity_level.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DisplayActivity.this, PopularityActivity.class));
            }
        });



        Me = (Button) findViewById(R.id.ripple_effect);
        /**
         * Click handler for Me button in Nav view
         * Starts the api call to add user to the spotlight and handles the appropriate errors.
         */
        Me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotlightHelper.AddMeToSpotLight(_Activity);
            }
        });

        /**
         * Click handler for credits and super power in the nav bar
         */
        LinearLayout creditsLayout = (LinearLayout) HeaderView.findViewById(R.id.credit_header);
        LinearLayout superPowerLayout = (LinearLayout) HeaderView.findViewById(R.id.super_power_header);
        creditsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DisplayActivity.this, RefillCreditsActivity.class));
            }
        });
        superPowerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(DisplayActivity.this, SuperPowerActivity.class), SUPERPOWER_REQUEST);
            }
        });


        getMyProfile();
        gotoFragment();

        //Starting the push notification service.
        Intent notificationService = new Intent(DisplayActivity.this, PushNotificationService.class);
        startService(notificationService);
    }

    private void gotoFragment() {
        DisplayFragment fragment = new DisplayFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.display_container, fragment).commit();
    }


    private void getMyProfile() {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        Ion.with(this)
                .load(Endpoints.myProfileUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, DisplayActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                JSONObject userObj = successObj.getJSONObject("user");
                                ApplicationSingleTon.name = userObj.optString("name");
                                user_name.setText(ApplicationSingleTon.name);
                                ApplicationSingleTon.Credits = Integer.parseInt(userObj.optString("credits", "0"));
                                credit.setText("" + ApplicationSingleTon.Credits);

                                if (userObj.optString("superpower_activated").equals("true")) {
                                    superPowerActive.setText("Active");
                                    superPowerActive.setTextColor(getResources().getColor(R.color.white));
                                } else {
                                    superPowerActive.setText("Inactive");
                                    superPowerActive.setTextColor(getResources().getColor(R.color.colorAccent));
                                }
                                JSONObject profileObj = userObj.getJSONObject("profile_pic_url");
                                ApplicationSingleTon.imageUrl = profileObj.optString("encounter");
                                Glide.with(DisplayActivity.this).load(ApplicationSingleTon.imageUrl).placeholder(R.drawable.profile_placeholder).dontAnimate().into(user_image);
                                JSONObject popularityObj = successObj.optJSONObject("user_popularity");
                                if (popularityObj != null) {
                                    switch (popularityObj.optString("popularity_type")) {
                                        case "very_very_low":
                                            popularity_level.setImageResource(R.drawable.battery_10);
                                            break;
                                        case "very_low":
                                            popularity_level.setImageResource(R.drawable.battery_20);
                                            break;
                                        case "low":
                                            popularity_level.setImageResource(R.drawable.battery_30);
                                            break;
                                        case "medium":
                                            popularity_level.setImageResource(R.drawable.battery_50);
                                            break;
                                        case "high":
                                            popularity_level.setImageResource(R.drawable.battery);
                                            break;
                                    }
                                }
                                BusProvider.getInstance().post(new NotifyProfileRecieved());

                                //Fetching the spot light data after user profile is loaded successfully.
                                getSpotLightData();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivityForResult(new Intent(DisplayActivity.this, FilterActivity.class), FILTER_CODE);
            return true;
        } else if (id == R.id.action_chat) {
            startActivity(new Intent(this, ChatHistoryActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onClick (View view) {
        // Handle navigation view item clicks here.
        int id = view.getId();

        if (id == R.id.nav_message) {
            startActivity(new Intent(this, ChatHistoryActivity.class));
        } else if (id == R.id.nav_invite) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=com.provenlogic.voodoo");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (id == R.id.nav_tnc) {
            String url = "http://staging.datingframework.com/terms-and-conditions";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (id == R.id.nav_about) {
            String url = "http://staging.datingframework.com/about-us";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (id == R.id.nav_nearby) {
            PeopleNearbyFragment newFragment = new PeopleNearbyFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.display_container, newFragment);

            transaction.commit();
        }else if (id==R.id.nav_encounter){
            DisplayFragment newFragment = new DisplayFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.display_container, newFragment);

            transaction.commit();
        }else if (id==R.id.nav_activity){
            startActivity(new Intent(this,ActivityPageActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILTER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Boolean status = data.getBooleanExtra("is_updated", false);
                if (status) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getInstance().post(new NotifyFilterChange());
                        }
                    }, 500);

                }
            }
        }

        //Some purchase was made in the super power activity.
        if(requestCode == SUPERPOWER_REQUEST){
            if(resultCode == RESULT_OK){

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        //ChatManager.Init(DisplayActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        //ChatManager.Destroy();
    }

    /**
     * Function to fetch the spotlight members and display them in
     * navigation bar.
     * Author ANURAG
     */
    private void getSpotLightData(){
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        Ion.with(this)
                .load(Endpoints.spotlight)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(result == null)
                            return;

                        ArrayList<userDetail> userDetailArrayList  = new ArrayList<userDetail>();
                        try{
                            JSONObject data = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(data, DisplayActivity.this)){
                                return;
                            }

                            JSONObject obj = data.getJSONObject("success_data");
                            JSONArray arr = obj.getJSONArray("spotlight_users");
                            userDetail me = new userDetail();
                            me.setName("Add me");
                            me.setPicture(ApplicationSingleTon.imageUrl);


                            userDetailArrayList.add(me);
                            for(int i = 0 ; i < arr.length() ; ++i){
                                JSONObject ob = arr.getJSONObject(i);
                                String name = ob.getString("name");
                                JSONObject ob1 = ob.getJSONObject("profile_picture_url");
                                String url = ob1.getString("thumbnail");
                                int id = ob.getInt("id");
                                userDetail user = new userDetail();
                                user.setName(name);
                                user.setPicture(url);
                                user.setId(""+id);
                                userDetailArrayList.add(user);
                            }
                            RecyclerView spot = (RecyclerView) findViewById(R.id.spotlight);
                            GridLayoutManager linearLayoutManager = new GridLayoutManager(DisplayActivity.this, 3);
                            spot.setNestedScrollingEnabled(false);
                            spot.setLayoutManager(linearLayoutManager);
                            spot.setItemAnimator(new DefaultItemAnimator());
                            spot.setAdapter(new SpotLightAdapter(userDetailArrayList, DisplayActivity.this));

                            //Changing the visibility of the views after the spotlight data is fetched
                            // eg after data is fetched hiding the progressbar from the nav view.

                            TextView loadingText = (TextView) findViewById(R.id.loading_spotlight_msg);
                            ProgressBar loadingProgress = (ProgressBar) findViewById(R.id.loading_spotlight_progress);
                            ImageView addImage = (ImageView) findViewById(R.id.add_Image);

                            loadingProgress.setVisibility(View.GONE);
                            loadingText.setVisibility(View.GONE);
                            spot.setVisibility(View.VISIBLE);
                            addImage.setVisibility(View.VISIBLE);
                            Me.setVisibility(View.VISIBLE);
                        }catch (JSONException eq){
                            //Toast.makeText(getApplicationContext(), eq.toString() , Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * This function updates the user's credits count after any purchase or deduction.
     */
    public void UpdateCreditsCount(int newCredits){
        credit.setText("" + newCredits);
    }

    /**
     * This function updates the super power status
     * @param string new status
     */
    public void UpdateSuperPowerStatus(String string, boolean active){
        superPowerActive.setText(string);
        if(active){
            superPowerActive.setTextColor(getResources().getColor(R.color.white));
        }else{
            superPowerActive.setTextColor(Color.RED);
        }
    }


    /**
     * This function recursively plays the ripple effect on the view
     * @param view on which ripple animation has to be played.
     */
    private void ForceRippleAnimation(final View view){
        if(!PlayRippleEffect){
            return;
        }
        Drawable background = view.getBackground();
        if(Build.VERSION.SDK_INT >= 21 && background instanceof RippleDrawable)
        {
            final RippleDrawable rippleDrawable = (RippleDrawable) background;

            rippleDrawable.setState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled});

            Handler handler = new Handler();

            handler.postDelayed(new Runnable()
            {
                @Override public void run()
                {
                    rippleDrawable.setState(new int[]{});
                    ForceRippleAnimation(view);
                }
            }, 1500);
        }
    }
}
