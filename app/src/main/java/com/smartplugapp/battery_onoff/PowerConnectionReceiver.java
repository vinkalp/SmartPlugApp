package com.smartplugapp.battery_onoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.smartplugapp.R;
import com.smartplugapp.common.Utils;

/**
 * Created by vinayts on 13-02-2018.
 */

public class PowerConnectionReceiver extends BroadcastReceiver {

    public PowerConnectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

		/*if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
			Toast.makeText(context, "The device is charging", Toast.LENGTH_SHORT).show();
		} else {
			intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED);
			Toast.makeText(context, "The device is not charging", Toast.LENGTH_SHORT).show();
		}*/
        generateNotification(context,intent);
    }

    private void generateNotification(Context ctx,Intent intentBroad) {

        String title = "";
        String msg = "";
        if (intentBroad.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            title = "Connected";
            msg = "Charger connected";
            int level = Utils.batteryLevel(ctx);
            msg = msg + "\nBattery level "+level+"%";

        } else if (intentBroad.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)){
            title = "DisConnected";
            msg = "Charger DisConnected";
            int level = Utils.batteryLevel(ctx);
            msg = msg + "\nBattery level "+level+"%";

        } else if (intentBroad.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
            int level = intentBroad.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (level!=-1) {
                Log.e("Battery Level","level is "+level);
                msg = msg + "Battery level "+level+"%";
            }
        }

        Intent intent = new Intent(ctx, Battery_ON_OFF.class);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
               .setSmallIcon(R.drawable.ic_launcher_background)
                .setTicker("")
                .setContentTitle(title)
                .setContentText(msg)
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent);
				/*.setContentInfo("Info");*/


        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());
    }



}