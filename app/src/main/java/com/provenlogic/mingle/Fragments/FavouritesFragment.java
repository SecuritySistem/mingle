package com.provenlogic.mingle.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.ActivityListAdapter;
import com.provenlogic.mingle.Models.userDetail;
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
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityListAdapter nearbyListAdapter;
    private ArrayList<userDetail> userDetailArrayList;
    private String id, token;
    private TextView empty_text;
    private ProgressBar progess;

    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userDetailArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        id = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.SESSION_TOKEN, "");
        recyclerView = (RecyclerView) view.findViewById(R.id.nearby_ppl);
        empty_text = (TextView) view.findViewById(R.id.empty_text);
        progess = (ProgressBar) view.findViewById(R.id.progess);
        nearbyListAdapter = new ActivityListAdapter(userDetailArrayList, getActivity());
        GridLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(nearbyListAdapter);

        getFavourites();


        return view;
    }

    private void getFavourites() {
        userDetailArrayList.clear();
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        progess.setVisibility(View.VISIBLE);

        Ion.with(getActivity())
                .load(Endpoints.MyLikeUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        progess.setVisibility(View.GONE);
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
                                JSONArray userArray = successObj.getJSONArray("my_liked_users");
                                for (int i = 0; i < userArray.length(); i++) {
                                    JSONObject userObj = userArray.getJSONObject(i);
                                    userDetail user = new userDetail();
                                    user.setId(userObj.optString("id"));
                                    user.setName(userObj.optString("name"));
                                    JSONObject pictureObj = userObj.getJSONObject("profile_picture_url");
                                    user.setPicture(pictureObj.optString("encounter"));
                                    user.setShould_show(true);
                                    userDetailArrayList.add(user);
                                }
                                if (userDetailArrayList.isEmpty())
                                    empty_text.setVisibility(View.VISIBLE);
                                else empty_text.setVisibility(View.GONE);

                                nearbyListAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }


}
