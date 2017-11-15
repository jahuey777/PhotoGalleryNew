package com.example.jaimejahuey.photogallerybnr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jaimejahuey on 11/14/17.
 */

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, " Received broadcast intent: " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);

    }

}
