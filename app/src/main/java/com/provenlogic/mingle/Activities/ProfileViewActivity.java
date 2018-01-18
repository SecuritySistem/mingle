package com.provenlogic.mingle.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.InterestListAdapter;
import com.provenlogic.mingle.Adapters.ProfileImagesViewPagerAdapter;
import com.provenlogic.mingle.Models.InterestInfo;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SeekArc;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileViewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final int EDIT_PROFILE_CODE = 12;
    boolean result = false;
    private String suggestion_id;
    private ViewPager intro_images;
    private LinearLayout pager_indicator, button_layout, interest_layout, score_layout, verify_layout;
    private ProfileImagesViewPagerAdapter mAdapter;
    private ImageView[] dots;
    private ImageView chatMsgBtn;
    private int dotsCount;
    private ArrayList<String> images;
    private TextView name;
    private TextView age, school, work;
    private TextView distance;
    private RecyclerView interest_list;
    private ArrayList<InterestInfo> interestInfoArrayList, fullInterestInfoArrayList;
    private InterestListAdapter interestListAdapter;
    private SeekArc scoreArc;
    private TextView score_text, like_number, verified_text;
    private Button report_button;
    private int reason_number = 0;
    private TextView image_number,place_name;

    private String id, token, fromWhere;
    private ImageView dislike_button, like_button, verified_image;

    private boolean isSpinnerSet = false;

    /**
     * Other user's profile info.
     */
    private String userId;
    private String userName;
    private String profileImageUrl;
    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_profile);
        getSupportActionBar().hide();

        interestInfoArrayList = new ArrayList<>();
        fullInterestInfoArrayList = new ArrayList<>();
        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        images = new ArrayList<>();
        suggestion_id = getIntent().getExtras().getString("suggestion_id");
        try {
            fromWhere = getIntent().getExtras().getString("from");
        } catch (Exception e) {
            e.printStackTrace();
        }
        intro_images = (ViewPager) findViewById(R.id.images_viewpager);
        pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
        name = (TextView) findViewById(R.id.name);
        age = (TextView) findViewById(R.id.age);
        school = (TextView) findViewById(R.id.school);
        work = (TextView) findViewById(R.id.work);
        distance = (TextView) findViewById(R.id.distance);
        interest_list = (RecyclerView) findViewById(R.id.interest_list);

        interest_layout = (LinearLayout) findViewById(R.id.interest_layout);
        score_layout = (LinearLayout) findViewById(R.id.score_layout);
        verify_layout = (LinearLayout) findViewById(R.id.verify_layout);
        image_number = (TextView) findViewById(R.id.image_number);

        button_layout = (LinearLayout) findViewById(R.id.button_layout);
        score_text = (TextView) findViewById(R.id.score_text);
        like_number = (TextView) findViewById(R.id.like_number);
        report_button = (Button) findViewById(R.id.report_button);
        report_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportDialog();
            }
        });

        like_button = (ImageView) findViewById(R.id.like_button);
        dislike_button = (ImageView) findViewById(R.id.dislike_button);
        scoreArc = (SeekArc) findViewById(R.id.seekArc);
        scoreArc.setMax(10);
        scoreArc.setArcWidth(10);
        scoreArc.setProgressWidth(10);
        scoreArc.setRoundedEdges(true);
        scoreArc.setSweepAngle(320);
        scoreArc.setProgress(5);
        scoreArc.setStartAngle(20);
        scoreArc.setProgressColor(getResources().getColor(R.color.violet));
        scoreArc.setArcColor(getResources().getColor(R.color.black_100));
        verified_text = (TextView) findViewById(R.id.verified_text);
        verified_image = (ImageView) findViewById(R.id.verified_image);
        place_name = (TextView) findViewById(R.id.place_name);

        chatMsgBtn = (ImageView) findViewById(R.id.start_chat);
        chatMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        like_button.setOnClickListener(this);
        dislike_button.setOnClickListener(this);
        setUpInterestRecyclerView();
        setUpProfileImages();
        getProfileInfo();
      /*  if (fromWhere.equals("discover")) {

            button_layout.setVisibility(View.VISIBLE);

        } else if (fromWhere.equals("account")) {
            button_layout.setVisibility(View.GONE);

        }*/
    }

    private void setUpInterestRecyclerView() {
        interestListAdapter = new InterestListAdapter(interestInfoArrayList, fullInterestInfoArrayList, this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        interest_list.setLayoutManager(mLayoutManager);
        interest_list.setItemAnimator(new DefaultItemAnimator());
        interest_list.setAdapter(interestListAdapter);
    }

    private void report() {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("reported_user_id", suggestion_id);
        json.addProperty("reason", getResources().getStringArray(R.array.report_reasons)[reason_number]);

        Ion.with(this)
                .load(Endpoints.blockUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ProfileViewActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.optJSONObject("success_data");
                                if (successObj != null)
                                    Toast.makeText(ProfileViewActivity.this, successObj.optString("success_text"), Toast.LENGTH_SHORT).show();
                            } else {
                                JSONObject errorObj = jsonObject.optJSONObject("error_data");
                                if (errorObj != null)
                                    Toast.makeText(ProfileViewActivity.this, errorObj.optString("error_text"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Loads the other user's profile.
     */
    private void getProfileInfo() {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("view_user_id", suggestion_id);

        images.clear();
        Ion.with(this)
                .load(Endpoints.viewUserUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        progressDialog.dismiss();
                        if (result == null)
                            return;
                        Log.d("USERINFO", result.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ProfileViewActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObject = jsonObject.getJSONObject("success_data");
                                JSONObject userObject = successObject.getJSONObject("user");
                                name.setText(userObject.optString("name") + ", ");
                                age.setText(userObject.optString("age"));
                                if (successObject.optString("work").isEmpty()) {
                                    work.setVisibility(View.GONE);
                                } else {
                                    work.setVisibility(View.VISIBLE);
                                    work.setText(successObject.optString("work"));
                                }
                                if (successObject.optString("school").isEmpty()) {
                                    school.setVisibility(View.GONE);
                                } else {
                                    school.setVisibility(View.VISIBLE);
                                    school.setText(successObject.optString("school"));
                                }

                                //Getting user's profile info
                                userName = userObject.optString("name");
                                userId = userObject.getString("id");
                                JSONObject profilePicture = userObject.getJSONObject("profile_pic_url");
                                profileImageUrl = profilePicture.getString("thumbnail");
                                contactId = "";
                                String gender = userObject.optString("gender");
                                if (userObject.optString("verified").equalsIgnoreCase("verified")) {
                                    boolean isAny = false;
                                    if (userObject.optString("register_from").equalsIgnoreCase("facebook")) {
                                        verified_text.setText("Facebook");
                                        verified_image.setImageResource(R.drawable.facebook);
                                        isAny = true;
                                    }
                                    if (userObject.optString("register_from").equalsIgnoreCase("google")) {
                                        verified_text.setText("Google");
                                        verified_image.setImageResource(R.drawable.google_plus);
                                        isAny = true;
                                    }
                                    if (!isAny)
                                        verify_layout.setVisibility(View.GONE);
                                } else {
                                    verify_layout.setVisibility(View.GONE);
                                }
                                JSONObject distanceObject = successObject.optJSONObject("distance");
                                if (distanceObject != null) {
                                    double dist = distanceObject.optDouble("value");
                                    String unit = distanceObject.optString("unit");
                                    if (dist <= 0) {
                                        distance.setText("(less than a " + unit + " away)");

                                    } else
                                        distance.setText("(~" + (int) dist + " " + unit + " away)");
                                }

                                /**
                                 * Getting more details on the about me .
                                 */
                                JSONArray field_section = successObject.getJSONArray("field_sections");
                                String moreInfo = "";
                                for(int i = 0 ; i < field_section.length() ; ++i){
                                    JSONObject o1 = field_section.getJSONObject(i);
                                    JSONArray fields = o1.getJSONArray("fields");
                                    for(int j = 0 ; j < fields.length() ; ++j){
                                        JSONObject o2 = fields.getJSONObject(j);
                                        String text = o2.getString("text") + " - ";
                                        //moreInfo += o2.getString("text") + " - ";
                                        JSONArray opt = o2.getJSONArray("options");
                                        boolean flag = false;
                                        for(int k = 0 ; k < opt.length() ; ++k){
                                            JSONObject o3 = opt.getJSONObject(k);
                                            if(o3.getString("is_selected").equals("true")){
                                                moreInfo += text + (o3.getString("text") + System.getProperty("line.separator"));
                                                flag = true;
                                            }
                                        }
                                        if(!flag){
                                            //moreInfo += "Not Available" + System.getProperty("line.separator");
                                        }
                                    }
                                }


                                /**
                                 * Setting up the about me information in the user's profile
                                 */

                                if(successObject.getString("about_me").equals("") || successObject.getString("about_me").equals("null")){
                                    //No information about me is available, so do nothing here.
                                }else{
                                    LinearLayout abt_me_layout = (LinearLayout) findViewById(R.id.about_me_layout);
                                    abt_me_layout.setVisibility(View.VISIBLE);
                                    TextView abt_me_text = (TextView) findViewById(R.id.about_me);
                                    abt_me_text.setText(successObject.getString("about_me") + System.getProperty("line.separator") + System.getProperty("line.separator") + moreInfo);
                                }

                                /**
                                 * Setting up the user's city and country
                                 */
                                if(!userObject.getString("city").equals("")){
                                    place_name.setText(userObject.getString("city"));
                                    if(!userObject.getString("country").equals("")){
                                        place_name.setText(place_name.getText() + ", " + userObject.getString("country"));
                                    }
                                }


                                JSONArray imageArray = successObject.optJSONArray("photos");
                                if (imageArray != null) {
                                    for (int j = 0; j < imageArray.length(); j++) {
                                        JSONObject imageObj = imageArray.getJSONObject(j);
                                        JSONObject photoObj = imageObj.getJSONObject("photo_url");
                                        images.add(photoObj.optString("encounter"));
                                    }
                                }

                                JSONArray interestArray = successObject.optJSONArray("user_interests");
                                if (interestArray != null) {
                                    for (int k = 0; k < interestArray.length(); k++) {
                                        JSONObject interestObj = interestArray.getJSONObject(k);
                                        InterestInfo interestInfo = new InterestInfo();
                                        interestInfo.setInterestId(interestObj.optString("interest_id"));
                                        interestInfo.setInterestName(interestObj.optString("interest_text"));
                                        if (k < 4) {
                                            if (k == 3)
                                                interestInfoArrayList.add(null);
                                            else
                                                interestInfoArrayList.add(interestInfo);
                                        }
                                        fullInterestInfoArrayList.add(interestInfo);
                                    }

                                }

                                JSONArray common_interestArray = successObject.optJSONArray("commom_interests");
                                if (common_interestArray != null) {
                                    for (int k = 0; k < common_interestArray.length(); k++) {
                                        JSONObject interestObj = common_interestArray.getJSONObject(k);
                                       /* InterestInfo interestInfo = new InterestInfo();
                                        interestInfo.setInterestId();
                                        interestInfo.setInterestName(interestObj.optString("interest"));
                                        if (interestInfoArrayList.contains(interestInfo)){
                                            int index = interestInfoArrayList.indexOf(interestInfo);
                                            interestInfoArrayList.get(index).setCommon(true);
                                        }*/
                                        for (int g = 0; g < interestInfoArrayList.size() - 1; g++) {
                                            if (interestObj.optString("interestid").equals(interestInfoArrayList.get(g).getInterestId())) {
                                                interestInfoArrayList.get(g).setCommon(true);
                                            }
                                        }
                                        for (int g = 0; g < fullInterestInfoArrayList.size(); g++) {
                                            if (interestObj.optString("interestid").equals(fullInterestInfoArrayList.get(g).getInterestId())) {
                                                fullInterestInfoArrayList.get(g).setCommon(true);
                                            }
                                        }
                                    }

                                }

                                JSONObject scoreObj = successObject.optJSONObject("user_score");
                                if (scoreObj != null) {
                                    scoreArc.setProgress((int) scoreObj.optDouble("score"));
                                    score_text.setText(String.format("%.1f", scoreObj.optDouble("score")));
                                    String likes = scoreObj.optString("likes");
                                    if (likes.equals("1") || likes.equals("0")) {
                                        if (gender.equalsIgnoreCase("female"))
                                            like_number.setText(Html.fromHtml("<b>" + likes + "</b> person has liked her"));
                                        else
                                            like_number.setText(Html.fromHtml("<b>" + likes + "</b> person has liked him"));
                                    } else {
                                        if (gender.equalsIgnoreCase("female"))
                                            like_number.setText(Html.fromHtml("<b>" + likes + "</b> people have liked her"));
                                        else
                                            like_number.setText(Html.fromHtml("<b>" + likes + "</b> people have liked him"));
                                    }
                                } else {
                                    score_layout.setVisibility(View.GONE);
                                }


                                setUpProfileImages();
                                image_number.setText(String.valueOf(images.size()));
                            }
                            if (interestInfoArrayList.isEmpty()) {
                                interest_layout.setVisibility(View.GONE);
                            } else {
                                interestListAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void setUpProfileImages() {
        // Set product images
        mAdapter = new ProfileImagesViewPagerAdapter(this, images);
        intro_images.setAdapter(mAdapter);
        intro_images.setCurrentItem(0);
        intro_images.setOnPageChangeListener(this);
        setUiPageViewIndicator();
    }

    private void setUiPageViewIndicator() {
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];
        pager_indicator.removeAllViews();

        if (dotsCount == 0)
            return;

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(4, 0, 4, 0);

            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.nonselecteditem_dot));
        }
        dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selecteditem_dot));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                result = data.getBooleanExtra("isUpdated", false);
                if (result) {
                    getProfileInfo();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (result) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("isUpdated", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        } else {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, resultIntent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        Intent resultIntent = new Intent();
        switch (view.getId()) {
            case R.id.dislike_button:
                resultIntent.putExtra("action", "dislike");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                break;
            case R.id.like_button:
                resultIntent.putExtra("action", "like");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                break;

        }
    }

    private void reportDialog() {
        reason_number = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("What's the problem with?")

                .setSingleChoiceItems(R.array.report_reasons, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reason_number = which;
                    }
                })

                .setPositiveButton("Report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        report();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private static class CustomAdapter<T> extends ArrayAdapter<String> {
        public CustomAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText("");
            return view;
        }
    }

}
