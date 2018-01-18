package com.provenlogic.mingle.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Activities.ChatActivity;
import com.provenlogic.mingle.Activities.ProfileActivity;
import com.provenlogic.mingle.Activities.ProfileViewActivity;
import com.provenlogic.mingle.Adapters.CardsAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.NotifyProfileRecieved;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.BusProvider;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;
import com.skyfishjy.library.RippleBackground;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.arjsna.swipecardlib.SwipeCardView;


public class DisplayFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 5674;
    private final int PROFILE_VIEW_CODE = 1276;

    private String id, token;
    private ArrayList<userDetail> userDetailArrayList;
    private ImageView centerImage;
    private CardsAdapter cardsAdapter;
    private SwipeCardView swipeCardView;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LinearLayout button_layout;
    private TextView no_match_text;
    private ImageView like_button, dislike_button;
    private RippleBackground loading_view;
    private FrameLayout housing_frame;
    private View view;
    private ViewGroup parent_linear_layout;
    private ImageView boost;

    private int[] status = {1/*like*/, 2/*dislike*/, 3/*super_like*/};

    private boolean isDataAvailable = false, isDownloadingData = false;
    private boolean encounterQuotaOver = false;

    private boolean fetchingNearbyPeople;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fetchingNearbyPeople = true;

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_display, container, false);
        parent_linear_layout = (ViewGroup) view.findViewById(R.id.parent);
        userDetailArrayList = new ArrayList<>();
        id = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.SESSION_TOKEN, "");

        centerImage = (ImageView) view.findViewById(R.id.centerImage);

        swipeCardView = (SwipeCardView) view.findViewById(R.id.frame);
        button_layout = (LinearLayout) view.findViewById(R.id.button_layout);
        no_match_text = (TextView) view.findViewById(R.id.no_match_text);
        like_button = (ImageView) view.findViewById(R.id.like_button);
        like_button.setOnClickListener(this);
        dislike_button = (ImageView) view.findViewById(R.id.dislike_button);
        dislike_button.setOnClickListener(this);
        loading_view = (RippleBackground) view.findViewById(R.id.loading_view);
        loading_view.startRippleAnimation();
        housing_frame = (FrameLayout) view.findViewById(R.id.housing_frame);
        boost = (ImageView) view.findViewById(R.id.boost);
        boost.setOnClickListener(this);
        setUpSwipableCard(swipeCardView);
        button_layout.setVisibility(View.GONE);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        //getLocation(LocationManager.NETWORK_PROVIDER);
        return view;
    }

    @Subscribe
    public void onProfileNotifyRecieved(NotifyProfileRecieved event) {
        Glide.with(this).load(ApplicationSingleTon.imageUrl).placeholder(R.drawable.user_profile).dontAnimate().into(centerImage);
    }

    private void setUpSwipableCard(final SwipeCardView swipeCardView) {
        cardsAdapter = new CardsAdapter(getActivity(), userDetailArrayList);
        swipeCardView.setAdapter(cardsAdapter);
        swipeCardView.setOnItemClickListener(new SwipeCardView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                userDetail user = (userDetail) dataObject;
                Intent intent = new Intent(getActivity(), ProfileViewActivity.class);
                intent.putExtra("suggestion_id", user.getId());
                startActivityForResult(intent, PROFILE_VIEW_CODE);
            }
        });
        swipeCardView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {

                userDetail user = (userDetail) dataObject;
                // lastUserDetail = user;
                sendLikeStatus(user.getId(), status[1]);
            }

            @Override
            public void onCardExitRight(Object dataObject) {
                userDetail user = (userDetail) dataObject;
                // lastUserDetail = user;
                sendLikeStatus(user.getId(), status[0]);

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (!userDetailArrayList.isEmpty()) {
                    if (!isDownloadingData)
                        fetchNearbyPeople();
                    if (itemsInAdapter == 0) {
                        button_layout.setVisibility(View.GONE);
                        if (!isDataAvailable) {
                            no_match_text.setVisibility(View.VISIBLE);

                        }
                    }
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = swipeCardView.getSelectedView();
                //ImageView like = (ImageView) view.findViewById(R.id.like);
                //ImageView dislike = (ImageView) view.findViewById(R.id.dislike);
                if (scrollProgressPercent > 0) {
                    int transparency = (int) (scrollProgressPercent * 255);
                    //like.setImageAlpha(transparency);
                }
                if (scrollProgressPercent < 0) {
                    int transparency = (int) (scrollProgressPercent * -255);
                    //dislike.setImageAlpha(transparency);
                }

                if (scrollProgressPercent == 0) {
                    //like.setImageAlpha(0);
                    //dislike.setImageAlpha(0);
                }

            }

            @Override
            public void onCardExitTop(Object dataObject) {
               /* userDetail user = (userDetail) dataObject;
                lastUserDetail = user;
                sendLikeStatus(user.getId(), status[2]);
                current_position++;*/
            }

            @Override
            public void onCardExitBottom(Object dataObject) {

            }
        });
    }

    private void showMatchDialog(final String match_name, final String match_picture, final String match_id, final String contact_id) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.match_dialog);

        ImageView myImage = (ImageView) dialog.findViewById(R.id.my_image);
        ImageView userImage = (ImageView) dialog.findViewById(R.id.user_image);
        TextView match_text = (TextView) dialog.findViewById(R.id.match_text);
        Button send_message, keep_swiping;
        send_message = (Button) dialog.findViewById(R.id.send_message);
        keep_swiping = (Button) dialog.findViewById(R.id.keep_swiping);
        Glide.with(this).load(ApplicationSingleTon.imageUrl).placeholder(R.drawable.user_profile).dontAnimate().into(myImage);
        Glide.with(this).load(match_picture).placeholder(R.drawable.user_profile).dontAnimate().into(userImage);
        match_text.setText("You and " + match_name + " have liked each other");
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("receiverId", match_id);
                intent.putExtra("receiverImageUrl", match_picture);
                intent.putExtra("receiverName", match_name);
                intent.putExtra("contact_id", contact_id);
                startActivity(intent);
            }
        });
        keep_swiping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    private void sendLikeStatus(String id, int status) {

        if (!ApplicationSingleTon.isEncounterAvailable){
            showQoutaOverDialog();
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, this.id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("encounter_id", id);
        if (status == 1)
            json.addProperty("like", "_like");
        if (status == 2)
            json.addProperty("like", "_dislike");

        Ion.with(this)
                .load(Endpoints.likeUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                if (successObj.optBoolean("match_found")) {
                                    JSONObject userObj = successObj.getJSONObject("user");
                                    JSONObject profileObj = userObj.getJSONObject("profile_pics");
                                    showMatchDialog(userObj.optString("name"), profileObj.optString("encounter"), userObj.optString("id"), successObj.optString("contact_id"));
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }


    @Override
    public void onResume() {
        mGoogleApiClient.connect();
        BusProvider.getInstance().register(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLastLocation == null){
            getLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                fetchNearbyPeople();
                updateLocation();
            }
        }
    }

    /**
     * Gets the current location of the user based on the provided network provider
     * @param provider
     * @return
     *//**
    private Location getLocation(String provider){
        long MIN_DISTANCE_FOR_UPDATE = 10;
        long MIN_TIME_FOR_UPDATE = 1000 * 60 * 2;
        final LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if(mLocationManager.isProviderEnabled(provider)){
            try{
                mLocationManager.requestLocationUpdates(provider, MIN_TIME_FOR_UPDATE, MIN_DISTANCE_FOR_UPDATE, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        mLastLocation = location;
                        fetchNearbyPeople();
                        updateLocation();
                        mLocationManager.removeUpdates(this);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });
            }catch(SecurityException e){

            }
        }
        return mLastLocation;
    }*/

    private void updateLocation() {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("latitude", mLastLocation.getLatitude());
        json.addProperty("longitude", mLastLocation.getLongitude());

        Ion.with(this)
                .load(Endpoints.updateLocation)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (result == null)
                            return;
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    getLocation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void fetchNearbyPeople() {

        fetchingNearbyPeople = true;

        if (!loading_view.isRippleAnimationRunning())
            loading_view.startRippleAnimation();

        isDownloadingData = true;
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        if (ApplicationSingleTon.locationPreference == 0) {
            json.addProperty(Const.Params.LATITUDE, mLastLocation.getLatitude());
            json.addProperty(Const.Params.LONGITUDE, mLastLocation.getLongitude());
        }


        Ion.with(this)
                .load(Endpoints.getEncountersUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        isDownloadingData = false;
                        loading_view.stopRippleAnimation();
                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObject = jsonObject.getJSONObject("success_data");
                                if (successObject.optInt("encounters_left") == 0) {
                                    ApplicationSingleTon.isEncounterAvailable = false;
                                    showQoutaOverDialog();
                                } else ApplicationSingleTon.isEncounterAvailable = true;
                                button_layout.setVisibility(View.VISIBLE);

                                no_match_text.setVisibility(View.GONE);
                                JSONArray jsonArray = successObject.optJSONArray("encouters");
                                if (jsonArray != null) {
                                    if (jsonArray.length() > 0) {
                                        isDataAvailable = true;
                                    } else {
                                        isDataAvailable = false;
                                        no_match_text.setVisibility(View.VISIBLE);
                                        button_layout.setVisibility(View.GONE);
                                    }
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject userObject = jsonArray.getJSONObject(i);
                                        userDetail detail = new userDetail();
                                        detail.setId(userObject.optString("id"));
                                        detail.setName(userObject.optString("name"));
                                        detail.setAge(userObject.optString("age"));
                                        detail.setDescription(userObject.optString("description"));
                                        JSONObject prof_pic_obj = userObject.optJSONObject("profile_picture_url");
                                        if (prof_pic_obj != null) {
                                            detail.setPicture(prof_pic_obj.optString("encounter"));
                                        }
                                        //  detail.setDistance(userObject.optString("distance"));
                                        userDetailArrayList.add(detail);
                                    }
                                    //  rippleBackground.stopRippleAnimation();
                                    cardsAdapter.notifyDataSetChanged();
                                    fetchingNearbyPeople = false;
                                    button_layout.setVisibility(View.VISIBLE);
                                }
                            } else {
                                //  rippleBackground.stopRippleAnimation();
                                no_match_text.setVisibility(View.VISIBLE);
                                button_layout.setVisibility(View.GONE);
                                Toast.makeText(getActivity(), jsonObject.optString(Const.Params.ERROR), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void showQoutaOverDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.likes_over, null);
        dialogBuilder.setView(dialogView);

        final TextView timeLeftText = (TextView) dialogView.findViewById(R.id.time_left);

        //Getting the time left till next likes become active
        android.text.format.Time time = new android.text.format.Time();
        time.setToNow();
        long currentTime = time.hour*60*60 + time.minute*60 + time.second;
        long remmainingTime = (24*60*60) - currentTime;

        Log.d("C TIME" , currentTime + "");
        Log.d("R TIME" , remmainingTime + "");
        Log.d("CALC", "" + 24*60*60);

        //Countdown till like feature becomes active
        final CountDownTimer timer = new CountDownTimer(remmainingTime*1000 , 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftText.setText(HelperFunctions.ConvertSecondsToHMmSs(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {

            }
        };


        final AlertDialog alertDialog = dialogBuilder.create();
        LinearLayout purchase_super_power = (LinearLayout) dialogView.findViewById(R.id.purchase_superpower);
        purchase_super_power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                startActivity(new Intent(getActivity(), ProfileActivity.class));

                //stopping the timer
                timer.cancel();
            }
        });
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        alertDialog.show();
        timer.start();
    }

    @Override
    public void onClick(View view) {
        View view1 = swipeCardView.getSelectedView();
        switch (view.getId()) {
            case R.id.like_button:
                if(!fetchingNearbyPeople){
                    //Still fetching results
                    //ImageView like = (ImageView) view1.findViewById(R.id.like);
                    //like.setImageAlpha(255);
                    swipeCardView.throwRight();
                }
                break;
            case R.id.dislike_button:
                if(!fetchingNearbyPeople){
                    //Still fetching results
                    //ImageView dislike = (ImageView) view1.findViewById(R.id.dislike);
                    //dislike.setImageAlpha(255);
                    swipeCardView.throwLeft();
                }
                break;
            case R.id.boost:
                getBoostCredits();
                break;
        }
    }

    private void getBoostCredits() {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        Ion.with(this)
                .load(Endpoints.checkBoost)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                if (jsonObject.optBoolean("profile_boosted")) {
                                    Toast.makeText(getActivity(), "Your profile is already boosted.", Toast.LENGTH_LONG).show();
                                } else {
                                    if (jsonObject.optInt("user_credit_balance") >= jsonObject.optInt("boost_credits")) {
                                        showBoostConfirmationDialog(jsonObject.optInt("boost_credits"));
                                    } else {
                                        Toast.makeText(getActivity(), "You do not have enough credit balance to purchase boost", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void showBoostConfirmationDialog(int credit) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Activate Boost")
                .setMessage("Activate Boost for " + credit + " credit(s).")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activateBoost();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void activateBoost() {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(getActivity(), "Activating boost...");
        progressDialog.show();
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        Ion.with(getActivity())
                .load(Endpoints.activateBoost)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        progressDialog.dismiss();
                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                Toast.makeText(getActivity(), successObj.optString("success_text"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_VIEW_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final View view1 = swipeCardView.getSelectedView();
                Handler handler = new Handler();
                String status = data.getStringExtra("action");
                if (status.equals("like")) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //ImageView like = (ImageView) view1.findViewById(R.id.like);
                            //like.setImageAlpha(255);
                            swipeCardView.throwRight();
                        }
                    }, 500);

                } else if (status.equals("dislike")) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //ImageView dislike = (ImageView) view1.findViewById(R.id.dislike);
                            //dislike.setImageAlpha(255);
                            swipeCardView.throwLeft();
                        }
                    }, 500);

                }
            }
        }

    }
    /*
    @Subscribe
    public void onFilterChangeListener(NotifyFilterChange event) {
        userDetailArrayList.clear();
        cardsAdapter.notifyDataSetChanged();
        button_layout.setVisibility(View.GONE);

        userDetailArrayList = new ArrayList<>();
        housing_frame.removeView(swipeCardView);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.swipe_layout, parent_linear_layout, false);
        SwipeCardView swipeCardView = new SwipeCardView(getActivity());
        swipeCardView.setMaxVisible(1);

        FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        //   newParams.addRule(RelativeLayout.ALIGN_BOTTOM,button_layout.getId());
        // parent_view.addView(swipeCardView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        housing_frame.addView(swipeCardView, newParams);

        setUpSwipableCard(swipeCardView);
        fetchNearbyPeople();
    }
    */
}
