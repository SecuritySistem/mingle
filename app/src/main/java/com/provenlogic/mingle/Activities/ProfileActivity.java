package com.provenlogic.mingle.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.provenlogic.mingle.Adapters.InterestListAdapter;
import com.provenlogic.mingle.Adapters.MyImageListAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.InterestInfo;
import com.provenlogic.mingle.Models.PackageDetail;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements MyImageListAdapter.CallbackInterface, View.OnClickListener {

    private int REFILL_CREDIT_REQUEST = 5;
    private int CREDIT_UPDATE_REQUEST = 3;
    private static final int PICK_IMAGE = 1243;
    private static final int REQUEST_CODE_PAYPAL = 1;
    private static final int REQUEST_CODE_PAYPAL_1 = 2;
    public static PayPalConfiguration config = new PayPalConfiguration()
            .environment(Const.CONFIG_ENVIRONMENT)
            .clientId(Const.CONFIG_CLIENT_ID);
    int arrayCount = 0;
    private String id, token;
    private LinearLayout interest_layout, score_layout, verify_layout, super_layout, popularity_layout, credits_layout;
    private RecyclerView my_image_list;
    private MyImageListAdapter myImageListAdapter;
    private ArrayList<String> myImages;
    private ImageView popularity_level;
    private TextView active_status, popularity_status, credits, name, age;
    private RecyclerView interest_list;
    private ArrayList<InterestInfo> interestInfoArrayList, fullInterestInfoArrayList;
    private InterestListAdapter interestListAdapter;
    private SeekArc scoreArc;
    private TextView score_text, like_number, verified_text, place_name;
    private ImageView verified_image;
    private ArrayList<String> packageList;
    private ArrayList<PackageDetail> packageDetailList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myImages = new ArrayList<>();
        interestInfoArrayList = new ArrayList<>();
        fullInterestInfoArrayList = new ArrayList<>();
        my_image_list = (RecyclerView) findViewById(R.id.my_image_list);
        popularity_level = (ImageView) findViewById(R.id.popularity_level);
        credits = (TextView) findViewById(R.id.credits);
        active_status = (TextView) findViewById(R.id.active_status);
        popularity_status = (TextView) findViewById(R.id.popularity_status);
        name = (TextView) findViewById(R.id.name);
        age = (TextView) findViewById(R.id.age);
        place_name = (TextView) findViewById(R.id.place_name);
        credits_layout = (LinearLayout) findViewById(R.id.credits_layout);
        popularity_layout = (LinearLayout) findViewById(R.id.popularity_layout);
        super_layout = (LinearLayout) findViewById(R.id.super_layout);
        credits_layout.setOnClickListener(this);
        super_layout.setOnClickListener(this);

        interest_list = (RecyclerView) findViewById(R.id.interest_list);
        score_text = (TextView) findViewById(R.id.score_text);
        like_number = (TextView) findViewById(R.id.like_number);

        interest_layout = (LinearLayout) findViewById(R.id.interest_layout);
        score_layout = (LinearLayout) findViewById(R.id.score_layout);
        verify_layout = (LinearLayout) findViewById(R.id.verify_layout);
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

        id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");
        setUpInterestRecyclerView();
        setUpImageRecyclerView();
        getMyProfile();
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
    }

    private void setUpInterestRecyclerView() {
        interestListAdapter = new InterestListAdapter(interestInfoArrayList, fullInterestInfoArrayList, this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        interest_list.setLayoutManager(mLayoutManager);
        interest_list.setItemAnimator(new DefaultItemAnimator());
        interest_list.setAdapter(interestListAdapter);
    }

    private void payByPayPal(String amount) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Total bill",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL);
    }

    private void payByPayPalForSuper(String amount) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Total bill",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL_1);
    }

    /**
     * Loads the my profile
     */
    private void getMyProfile() {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Loading..");
        progressDialog.setCancelable(false);
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
                        try {

                            Log.d("PROFILE", result.toString());

                            JSONObject jsonObject = new JSONObject(result.toString());
                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ProfileActivity.this)){
                                return;
                            }
                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                JSONObject userObj = successObj.getJSONObject("user");
                                credits.setText(userObj.optString("credits", "0"));
                                if (userObj.optString("superpower_activated").equals("true")) {
                                    active_status.setText("Active");
                                    active_status.setTextColor(getResources().getColor(R.color.white));
                                } else {
                                    active_status.setText("Inactive");
                                    active_status.setTextColor(getResources().getColor(R.color.colorAccent));
                                }
                                name.setText(userObj.optString("name"));
                                if (!userObj.optString("age").equals("")) {
                                    age.setText(", " + userObj.optString("age"));
                                }
                                if (userObj.optString("verified").equalsIgnoreCase("verified")) {
                                    boolean isAny = false;
                                    if (userObj.optString("register_from").equalsIgnoreCase("facebook")) {
                                        verified_text.setText("Facebook");
                                        verified_image.setImageResource(R.drawable.facebook);
                                        isAny = true;
                                    }
                                    if (userObj.optString("register_from").equalsIgnoreCase("google")) {
                                        verified_text.setText("Google");
                                        verified_image.setImageResource(R.drawable.google_plus);
                                        isAny = true;
                                    }
                                    if (!isAny)
                                        verify_layout.setVisibility(View.GONE);
                                } else {
                                    verify_layout.setVisibility(View.GONE);
                                }

                                /**
                                 * Getting more details on the about me .
                                 */
                                JSONArray field_section = successObj.getJSONArray("field_sections");
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
                                           // moreInfo += "Not Available" + System.getProperty("line.separator");
                                        }
                                    }
                                }

                                /**
                                 * Setting up the about me information in the user's profile
                                 */
                                TextView abt_me_text = (TextView) findViewById(R.id.about_me);
                                if(successObj.getString("about_me").equals("") || successObj.getString("about_me").equals("null")){
                                    abt_me_text.setText("Not set yet, click to update your status.");
                                }else{

                                    abt_me_text.setText(successObj.getString("about_me") + System.getProperty("line.separator") + System.getProperty("line.separator") + moreInfo);
                                }

                                /**
                                 * Setting up the click listener so that user can update his about me info.
                                 */
                                abt_me_text.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        HelperFunctions.UpdateAboutMeStatus(ProfileActivity.this, id, token);
                                    }
                                });

                                place_name.setText(userObj.optString("city") + ", " + userObj.optString("country"));

                                JSONArray interestArray = successObj.optJSONArray("user_interests");
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

                                JSONObject profileObj = userObj.getJSONObject("profile_pic_url");

                                JSONObject popularityObj = successObj.optJSONObject("user_popularity");
                                if (popularityObj != null) {
                                    switch (popularityObj.optString("popularity_type")) {
                                        case "very_very_low":
                                            popularity_level.setImageResource(R.drawable.battery_level_vvl);
                                            popularity_status.setText("Very very low");
                                            break;
                                        case "very_low":
                                            popularity_level.setImageResource(R.drawable.battery_level_vl);
                                            popularity_status.setText("Very low");
                                            break;
                                        case "low":
                                            popularity_level.setImageResource(R.drawable.battery_level_l);
                                            popularity_status.setText("Low");
                                            break;
                                        case "medium":
                                            popularity_level.setImageResource(R.drawable.battery_level_mid);
                                            popularity_status.setText("Medium");
                                            popularity_status.setTextColor(getResources().getColor(R.color.white));
                                            break;
                                        case "high":
                                            popularity_level.setImageResource(R.drawable.battery_level_high);
                                            popularity_status.setText("High");
                                            popularity_status.setTextColor(getResources().getColor(R.color.white));
                                            break;
                                    }
                                }

                                JSONObject scoreObj = successObj.optJSONObject("user_score");
                                if (scoreObj != null) {
                                    scoreArc.setProgress((int) scoreObj.optDouble("score"));
                                    score_text.setText(String.format("%.1f", scoreObj.optDouble("score")));
                                    String likes = scoreObj.optString("likes", "0");
                                    if (likes.equals("0")) {

                                        like_number.setText("No one has liked you yet");
                                    } else {

                                        like_number.setText(Html.fromHtml("<b>" + likes + "</b> people has liked you"));
                                    }
                                } else {
                                    score_layout.setVisibility(View.GONE);
                                }

                                JSONArray photoArray = successObj.getJSONArray("photos");
                                for (int j = 0; j < photoArray.length(); j++) {
                                    JSONObject photoObj = photoArray.getJSONObject(j);
                                    JSONObject photoUrlObj = photoObj.getJSONObject("photo_url");
                                    myImages.add(photoUrlObj.optString("encounter"));
                                }

                            }
                            myImages.add(0, null);
                            myImageListAdapter.notifyDataSetChanged();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    private void setUpImageRecyclerView() {
        myImageListAdapter = new MyImageListAdapter(myImages, this);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        my_image_list.setLayoutManager(mLayoutManager);
        my_image_list.setItemAnimator(new DefaultItemAnimator());
        my_image_list.setAdapter(myImageListAdapter);
    }

    @Override
    public void onHandleSelection(int position, String text) {
        if (position == 0) {
            getImageFromGallery();
        }
    }

    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    File imageFile = persistImage(bitmap, String.valueOf(System.currentTimeMillis()));
                    sendToServer(imageFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
        if (requestCode == REQUEST_CODE_PAYPAL) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i("paymentExample", confirm.toJSONObject().toString(4));

                        // TODO: send 'confirm' to your server for verification.
                        // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                        // for more details.
                        String paymentId = confirm.getProofOfPayment().getPaymentId();
                        addMoney(paymentId, "paypal", confirm.getPayment().getAmountAsLocalizedString());

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }

        if (requestCode == REQUEST_CODE_PAYPAL_1) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i("paymentExample", confirm.toJSONObject().toString(4));

                        // TODO: send 'confirm' to your server for verification.
                        // see https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                        // for more details.
                        String paymentId = confirm.getProofOfPayment().getPaymentId();
                        addSuperPower(paymentId, "paypal", confirm.getPayment().getAmountAsLocalizedString());

                    } catch (JSONException e) {
                        Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paymentExample", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }

        /**
         * Request for credit update from the popularity activity or refill credit activity.
         */
        if(requestCode == CREDIT_UPDATE_REQUEST || requestCode == REFILL_CREDIT_REQUEST){
            if(resultCode == RESULT_OK){
                credits.setText("" + ApplicationSingleTon.Credits);
            }
        }
    }

    private void addMoney(String paymentId, String paypal, String amount) {
        final ProgressDialog progress = new ProgressDialog(ProfileActivity.this);
        progress.setTitle("Adding Money");
        progress.setMessage("Please wait while we are adding money");
        progress.show();

        final JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("transaction_id", paymentId);
        json.addProperty("package_id", packageDetailList.get(arrayCount).getId());
        json.addProperty("amount", packageDetailList.get(arrayCount).getAmount());

        Ion.with(this)
                .load(Endpoints.buyCredits)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error

                        progress.dismiss();
                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ProfileActivity.this)){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                String credit = jsonObject.optString("user_credit_balance");
                                credits.setText(credit);
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void addSuperPower(String paymentId, String paypal, String amount) {
        final JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("transaction_id", paymentId);
        json.addProperty("package_id", packageDetailList.get(arrayCount).getId());
        json.addProperty("amount", packageDetailList.get(arrayCount).getAmount());

        Ion.with(this)
                .load(Endpoints.buySuperPowerUrl)
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ProfileActivity.this)){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                active_status.setText("Active");
                                active_status.setTextColor(getResources().getColor(R.color.white));
                                ApplicationSingleTon.isEncounterAvailable = true;
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void sendToServer(File imageFile) {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Uploading..");
        progressDialog.show();
        List<Part> files = new ArrayList();
        files.add(new FilePart("photos[]", imageFile));
        Ion.with(this)
                .load(Endpoints.uploadOtherPhotosUrl)
                .setMultipartParameter(Const.Params.ID, id)
                .setMultipartParameter(Const.Params.TOKEN, token)
                .addMultipartParts(files)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        progressDialog.dismiss();
                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, ProfileActivity.this)){
                                return;
                            }

                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                JSONObject successObj = jsonObject.getJSONObject("success_data");
                                JSONArray photoArray = successObj.optJSONArray("photo_urls");
                                for (int i = 0; i < photoArray.length(); i++) {
                                    JSONObject photoObj = photoArray.getJSONObject(i);
                                    myImages.add(photoObj.optString("encounter"));
                                }
                                myImageListAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    private File persistImage(Bitmap bitmap, String name) {
        File filesDir = getApplicationContext().getFilesDir();
        File imageFile = new File(filesDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        return imageFile;
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
            case R.id.credits_layout:
               //getCreditPackages();
                startActivityForResult(new Intent(ProfileActivity.this, RefillCreditsActivity.class), REFILL_CREDIT_REQUEST);
                break;
            case R.id.super_layout:
                //getSuperPowerPackages();
                startActivity(new Intent(ProfileActivity.this, SuperPowerActivity.class));
                break;
            case R.id.popularity_layout:
                //start popularity activity to provide options to increase the popularity.
                startActivityForResult(new Intent(ProfileActivity.this, PopularityActivity.class), CREDIT_UPDATE_REQUEST);
                break;
        }
    }

    private void getCreditPackages() {

        final ProgressDialog progress = new ProgressDialog(ProfileActivity.this);
        progress.setTitle("Getting Credit Packages");
        progress.setMessage("Wait while getting credit packages from server");
        progress.show();

        JsonObject json = new JsonObject();
        json.addProperty("type", "credit");

        Ion.with(this)
                .load(Endpoints.getCreditPackages)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error

                        progress.dismiss();

                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                JSONArray packageArray = jsonObject.getJSONArray("packages");
                                packageList = new ArrayList<>();
                                packageDetailList = new ArrayList<>();
                                for (int i = 0; i < packageArray.length(); i++) {
                                    JSONObject packObj = packageArray.getJSONObject(i);
                                    PackageDetail packageDetail = new PackageDetail();
                                    packageDetail.setId(packObj.optString("package_id"));
                                    packageDetail.setAmount(packObj.optString("amount"));
                                    packageDetail.setCredits(packObj.optString("credits"));
                                    packageDetail.setCurrency(packObj.optString("currency"));
                                    packageDetailList.add(packageDetail);
                                    packageList.add(packageDetail.getCredits() + " credits - " + packageDetail.getCurrency() + " " + packageDetail.getAmount());
                                }
                                createDialogSingleChoice(packageList, packageDetailList);
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void getSuperPowerPackages() {
        final ProgressDialog progress = new ProgressDialog(ProfileActivity.this);
        progress.setTitle("Wait while loading");
        progress.setMessage("We are getting list of packages for you.");
        progress.show();

        JsonObject json = new JsonObject();
        json.addProperty("type", "superpower");

        Ion.with(this)
                .load(Endpoints.getCreditPackages)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error

                        progress.dismiss();

                        if (result == null)
                            return;
                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                JSONArray packageArray = jsonObject.getJSONArray("packages");
                                packageList = new ArrayList<>();
                                packageDetailList = new ArrayList<>();
                                for (int i = 0; i < packageArray.length(); i++) {
                                    JSONObject packObj = packageArray.getJSONObject(i);
                                    PackageDetail packageDetail = new PackageDetail();
                                    packageDetail.setId(packObj.optString("package_id"));
                                    packageDetail.setAmount(packObj.optString("amount"));
                                    packageDetail.setPackname_name(packObj.optString("package_name"));
                                    packageDetail.setCurrency(packObj.optString("currency"));
                                    packageDetailList.add(packageDetail);
                                    packageList.add(packageDetail.getPackname_name() + "  - " + packageDetail.getCurrency() + " " + packageDetail.getAmount());
                                }
                                createDialogSuperSingleChoice(packageList, packageDetailList);
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void createDialogSingleChoice(ArrayList<String> packageList, final ArrayList<PackageDetail> packageDetailList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select a Package")

                .setSingleChoiceItems(packageList.toArray(new String[packageList.size()]), 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        arrayCount = which;
                    }
                })

                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        payByPayPal(packageDetailList.get(arrayCount).getAmount());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.show();
    }

    private void createDialogSuperSingleChoice(ArrayList<String> packageList, final ArrayList<PackageDetail> packageDetailList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select a Package")

                .setSingleChoiceItems(packageList.toArray(new String[packageList.size()]), 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        arrayCount = which;
                    }
                })

                // Set the action buttons
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        payByPayPalForSuper(packageDetailList.get(arrayCount).getAmount());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.show();
    }

    /**
     * This function is to update the about me status of the current logged in user.
     * @param newUpdate is the new status.
     */
    public void UpdateAboutMeStatus(String newUpdate){
        TextView abt_me_text = (TextView) findViewById(R.id.about_me);
        abt_me_text.setText(newUpdate);
    }
}
