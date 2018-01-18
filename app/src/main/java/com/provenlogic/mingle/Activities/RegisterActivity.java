package com.provenlogic.mingle.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Models.GenderOptions;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Anurag on 4/11/2017.
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1845;
    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy");
    private RadioGroup gender_group;
    private EditText name_et, email_et, city_et, confirm_password_et, password_et, country_et;
    private ArrayList<GenderOptions> genderOptionsArrayList;
    private int choosenGenderCount = -1;
    private LinearLayout his_birthday_layout, her_birthday_layout;
    private TextView her_birthday, his_birthday;
    private LocalDate dob, couple_dob;
    private Button register;
    private String city, country;
    private Place searchPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register");
        genderOptionsArrayList = new ArrayList<>();
        gender_group = (RadioGroup) findViewById(R.id.gender_group);
        name_et = (EditText) findViewById(R.id.name_et);
        email_et = (EditText) findViewById(R.id.email_et);
        city_et = (EditText) findViewById(R.id.city_et);
        city_et.setInputType(InputType.TYPE_CLASS_TEXT);
        country_et = (EditText) findViewById(R.id.country_et);
        his_birthday_layout = (LinearLayout) findViewById(R.id.his_birthday_layout);
        his_birthday_layout.setOnClickListener(this);
        her_birthday = (TextView) findViewById(R.id.her_birthday);
        his_birthday = (TextView) findViewById(R.id.his_birthday);
        register = (Button) findViewById(R.id.register);
        password_et = (EditText) findViewById(R.id.password_et);
        confirm_password_et = (EditText) findViewById(R.id.confirm_password_et);
        register.setOnClickListener(this);

        her_birthday_layout = (LinearLayout) findViewById(R.id.her_birthday_layout);
        her_birthday_layout.setOnClickListener(this);
        city_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showPlacesDialog();
            }
        });
        getCustomFields();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    private void getCustomFields() {
        final JsonObject json = new JsonObject();
        json.addProperty("foo", "bar");

        Ion.with(this)
                .load(Endpoints.getCustomFields)
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
                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                JSONObject genderObj = successObj.getJSONObject("gender");
                                JSONArray optionsArray = genderObj.getJSONArray("options");
                                for (int i = 0; i < optionsArray.length(); i++) {
                                    JSONObject optionObj = optionsArray.getJSONObject(i);
                                    GenderOptions genderOptions = new GenderOptions();
                                    genderOptions.setId(optionObj.optString("id"));
                                    genderOptions.setCode(optionObj.optString("code"));
                                    genderOptions.setText(optionObj.optString("text"));
                                    if(optionObj.optString("text").equals("Male") || optionObj.optString("text").equals("Female")){
                                        genderOptionsArrayList.add(genderOptions);
                                    }
                                }
                                setUpRadioButtons();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                searchPlace = PlaceAutocomplete.getPlace(this, data);
                List<Address> addresses = null;
                try {
                    Geocoder mGeocoder = new Geocoder(this, Locale.getDefault());
                    addresses = mGeocoder.getFromLocation(searchPlace.getLatLng().latitude, searchPlace.getLatLng().longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        city_et.setText(addresses.get(0).getLocality());
                        city = addresses.get(0).getLocality();
                        country = addresses.get(0).getCountryName();
                       /* country.setText(addresses.get(0).getCountryName());
                        state.setText(addresses.get(0).getAdminArea());
                        pincode.setText(addresses.get(0).getPostalCode());*/
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void setUpRadioButtons() {
        gender_group.removeAllViews();
        gender_group.setOnCheckedChangeListener(null);
        for (int i = 0; i < genderOptionsArrayList.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(genderOptionsArrayList.get(i).getText());
            radioButton.setId(i);
            gender_group.addView(radioButton);
        }
        gender_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    choosenGenderCount = checkedId;
                    switch (genderOptionsArrayList.get(choosenGenderCount).getCode()) {
                        case "couple":
                        case "custom_couple":
                            her_birthday_layout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            her_birthday_layout.setVisibility(View.GONE);

                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.his_birthday_layout:
                openDefaultDatePicker();
                break;
            case R.id.her_birthday_layout:
                openCoupleDatePicker();
                break;
            case R.id.register:
                if (checkForEmpty())
                    register();
                break;
        }
    }

    private boolean checkForEmpty() {
        if (name_et.getText().toString().isEmpty()) {
            name_et.setError("Enter a name");
            return false;
        }
        if (email_et.getText().toString().isEmpty()) {
            email_et.setError("Enter a email address");
            return false;
        } else {
            if (!isValidEmail(email_et.getText().toString())) {
                email_et.setError("Enter a valid email address");
                return false;
            }
        }
        if (city_et.getText().toString().isEmpty()) {
            city_et.setError("Enter a city");
            return false;
        }
        if (choosenGenderCount == -1) {
            Toast.makeText(this, "Choose a gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (dob == null) {
            Toast.makeText(this, "Choose a date of birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password_et.getText().toString().isEmpty() || password_et.getText().toString().length() < 8) {
            password_et.setError("Enter a password");
            return false;
        }
        if (confirm_password_et.getText().toString().isEmpty() || !confirm_password_et.getText().toString().equals(password_et.getText().toString())) {
            confirm_password_et.setError("confirm your password");
            return false;
        }

        return true;

    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void register() {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObject json = new JsonObject();
        json.addProperty("name", name_et.getText().toString());
        json.addProperty("username", email_et.getText().toString());
        json.addProperty("city", city_et.getText().toString());
        json.addProperty("country", country_et.getText().toString());
        json.addProperty("gender", genderOptionsArrayList.get(choosenGenderCount).getCode());
        json.addProperty("lat", 10.0f);
        json.addProperty("lng", 10.0f);
        json.addProperty("password", password_et.getText().toString());
        json.addProperty("password_confirmation", confirm_password_et.getText().toString());
        json.addProperty("dob", fmt.print(dob));
        if (couple_dob != null)
            json.addProperty("couple_dob", fmt.print(dob));

        Ion.with(this)
                .load(Endpoints.registerUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        progressDialog.dismiss();
                        if (result == null)
                            return;
                        Log.d("DATA", result.toString());
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.getString("status").equals("success")) {
                                JSONObject successObject = jsonObject.getJSONObject("success_data");
                                StringBuilder success_msg = new StringBuilder("");
                                success_msg.append("Successfully Registered.");

                                SharedPreferencesUtils.setParam(RegisterActivity.this, SharedPreferencesUtils.SESSION_TOKEN, successObject.optString("access_token"));
                                SharedPreferencesUtils.setParam(RegisterActivity.this, SharedPreferencesUtils.USER_ID, successObject.optString("user_id"));
                                if (successObject.optBoolean("email_verify_required")) {
                                    success_msg.append("Please verify your email address.");
                                }
                                Toast.makeText(RegisterActivity.this, success_msg.toString(), Toast.LENGTH_LONG).show();
                                //onBackPressed();
                                finish();
                                startActivity(new Intent(RegisterActivity.this, SetProfilePictureActivity.class));
                            } else {
                                JSONArray errorArray = jsonObject.getJSONArray("error_data");
                                StringBuilder error_msg = new StringBuilder("");
                                for (int j = 0; j < errorArray.length(); j++) {
                                    error_msg.append(errorArray.get(j));
                                }
                                Toast.makeText(RegisterActivity.this, error_msg.toString(), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            Log.d("REGISTER", e1.toString());
                        }

                    }
                });
    }

    private void openDefaultDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 18;
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        dob = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                        his_birthday.setText("" + dayOfMonth + "-" + monthOfYear + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        DateTime max_date = new DateTime(mYear, mMonth + 1, mDay, 0, 0);
        datePickerDialog.getDatePicker().setMaxDate(max_date.getMillis());

        datePickerDialog.show();
    }

    private void openCoupleDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 18;
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        couple_dob = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                        her_birthday.setText("" + dayOfMonth + "-" + monthOfYear + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        DateTime max_date = new DateTime(mYear, mMonth + 1, mDay, 0, 0);
        datePickerDialog.getDatePicker().setMaxDate(max_date.getMillis());

        datePickerDialog.show();
    }

}