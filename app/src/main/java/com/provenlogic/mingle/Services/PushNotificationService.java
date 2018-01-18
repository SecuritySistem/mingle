package com.provenlogic.mingle.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by Anurag on 15/4/17.
 */

public class PushNotificationService extends IntentService{

    public PushNotificationService(){
        super("Voodoo Push Notification Service");
    }

    public PushNotificationService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // This describes what will happen when service is triggered
    }
}
