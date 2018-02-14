package com.smartplugapp.battery_onoff;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by vinayts on 13-02-2018.
 */

public class RestartService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context,Battery_BackgroundService.class));
    }
}
