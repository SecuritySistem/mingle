package com.provenlogic.mingle.Activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public class BasicInfoActivity extends AppCompatActivity implements View.OnClickListener {

    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
    DateTimeFormatter print_dtf = DateTimeFormat.forPattern("dd-MM-yyyy");
    private RadioButton female_checkbox, male_checkbox;
    private TextView name, birthday;
    private DateTime dob;
    private String id, token;
    private LinearLayout dob_layout, name_layout;
    private boolean isChanged = false, isFirstTimeChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        female_checkbox = (RadioButton) findViewById(R.id.female_checkbox);
        male_checkbox = (RadioButton) findViewById(R.id.male_checkbox);
        birthday = (TextView) findViewById(R.id.birthday);
        name = (TextView) findViewById(R.id.name);
        dob_layout = (LinearLayout) findViewById(R.id.dob_layout);
        name_layout = (LinearLayout) findViewById(R.id.name_layout);
        name_layout.setOnClickListener(this);
        dob_layout.setOnClickListener(this);

        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        getProfile();
        female_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (isFirstTimeChanged)
                        isChanged = true;
                }
                isFirstTimeChanged = true;
            }
        });
        male_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (isFirstTimeChanged)
                        isChanged = true;
                }
                isFirstTimeChanged = true;
            }
        });

    }

    private void getProfile() {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Loading..");
        progressDialog.show();
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
                        progressDialog.hide();
                        if (result == null)
                            return;

                        JSONObject jsonObject = null;
                        try {

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, BasicInfoActivity.this)){
                                return;
                            }

                            jsonObject = new JSONObject(result.toString());
                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                JSONObject userObj = successObj.getJSONObject("user");
                                name.setText(userObj.optString("name"));
                                birthday.setText(userObj.optString("dob"));
                                if (!userObj.optString("dob").isEmpty())
                                    dob = dtf.parseDateTime(userObj.optString("dob"));
                                if (userObj.optString("gender_text").equalsIgnoreCase("male")) {
                                    male_checkbox.setChecked(true);
                                } else female_checkbox.setChecked(true);
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.name_layout:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
                dialogBuilder.setView(dialogView);
                final EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
                editText.setText(name.getText().toString());
                dialogBuilder.setTitle("Name");
                dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isChanged = true;
                        name.setText(editText.getText().toString());
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });


                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

                break;
            case R.id.dob_layout:
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        dob = new DateTime().withDate(i, i1 + 1, i2);
                        birthday.setText(dtf.print(dob));
                        isChanged = true;
                    }
                }, dob.getYear(), dob.getMonthOfYear() - 1, dob.getDayOfMonth());
                datePickerDialog.show();

                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (isChanged)
            updateBasicInfo();
        else super.onBackPressed();
    }

    private void updateBasicInfo() {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("name", name.getText().toString());
        json.addProperty("dob", print_dtf.print(dob));
        if (male_checkbox.isChecked())
            json.addProperty("gender", "male");
        if (female_checkbox.isChecked())
            json.addProperty("gender", "female");

        Ion.with(this)
                .load(Endpoints.updateBasicInfoUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, BasicInfoActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                Toast.makeText(BasicInfoActivity.this, successObj.optString("success_text"), Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                JSONObject successObj = jsonObject.getJSONObject("error_data");
                                Toast.makeText(BasicInfoActivity.this, successObj.optString("error_text"), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }
}
