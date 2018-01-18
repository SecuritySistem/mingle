package com.provenlogic.mingle.Receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * This class is to automatically start the push notification service after device boot.
 * Created by Anurag on 15/4/17.
 */

public class VoodooWakefulBroadcastReceiver extends WakefulBroadcastReceiver{
    public VoodooWakefulBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
