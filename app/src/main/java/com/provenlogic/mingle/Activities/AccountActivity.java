package com.provenlogic.mingle.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.MainActivity;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {

    private TextView delete_account;
    private EditText email;
    private Button sign_out;

    private String id, token;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        delete_account = (TextView) findViewById(R.id.delete_account);
        email = (EditText) findViewById(R.id.email);
        sign_out = (Button) findViewById(R.id.sign_out);

        delete_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSurityDialog();
            }
        });
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        getProfile();
    }

    private void signOut() {
        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.USER_ID, "");
        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.IS_PRO_USER, "");
        SharedPreferencesUtils.setParam(this, SharedPreferencesUtils.PROPERTY_GCM_REG_ID, "");
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void delete_account(String password) {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Deleting account..");
        progressDialog.show();
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("password", password);

        Ion.with(this)
                .load(Endpoints.deleteAccount)
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
                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                SharedPreferencesUtils.setParam(AccountActivity.this, SharedPreferencesUtils.SESSION_TOKEN, "");
                                SharedPreferencesUtils.setParam(AccountActivity.this, SharedPreferencesUtils.USER_ID, "");
                                SharedPreferencesUtils.setParam(AccountActivity.this, SharedPreferencesUtils.IS_PRO_USER, "");
                                SharedPreferencesUtils.setParam(AccountActivity.this, SharedPreferencesUtils.PROPERTY_GCM_REG_ID, "");
                                LoginManager.getInstance().logOut();
                                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                JSONObject errorData = jsonObject.getJSONObject("error_data");
                                Toast.makeText(AccountActivity.this, errorData.optString("error_text"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void showSurityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("We are sorry to see you go. Are you sure you want to delete your account?");
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showPasswordDialog();
            }
        });
        builder.show();
    }

    private void showPasswordDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_label_editor, null);
        dialogBuilder.setView(dialogView);
        final EditText editText = (EditText) dialogView.findViewById(R.id.label_field);
        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        dialogBuilder.setTitle("Enter you password to continue");
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                delete_account(editText.getText().toString());
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
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
                        progressDialog.dismiss();
                        if (result == null)
                            return;

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, AccountActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                JSONObject userObj = successObj.getJSONObject("user");
                                email.setText(userObj.optString("username"));
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
}
