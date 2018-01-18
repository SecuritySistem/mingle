package com.provenlogic.mingle.Utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Activities.ProfileActivity;
import com.provenlogic.mingle.MainActivity;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Anurag on 4/6/2017.
 */
public class HelperFunctions {

    /**
     * This function updates the user about me status from its profile activity.
     * @param activity who called this function.
     */
    public static void UpdateAboutMeStatus(final Activity activity, final String id, final String token){
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        LayoutInflater inf = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inf.inflate(R.layout.alert_edittext, null, false);
        final EditText getInfo = (EditText) view.findViewById(R.id.alertDialog_editText);
        alert.setMessage("Enter your updated information");
        alert.setTitle("Update your About me info");
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newUpdate = getInfo.getText().toString();
                if(newUpdate.length() > 50){
                    dialog.dismiss();
                    CallApiToUpdateAboutMeInfo(activity, newUpdate, id, token);
                }else{
                    Toast.makeText(activity, "Enter at least 50 characters", Toast.LENGTH_LONG).show();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                dialog.dismiss();
            }
        });

        alert.show();
    }

    /**
     * This function updates the user's about me status at the server.
     * @param activity who called this function
     * @param newUpdate is new updated message
     * @param id of the user
     * @param token of the user
     */
    private static void CallApiToUpdateAboutMeInfo(final Activity activity, final String newUpdate, String id, String token){
        final ProgressDialog progress = new ProgressDialog(activity);
        progress.setTitle("Updating your status");
        progress.setMessage("Please wait while updating your status on server");
        progress.show();
        JsonObject json = new JsonObject();
        json.addProperty(Const.Params.ID, id);
        json.addProperty(Const.Params.TOKEN, token);
        json.addProperty("about_me", newUpdate );

        Ion.with(activity)
                .load(Endpoints.updateAboutMe)
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        Log.d("About me Updated", result.toString());
                        progress.dismiss();

                        if(result == null){
                            ShowSimpleDialogMessage(activity, "Something went wrong", "Something is not right, please try again");
                            return;
                        }
                        try{
                            JSONObject obj = new JSONObject(result.toString());
                            if(obj.getString("status").equals("success")){
                                ShowSimpleDialogMessage(activity, "Congrats", "Your about me status was successfully updated");

                                //Updating the about me status on the UI.
                                if(activity instanceof ProfileActivity){
                                    ProfileActivity act = (ProfileActivity) activity;
                                    act.UpdateAboutMeStatus(newUpdate);
                                    return;
                                }
                            }
                        }catch(JSONException eq){

                        }
                        ShowSimpleDialogMessage(activity, "Something went wrong", "Something is not right, please try again");
                    }
                });
    }

    /**
     * This function shows the simple dialog box.
     * @param activity who called this function
     * @param title of the dialog box
     * @param message of the dialog box
     */
    public static void ShowSimpleDialogMessage(Activity activity, String title, String message){
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                        //DisplayActivity.getInstance().UpdateCreditsCount(ApplicationSingleTon.Credits);
                    }})
                .show();
    }

    /**
     * This function converts the seconds into proper time
     * @param seconds
     * @return the proper time in string format
     */
    public static String ConvertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h,m,s);
    }

    /**
     * This function converts the xml dp to the screen pixels
     * @param context who called this method
     * @param dp to convert to pixels
     * @return
     */
    public static int ConvertDpToPx(Activity context, int dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    /**
     *
     * Checks whether user is authenticated or not, if not takes user to main login screen.
     * @param obj
     * @return true is user is authenticated, else false
     */
    public static boolean IsUserAuthenticated(JSONObject obj, Activity activity){
       try{
           if(obj == null) {
                Log.d("AUTH OBJ", "AUTH OBJ IS NULL");
               return false;
           }
           String status = obj.getString("status");
           if(status.equalsIgnoreCase("error")){
               //JSONArray arr = obj.getJSONArray("error_data");
               JSONObject ob = obj.getJSONObject("error_data");
               String authText = ob.getString("error_text");
               if(authText.equalsIgnoreCase("Authentication Error")){
                   SharedPreferencesUtils.setParam(activity, SharedPreferencesUtils.SESSION_TOKEN, "");
                   SharedPreferencesUtils.setParam(activity, SharedPreferencesUtils.USER_ID, "");
                   Intent intent = new Intent(activity, MainActivity.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                   activity.startActivity(intent);
                   //activity.startActivity(new Intent(activity, MainActivity.class));
                   Toast.makeText(activity, "You are not authenticated, logging you out", Toast.LENGTH_SHORT).show();
                   return false;
               }
               Log.d("Authenticated", "Authenticated");
               return true;
           }
       }catch(JSONException e){
           Log.d("Auth Exception", e.toString());
       }
        return false;
    }
}
