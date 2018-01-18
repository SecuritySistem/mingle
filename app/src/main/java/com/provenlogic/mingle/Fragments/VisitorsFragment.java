package com.provenlogic.mingle.Fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.provenlogic.mingle.Activities.DisplayActivity;
import com.provenlogic.mingle.Adapters.ActivityListAdapter;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Models.PackageDetail;
import com.provenlogic.mingle.Models.userDetail;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperFunctions;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class VisitorsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityListAdapter nearbyListAdapter;
    private ArrayList<userDetail> userDetailArrayList;
    private String id, token;
    private TextView empty_text;
    private LinearLayout ll;
    private Button activate_superpower;
    private ProgressBar progess;

    protected ArrayList<String> packageList;
    protected ArrayList<PackageDetail> packageDetailList;
    int arrayCount = 0;
    private static final int REQUEST_CODE_PAYPAL_1 = 2;
    public static PayPalConfiguration config = new PayPalConfiguration()
            .environment(Const.CONFIG_ENVIRONMENT)
            .clientId(Const.CONFIG_CLIENT_ID);

    public VisitorsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_visitors, container, false);
        id = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.USER_ID, "");
        token = (String) SharedPreferencesUtils.getParam(getActivity(), SharedPreferencesUtils.SESSION_TOKEN, "");
        recyclerView = (RecyclerView) view.findViewById(R.id.nearby_ppl);
        empty_text = (TextView) view.findViewById(R.id.empty_text);
        ll = (LinearLayout) view.findViewById(R.id.ll);
        progess = (ProgressBar) view.findViewById(R.id.progess);
        activate_superpower = (Button) view.findViewById(R.id.activate_superpower);
        activate_superpower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getActivity(), ProfileActivity.class));
                getSuperPowerPackages();
            }
        });
        nearbyListAdapter = new ActivityListAdapter(userDetailArrayList, getActivity());
        GridLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(nearbyListAdapter);

        if (userDetailArrayList.isEmpty())
            getVisitors();


        return view;
    }

    private void getVisitors() {
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        progess.setVisibility(View.VISIBLE);
        Ion.with(getActivity())
                .load(Endpoints.VisitorUrl)
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
                                boolean isSuperActivated;
                                if (successObj.optString("no_visitor_reason").equals("SUPERPOWER_NOT_ACTIVATED")) {
                                    isSuperActivated = false;
                                } else isSuperActivated = true;
                                if (isSuperActivated)
                                    ll.setVisibility(View.GONE);
                                else ll.setVisibility(View.VISIBLE);

                                JSONArray userArray = successObj.getJSONArray("visitors");
                                for (int i = 0; i < userArray.length(); i++) {
                                    JSONObject userObj = userArray.getJSONObject(i);
                                    userDetail user = new userDetail();
                                    user.setId(userObj.optString("id"));
                                    user.setName(userObj.optString("name"));
                                    JSONObject pictureObj = userObj.getJSONObject("profile_picture_url");
                                    user.setPicture(pictureObj.optString("encounter"));
                                    user.setShould_show(isSuperActivated);
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
                                return;
                            }

                            if (jsonObject.optBoolean(Const.Params.SUCCESS)) {
                                //active_status.setText("Active");
                                //active_status.setTextColor(getResources().getColor(R.color.white));
                                DisplayActivity.getInstance().UpdateSuperPowerStatus("Active", true);
                                ApplicationSingleTon.isEncounterAvailable = true;
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    /**
     * This function fetches the packages for the super power from the backend
     */
    protected void getSuperPowerPackages() {
        final ProgressDialog progress = new ProgressDialog(getActivity());
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

                            /**
                             * Logging out user if authentication fails, if user has logged in his/her account
                             * on some other device as well.
                             */
                            if(HelperFunctions.IsUserAuthenticated(jsonObject, getActivity())){
                                return;
                            }

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

    /**
     * Creates dialog box for the available packages.
     * @param packageList
     * @param packageDetailList
     */
    protected void createDialogSuperSingleChoice(ArrayList<String> packageList, final ArrayList<PackageDetail> packageDetailList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
     * Payment initiation by paypal
     * @param amount to pay
     */
    private void payByPayPalForSuper(String amount) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(amount), "USD", "Total bill",
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(getActivity(), com.paypal.android.sdk.payments.PaymentActivity.class);
        // send the same configuration for restart resiliency
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYPAL_1);
    }


}
