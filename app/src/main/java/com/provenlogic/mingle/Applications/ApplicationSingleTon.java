package com.provenlogic.mingle.Applications;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by amal on 20/02/17.
 */
public class ApplicationSingleTon extends Application {
    public static int locationPreference = 0;
    public static boolean isEncounterAvailable = false;
    public static String imageUrl = "";
    public static String name = "";

    //The credits the user owns currently.
    public static int Credits;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

}
