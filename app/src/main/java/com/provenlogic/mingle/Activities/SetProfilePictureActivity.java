package com.provenlogic.mingle.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.body.FilePart;
import com.koushikdutta.async.http.body.Part;
import com.koushikdutta.ion.Ion;
import com.provenlogic.mingle.Networking.Endpoints;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.Const;
import com.provenlogic.mingle.Utils.HelperMethods;
import com.provenlogic.mingle.Utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anurag on 12/4/17.
 */

public class SetProfilePictureActivity extends AppCompatActivity{

    private LinearLayout addPhotoLayout;
    private LinearLayout btn;
    private TextView text;
    private int PICK_IMAGE = 101;
    private ImageView image;
    private File mainFile;
    private TextView chooseAnother;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_picture);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload Profile Picture");

        addPhotoLayout = (LinearLayout) findViewById(R.id.add_photo_layout);
        addPhotoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });

        btn = (LinearLayout) findViewById(R.id.continue_layout);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(text.getText().equals("Skip For Now")){
                    //User doesn't want to upload any profile picture
                    Intent intent = new Intent(SetProfilePictureActivity.this, DisplayActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }else if(text.getText().equals("Continue")){
                    sendToServer(mainFile);
                }else if(text.getText().equals("Proceed")){
                    Intent intent = new Intent(SetProfilePictureActivity.this, DisplayActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        chooseAnother = (TextView) findViewById(R.id.choose_another_picture);
        chooseAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });

        image = (ImageView) findViewById(R.id.profile_image);
        text = (TextView) findViewById(R.id.btn_text);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }


    /**
     * Fires an Intent to pick image from the gallery
     */
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
                    addPhotoLayout.setVisibility(View.GONE);
                    InputStream selectedImage = getContentResolver().openInputStream(data.getData());
                    image.setVisibility(View.VISIBLE);
                    Bitmap bitmap = BitmapFactory.decodeStream(selectedImage);
                    mainFile = persistImage(bitmap, String.valueOf(System.currentTimeMillis()));
                    if(mainFile !=null){
                        text.setText("Continue");
                        chooseAnother.setVisibility(View.VISIBLE);
                        image.setImageBitmap(bitmap);
                    }else{
                        Toast.makeText(SetProfilePictureActivity.this, "Please select valid image", Toast.LENGTH_SHORT).show();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     *
     * @param bitmap
     * @param name
     * @return
     */
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

    /**
     *
     * @param imageFile
     */
    private void sendToServer(File imageFile) {
        final ProgressDialog progressDialog = HelperMethods.getLoadingDialog(this, "Uploading..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String id = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.USER_ID, "");
        String token = (String) SharedPreferencesUtils.getParam(this, SharedPreferencesUtils.SESSION_TOKEN, "");

        List<Part> files = new ArrayList();
        files.add(new FilePart("profile_picture", imageFile));
        Ion.with(this)
                .load(Endpoints.updateProfilePictureUrl)
                //.setJsonObjectBody(json)
                .setMultipartParameter(Const.Params.ID, id)
                .setMultipartParameter(Const.Params.TOKEN, token)

                .setMultipartParameter("crop_x", 10 + "")
                .setMultipartParameter("crop_y", 10 + "")
                .setMultipartParameter("crop_width", 100 + "")
                .setMultipartParameter("crop_height", 100 + "")
                .addMultipartParts(files)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        progressDialog.dismiss();
                        if (result == null)
                            return;

                        Log.d("RESULT", result.toString());

                        try {
                            JSONObject jsonObject = new JSONObject(result.toString());
                            if (jsonObject.optString(Const.Params.STATUS).equals("success")) {
                                Toast.makeText(SetProfilePictureActivity.this, "Profile Picture Set", Toast.LENGTH_SHORT).show();
                                chooseAnother.setVisibility(View.GONE);
                            }
                            text.setText("Proceed");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                            Log.d("Error", e1.toString());
                        }

                    }
                });
    }
}
