package com.provenlogic.mingle.Activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Adapters.PurposeListAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1845;

    private CrystalRangeSeekbar age_range;
    private CrystalSeekbar distance_seekbar;
    private TextView age_text, purpose_text, distance_text;
    private LinearLayout purpose_layout, info_layout, account_layout;
    private ImageView purpose_image;
    private CheckBox female_checkbox, male_checkbox;
    private RadioButton currentLocationButton, anotherLocationButton;
    private Location location;
    private String locationName = "";
    private TextView change_button;

    private String unit = "km", id, token;
    private boolean is_updated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        age_text = (TextView) findViewById(R.id.age_text);
        age_range = (CrystalRangeSeekbar) findViewById(R.id.age_rangebar);
        purpose_layout = (LinearLayout) findViewById(R.id.purpose_layout);
        purpose_image = (ImageView) findViewById(R.id.purpose_image);
        purpose_text = (TextView) findViewById(R.id.purpose_text);
        distance_seekbar = (CrystalSeekbar) findViewById(R.id.distance_seekbar);
        distance_text = (TextView) findViewById(R.id.distance_text);
        female_checkbox = (CheckBox) findViewById(R.id.female_checkbox);
        male_checkbox = (CheckBox) findViewById(R.id.male_checkbox);
        currentLocationButton = (RadioButton) findViewById(R.id.current_location_radio);
        anotherLocationButton = (RadioButton) findViewById(R.id.another_location_radio);
        change_button = (TextView) findViewById(R.id.change_button);
        account_layout = (LinearLayout) findViewById(R.id.account_layout);
        account_layout.setOnClickListener(this);
        info_layout = (LinearLayout) findViewById(R.id.info_layout);
        info_layout.setOnClickListener(this);

        currentLocationButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {

                    anotherLocationButton.setChecked(false);
                }
            }
        });

        anotherLocationButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!locationName.isEmpty()) {

                        currentLocationButton.setChecked(false);
                    } else
                        showPlacesDialog();
                    currentLocationButton.setChecked(false);
                }
            }
        });


        purpose_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPurposeDialog();
            }
        });
        age_range.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                age_text.setText("Show me people aged " + minValue + " to " + maxValue);
            }
        });
        distance_seekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                distance_text.setText("Show me people within " + value + " " + unit);
            }
        });
        change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPlacesDialog();
            }
        });
        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        getFilter();
    }

    private void showPlacesDialog() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private void showPurposeDialog() {
        final int[] purposeImages = {R.drawable.account_multiple, R.drawable.wechat, R.drawable.heart};
        final String[] purposeNameList = {"Make new friends", "Chat", "Date"};

        LayoutInflater inflater = getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.purpose_dialog_layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        builder.setTitle("I'm here to");
        final AlertDialog alertDialog = builder.create();
        ListView purpose_list = (ListView) dialoglayout.findViewById(R.id.purpose_list);
        purpose_list.setAdapter(new PurposeListAdapter(this, purposeNameList, purposeImages));

        purpose_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                purpose_image.setImageResource(purposeImages[i]);
                purpose_text.setText(purposeNameList[i]);
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_apply) {
            updateFilter();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFilter() {
        JsonObject json = new JsonObject();
        json.addProperty("user_id", id);
        json.addProperty("access_token", token);

        Ion.with(this)
                .load(Endpoints.getFilterUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, FilterActivity.this)){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.STATUS)) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                //  Toast.makeText(FilterActivity.this,obj.optString("success_text"),Toast.LENGTH_SHORT).show();
                                JSONObject ageObj = successObj.optJSONObject("perfered_ages");
                                if (ageObj != null) {
                                    age_range.setMaxStartValue(ageObj.optInt("max")).apply();
                                    age_range.setMinStartValue(ageObj.optInt("min")).apply();
                                }
                                JSONArray genderArray = successObj.optJSONArray("prefered_genders");
                                if (genderArray != null) {
                                    for (int i = 0; i < genderArray.length(); i++) {
                                        if (genderArray.getString(i).equals("male")) {
                                            male_checkbox.setChecked(true);
                                        }
                                        if (genderArray.getString(i).equals("female")) {
                                            female_checkbox.setChecked(true);
                                        }
                                    }
                                }

                                JSONObject distanceObj = successObj.optJSONObject("perfered_distance");
                                if (distanceObj != null) {
                                    distance_seekbar.setMinStartValue(distanceObj.optInt("value")).apply();
                                    unit = distanceObj.optString("unit");
                                    distance_text.setText("Show me people within " + distance_seekbar.getSelectedMinValue() + " " + unit);
                                }
                                JSONObject locationObject = successObj.optJSONObject("locations");
                                if (locationObject != null) {
                                    JSONObject nearbyObj = locationObject.optJSONObject("people_nearby");
                                    if (nearbyObj != null) {
                                        location = new Location("");
                                        location.setLatitude(nearbyObj.optDouble("latitude"));
                                        location.setLongitude(nearbyObj.optDouble("longitude"));
                                        locationName = nearbyObj.optString("location_name");
                                        if (locationName == null || locationName.equals("null"))
                                            locationName = "";
                                    }
                                }
                                updateLocationUi();

                                if (ApplicationSingleTon.locationPreference == 0)
                                    currentLocationButton.setChecked(true);
                                else anotherLocationButton.setChecked(true);
                            } else {
                                JSONObject obj = jsonObject.getJSONObject("error_data");
                                Toast.makeText(FilterActivity.this, obj.optString("error_text"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void updateLocationUi() {
        if (!locationName.isEmpty() && !locationName.equals("null")) {
            anotherLocationButton.setText(locationName);
            change_button.setVisibility(View.VISIBLE);
        } else change_button.setVisibility(View.GONE);
    }

    private void updateFilter() {
        JsonObject json = new JsonObject();
        json.addProperty("user_id", id);
        json.addProperty("access_token", token);
        String genders = "";
        if (male_checkbox.isChecked())
            genders = "male";
        if (female_checkbox.isChecked())
            genders = "female";
        if (male_checkbox.isChecked() && female_checkbox.isChecked())
            genders = "male,female";

        json.addProperty("prefered_genders", genders);
        String age = age_range.getSelectedMinValue() + "-" + age_range.getSelectedMaxValue();
        json.addProperty("prefered_ages", age);
        json.addProperty("prefered_distance", distance_seekbar.getSelectedMinValue());

        if (location != null) {
            json.addProperty("latitude", location.getLatitude());
            json.addProperty("longitude", location.getLongitude());
            json.addProperty("location_name", locationName);
        }


        Ion.with(this)
                .load(Endpoints.saveFilterUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, FilterActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject obj = jsonObject.getJSONObject("success_data");
                                is_updated = true;
                                if (currentLocationButton.isChecked())
                                    ApplicationSingleTon.locationPreference = 0;
                                else ApplicationSingleTon.locationPreference = 1;

                                Toast.makeText(FilterActivity.this, obj.optString("success_text"), Toast.LENGTH_SHORT).show();
                            } else {
                                JSONObject obj = jsonObject.getJSONObject("error_data");
                                Toast.makeText(FilterActivity.this, obj.optString("error_text"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place searchPlace = PlaceAutocomplete.getPlace(this, data);
                locationName = searchPlace.getName().toString();
                if(location == null){
                    location = new Location("");
                }
                location.setLatitude(searchPlace.getLatLng().latitude);
                location.setLongitude(searchPlace.getLatLng().longitude);
                anotherLocationButton.setChecked(true);
                updateLocationUi();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onBackPressed() {


        Intent intent = new Intent();
        if (is_updated)
            intent.putExtra("is_updated", true);
        else intent.putExtra("is_updated", false);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.info_layout:
                Intent intent;
                intent = new Intent(this, BasicInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.account_layout:
                Intent intent1;
                intent1 = new Intent(this, AccountActivity.class);
                startActivity(intent1);
                break;
        }
    }
}
