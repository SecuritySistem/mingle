package com.provenlogic.mingle.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.provenlogic.mingle.Applications.ApplicationSingleTon;
import com.provenlogic.mingle.R;
import com.provenlogic.mingle.Utils.SpotlightHelper;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Anurag on 4/6/2017.
 */

public class PopularityActivity extends AppCompatActivity {

    private CircleImageView p1,p2;
    private Button addMeToSpotlight;
    private Button activateSuperPower;
    /**
     * This bool is true when credits are used to spot user into the spotlight.
     */
    public static boolean CreditsUsedToAddToSpotLight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Popularity");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.activity_popularity);

        CreditsUsedToAddToSpotLight = false;

        p1 = (CircleImageView) findViewById(R.id.profile_image);
        p2 = (CircleImageView) findViewById(R.id.profile_image2);
        Glide.with(this).load(ApplicationSingleTon.imageUrl).placeholder(R.drawable.profile_placeholder).dontAnimate().into(p1);
        Glide.with(this).load(ApplicationSingleTon.imageUrl).placeholder(R.drawable.profile_placeholder).dontAnimate().into(p2);

        addMeToSpotlight = (Button) findViewById(R.id.activate_superpower2);
        /**
         * Add the user to the spotlight.
         */
        addMeToSpotlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpotlightHelper.AddMeToSpotLight(PopularityActivity.this);
            }
        });

        activateSuperPower = (Button)  findViewById(R.id.activate_superpower);
        activateSuperPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PopularityActivity.this, SuperPowerActivity.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            if(CreditsUsedToAddToSpotLight){
                //Credits are used
                setResult(RESULT_OK);
            }else{
                //Credits aren't used
                setResult(RESULT_CANCELED);
            }
            finish(); // close this activity and return to preview activity.
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(CreditsUsedToAddToSpotLight){
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
}
