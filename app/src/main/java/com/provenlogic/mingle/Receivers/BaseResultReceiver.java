package com.provenlogic.mingle.Receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by muthu on 15/4/17.
 */

public class BaseResultReceiver extends ResultReceiver{

    /**
     *
     */
    public interface NewNotificationAvailableListener{
        void onNewNotificationAvaliable(int notificationCode, Bundle notificationData);
    }

    //
    private NewNotificationAvailableListener notificationListener;

    /**
     * The default constructor of this class.
     * @param handler
     */
    public BaseResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     *
     * @param notificationListener
     */
    protected void setNotificationListsner(NewNotificationAvailableListener notificationListener){
        this.notificationListener = notificationListener;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if(notificationListener != null){
            notificationListener.onNewNotificationAvaliable(resultCode, resultData);
        }
    }
}
