package com.provenlogic.mingle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Activities.DisplayActivity;
import com.provenlogic.mingle.Activities.LoginActivity;
import com.provenlogic.mingle.Activities.RegisterActivity;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CallbackManager callbackManager;
    private ImageButton fb_sign_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fabric.with(this, new Crashlytics());
        getSupportActionBar().hide();
        String session_token = (String) SharedPreferencesUtils.getParam(this,SharedPreferencesUtils.SESSION_TOKEN,"");
        if (!session_token.isEmpty()){
            Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        Button reg = (Button) findViewById(R.id.register);
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        Button log = (Button) findViewById(R.id.login);
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        fb_sign_in = (ImageButton) findViewById(R.id.fb_sign_in);
        fb_sign_in.setOnClickListener(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        registerCallBack();
    }

    private void registerCallBack() {
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        String token = loginResult.getAccessToken().getToken();
                        callLoginApi(token);
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    private void callLoginApi(String token) {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Loading...");
        progressDialog.show();
        JsonObject json = new JsonObject();
        json.addProperty("auth_token", token);

        Ion.with(this)
                .load(Endpoints.facebookUrl)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        progressDialog.hide();
                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.optBoolean(Const.Params.STATUS)){
                                JSONObject successObject = jsonObject.getJSONObject("success_data");
                                SharedPreferencesUtils.setParam(MainActivity.this, SharedPreferencesUtils.SESSION_TOKEN, successObject.optString("access_token"));
                                SharedPreferencesUtils.setParam(MainActivity.this, SharedPreferencesUtils.USER_ID, successObject.optString("user_id"));
                                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

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
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fb_sign_in:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_birthday", "user_likes", "user_location", "user_hometown",
                        "user_friends"));
                break;
        }
    }
}
