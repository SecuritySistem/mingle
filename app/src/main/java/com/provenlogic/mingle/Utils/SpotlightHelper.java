package com.provenlogic.mingle.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Activities.DisplayActivity;
import com.provenlogic.mingle.Activities.PopularityActivity;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.Networking.Endpoints;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is a helper class to perform spotlight addition from multiple classes.
 * Created by Anurag on 4/6/2017.
 */

public class SpotlightHelper {
    /**
     *  This function adds user to the spot light so that other users can see him in their spotlight available in
     *  the nav bar.
     */
    public static void AddMeToSpotLight(final Activity _Activity){
        new AlertDialog.Builder(_Activity)
                .setTitle("Add me to Spotlight")
                .setMessage("This will let other users see you in their spotlight")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // continue
                        CreditDeductionPrompt(_Activity);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * This function prompts user for the credit deduction.
     */
    private static void CreditDeductionPrompt(final Activity _Activity){
        // Creating new dialog to prompt for credits deduction.
        new AlertDialog.Builder(_Activity)
                .setTitle("Credits needed")
                .setMessage("To get into spotlight 200 credits are required.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        if(ApplicationSingleTon.Credits < 200){
                            dialog.dismiss();

                            /**
                             *
                             */
                            new AlertDialog.Builder(_Activity)
                                    .setTitle("Low balance")
                                    .setMessage("You don't have enough balance to get into spotlight.")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // continue with delete
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();


                        }else{
                            /**
                             * Process the actual deduction of the credits from user account
                             */
                            dialog.dismiss();
                            ApiCallToDeductCreditsForSpotLight(_Activity);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * This function deducts the user credits from his balance and informs the backend API.
     */
    private static void ApiCallToDeductCreditsForSpotLight(final Activity _Activity){

        final ProgressDialog progress = new ProgressDialog(_Activity);
        progress.setTitle("Adding you to the Spotlight");
        progress.setMessage("Wait while contacting server and adding you to the Spotlight");
        progress.show();

        String id = (String) SharedPreferencesUtils.getParam(_Activity, SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(_Activity, SharedPreferencesUtils.SESSION_TOKEN, "");
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);

        Ion.with(_Activity)
                .load(Endpoints.spotlightAddMe)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                        progress.dismiss();
                        if(result == null){
                            SomethingWentWrong(_Activity);
                            return;
                        }

                        try{
                            JSONObject obj = new JSONObject(result.toString());
                            String status = obj.getString("status");
                            //if(status == "success")
                            {
                                JSONObject ob = obj.getJSONObject("success_data");
                                int credits = ob.getInt("user_credit_balance");
                                ApplicationSingleTon.Credits = credits;
                                AddedToSpotlight(_Activity);
                            }
                            Log.d("OBJ" , obj.toString());
                        }catch(JSONException eq){
                            Log.d("ERROR", eq.toString());
                        }
                    }
                });
    }

    /**
     * This method is called when something goes wrong
     */
    private static void SomethingWentWrong(Activity _Activity){
        new AlertDialog.Builder(_Activity)
                .setTitle("Something went wrong")
                .setMessage("We encountered some problem, please try again later.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * This function is called when user is successfully added to the spotlight
     */
    private static void AddedToSpotlight(final Activity _Activity){
        new AlertDialog.Builder(_Activity)
                .setTitle("Congratulations")
                .setMessage("You have been added to the Spotlight")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                        if(_Activity instanceof DisplayActivity){
                            DisplayActivity.getInstance().UpdateCreditsCount(ApplicationSingleTon.Credits);
                        }else if(_Activity instanceof PopularityActivity){
                            DisplayActivity.getInstance().UpdateCreditsCount(ApplicationSingleTon.Credits);
                            PopularityActivity.CreditsUsedToAddToSpotLight = true;
                        }
                    }})
                .show();
    }
}
