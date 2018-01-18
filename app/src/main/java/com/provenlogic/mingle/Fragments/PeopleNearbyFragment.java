package com.provenlogic.mingle.Fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.NearbyListAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.NotifyFilterChange;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.NearbyPeopleDecorator;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleNearbyFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int REQUEST_LOCATION_PERMISSION = 5674;

    private RecyclerView nearby_ppl;
    private NearbyListAdapter nearbyListAdapter;
    private ArrayList<userDetail> userDetailArrayList;
    private String id, token;
    private GoogleApiClient mGoogleApiClient;
    private ImageView myImage;

    private Location mLastLocation;

    public PeopleNearbyFragment() {
        // Required empty public constructor
    }

    private boolean loading;
    private int pastVisibleItem;
    private int visibleItemCount;
    private int totalItemCount;
    private String nextPageUrl;
    private NearbyPeopleDecorator itemDecorator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDetailArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_people_nearby, container, false);
        id = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.SESSION_TOKEN, "");
        nearby_ppl = (RecyclerView) view.findViewById(R.id.nearby_ppl);
        myImage = (ImageView) view.findViewById(R.id.myImage);

        Glide.with(this).load(ApplicationSingleTon.imageUrl).placeholder(R.drawable.user_profile).dontAnimate().into(myImage);

        nearbyListAdapter = new NearbyListAdapter(userDetailArrayList, getActivity());
        final GridLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(), 3);
        nearby_ppl.setLayoutManager(linearLayoutManager);
        nearby_ppl.setItemAnimator(new DefaultItemAnimator());
        itemDecorator = new NearbyPeopleDecorator(170);
        nearby_ppl.addItemDecoration(itemDecorator);
//        nearbyListAdapter.setHasStableIds(true);
        nearby_ppl.setAdapter(nearbyListAdapter);

        ((SimpleItemAnimator)nearby_ppl.getItemAnimator()).setSupportsChangeAnimations(false);
        nearby_ppl.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0){
                    //scrolled down
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                    if(!loading){
                        if((visibleItemCount + pastVisibleItem) >= totalItemCount){
                            loading = false;
                            Log.d("SCROLL", "MORE LOADING");
                            getMoreNearbyPeople();
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        return view;
    }

    @Override
    public void onResume() {
        mGoogleApiClient.connect();
        //BusProvider.getInstance().register(this);
        super.onResume();
        loading = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        //BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void getPeopleNearby() {
        loading = true;
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        if (ApplicationSingleTon.locationPreference == 0) {
            json.addProperty(Const.Params.LATITUDE, mLastLocation.getLatitude());
            json.addProperty(Const.Params.LONGITUDE, mLastLocation.getLongitude());
        }
        Ion.with(getActivity())
                .load(Endpoints.getPeopleNearbyUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        loading = false;
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

                            if (jsonObject.optString("status").equals("success")) {
                                JSONObject successData = jsonObject.getJSONObject("success_data");
                                JSONArray nearbyUsers = successData.getJSONArray("nearby_users");
                                for (int i = 0; i < nearbyUsers.length(); i++) {
                                    JSONObject userObj = nearbyUsers.getJSONObject(i);
                                    userDetail user = new userDetail();
                                    user.setId(userObj.optString("id"));
                                    user.setName(userObj.optString("name"));
                                    JSONObject pictObj = userObj.getJSONObject("profile_picture_url");
                                    user.setPicture(pictObj.optString("encounter"));
                                    userDetailArrayList.add(user);
                                    nearbyListAdapter.notifyItemInserted(nearbyListAdapter.getItemCount()+1);
                                }

                                /**
                                 * getting next page url for the pagination
                                 */
                                JSONObject paging = successData.getJSONObject("paging");
                                if(paging.getBoolean("more_pages")){
                                    nextPageUrl = paging.getString("next_page_url");
                               //     getMoreNearbyPeople();
                                    Log.d("NEXT", nextPageUrl);
                                }else{
                                    nextPageUrl = "";
                                }
                                //nearbyListAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    /**
     *
     */
    private void getMoreNearbyPeople(){
        if(nextPageUrl.isEmpty()){
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        if (ApplicationSingleTon.locationPreference == 0) {
            json.addProperty(Const.Params.LATITUDE, mLastLocation.getLatitude());
            json.addProperty(Const.Params.LONGITUDE, mLastLocation.getLongitude());
        }
        Ion.with(getActivity())
                .load(nextPageUrl)
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

                            if (jsonObject.optString("status").equals("success")) {
                                JSONObject successData = jsonObject.getJSONObject("success_data");
                                JSONArray nearbyUsers = successData.getJSONArray("nearby_users");
                                for (int i = 0; i < nearbyUsers.length(); i++) {
                                    JSONObject userObj = nearbyUsers.getJSONObject(i);
                                    userDetail user = new userDetail();
                                    user.setId(userObj.optString("id"));
                                    user.setName(userObj.optString("name"));
                                    JSONObject pictObj = userObj.getJSONObject("profile_picture_url");
                                    user.setPicture(pictObj.optString("encounter"));
                                    userDetailArrayList.add(user);
                                    nearbyListAdapter.notifyItemInserted(nearbyListAdapter.getItemCount()+1);
                                }

                                /**
                                 * getting next page url for the pagination
                                 */
                                JSONObject paging = successData.getJSONObject("paging");
                                if(paging.getBoolean("more_pages")){
                                    nextPageUrl = paging.getString("next_page_url");
                                }else{
                                    nextPageUrl = "";
                                }
                                //nearby_ppl.removeItemDecoration(itemDecorator);
                                //nearbyListAdapter.notifyDataSetChanged();
                                //nearby_ppl.addItemDecoration(itemDecorator);

                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mLastLocation == null)
            getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(View view) {

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
                getPeopleNearby();
                updateLocation();
            }
        }
    }

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

    @Subscribe
    public void onFilterChangeListener(NotifyFilterChange event) {
        userDetailArrayList.clear();

        getPeopleNearby();
    }

}
