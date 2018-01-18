package com.provenlogic.mingle.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.provenlogic.mingle.Activities.DisplayActivity;
import com.provenlogic.mingle.Adapters.SuperPowerPremiumPackageAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.PackageDetail;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.PaymentResolver;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Anurag on 4/10/2017.
 */

public class SuperPowerPaypalPaymentFragment extends SuperPowerPaymentFragment implements SuperPowerPaymentFragment.SuperPowerDetailsFetchedListener{

    public static PayPalConfiguration config = new PayPalConfiguration().environment(Const.CONFIG_ENVIRONMENT).clientId(Const.CONFIG_CLIENT_ID);
    private int REQUEST_CODE_PAYPAL = 101;

    private View view;
    private ListView ls;

    public SuperPowerPaypalPaymentFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        GetSuperPowerPackages(this);
        view = inflater.inflate(R.layout.super_power_payment_fragment, null, false);
        ls = (ListView) view.findViewById(R.id.list_view);
        return view;
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onResultAvailable(ArrayList<String> packageList,final ArrayList<PackageDetail> packageDetailList) {
        ls.setAdapter(new SuperPowerPremiumPackageAdapter(getActivity(), packageList, packageDetailList));
        LinearLayout progress = (LinearLayout) view.findViewById(R.id.loading_layout);
        progress.setVisibility(View.GONE);

        ls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PaymentResolver.setPaymentMode(PaymentResolver.PAYMENT_MODE.PAYPAL);
                PaymentResolver.setCurrency(packageDetailList.get(position).getCurrency());
                PaymentResolver.setAmount(packageDetailList.get(position).getAmount());
                PaymentResolver.setPackageId(packageDetailList.get(position).getId());
                payByPayPal(PaymentResolver.getAmount());
            }
        });
        ls.setVisibility(View.VISIBLE);
    }

    /**
     * This function initiates the paypal payment
     * @param amount to pay
     */
    private void payByPayPal(String amount) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Total bill",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(getActivity(), com.paypal.android.sdk.payments.PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL);
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
                        //paymentSuccessfull = true;
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
    }

    /**
     * This function send the confirmation to the server on the successful transaction.
     * @param paymentId
     * @param paypal
     * @param amount
     */
    private void addMoney(String paymentId, String paypal, String amount) {
        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setTitle("Adding Money");
        progress.setMessage("Please wait while we are adding money");
        progress.setCancelable(false);
        progress.show();

        String id = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.SESSION_TOKEN, "");

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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
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

}
