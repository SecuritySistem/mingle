package com.provenlogic.mingle.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.provenlogic.mingle.Adapters.HighlightAdapter;
import com.provenlogic.mingle.Adapters.PaymentAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.PaymentResolver;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by Anurag on 4/7/2017.
 */

public class RefillCreditsActivity extends AppCompatActivity {

    public static PayPalConfiguration config = new PayPalConfiguration().environment(Const.CONFIG_ENVIRONMENT).clientId(Const.CONFIG_CLIENT_ID);
    private int REQUEST_CODE_PAYPAL = 101;

    private int STRIPE_PAYMENT_REQUEST = 102;
    private boolean paymentSuccessfull;
    private ViewPager dotPager;
    private TabLayout dots;

    private ViewPager paymentPager;
    private PagerSlidingTabStrip strip;

    private LinearLayout continueBtn;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Resetting the payment mode for this fresh payment session.
        PaymentResolver.setPaymentMode(PaymentResolver.PAYMENT_MODE.NONE);
        paymentSuccessfull = false;

        setContentView(R.layout.activity_refill_credits);
        getSupportActionBar().setTitle("Refill Credits");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dotPager = (ViewPager) findViewById(R.id.dot_pager);
        dots = (TabLayout) findViewById(R.id.tabDots);
        dots.setupWithViewPager(dotPager, true);
        dotPager.setAdapter(new HighlightAdapter(getSupportFragmentManager()));

        paymentPager = (ViewPager) findViewById(R.id.payment_pager);
        strip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        strip.setTextSize(HelperFunctions.ConvertDpToPx(this, 12));
        strip.setIndicatorColor(getResources().getColor(R.color.colorAccent));
        paymentPager.setAdapter(new PaymentAdapter(getSupportFragmentManager()));
        strip.setViewPager(paymentPager);

        continueBtn = (LinearLayout) findViewById(R.id.continue_layout);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PaymentResolver.getPaymentMode() == PaymentResolver.PAYMENT_MODE.NONE){
                    //No proper payment mode is selected
                    Toast.makeText(RefillCreditsActivity.this, "Please select an appropriate package", Toast.LENGTH_SHORT).show();
                }else{
                    if(PaymentResolver.getPaymentMode() == PaymentResolver.PAYMENT_MODE.PAYPAL){
                        //Initiating paypal payment
                        payByPayPal(PaymentResolver.getAmount());
                    }else if(PaymentResolver.getPaymentMode() == PaymentResolver.PAYMENT_MODE.STRIPE){
                        //Initiating stripe payment.
                        startActivityForResult(new Intent(RefillCreditsActivity.this, StripePaymentActivity.class), STRIPE_PAYMENT_REQUEST);
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            if(paymentSuccessfull){
                setResult(RESULT_OK);
            }else{
                setResult(RESULT_CANCELED);
            }
            finish(); // close this activity and return to preview activity.
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(paymentSuccessfull){
                //Credits are used
                setResult(RESULT_OK);
            }else{
                //Credits aren't used
                setResult(RESULT_CANCELED);
            }
            finish();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
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
                        paymentSuccessfull = true;
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

        //Stripe Payment Results.
        if(requestCode == STRIPE_PAYMENT_REQUEST){
            if(resultCode == RESULT_OK){

            }
        }
    }

    /**
     * This function send the confirmation to the server on the successful transaction.
     * @param paymentId
     * @param paypal
     * @param amount
     */
    private void addMoney(String paymentId, String paypal, String amount) {
        final ProgressDialog progress = new ProgressDialog(RefillCreditsActivity.this);
        progress.setTitle("Adding Money");
        progress.setMessage("Please wait while we are adding money");
        progress.show();

        String id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        final JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("transaction_id", paymentId);
        json.addProperty("package_id", PaymentResolver.getPackageId());
        json.addProperty("amount", PaymentResolver.getAmount());

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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, RefillCreditsActivity.this)){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                String credit = jsonObject.optString("user_credit_balance");
                                ApplicationSingleTon.Credits = Integer.parseInt(credit, 0);
                                DisplayActivity.getInstance().UpdateCreditsCount(ApplicationSingleTon.Credits);
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    /**
     * This function initiates the paypal payment
     * @param amount to pay
     */
    private void payByPayPal(String amount) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Total bill",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL);
    }
}
