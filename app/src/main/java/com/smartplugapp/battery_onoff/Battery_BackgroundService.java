package com.smartplugapp.battery_onoff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.smartplugapp.DeviceScanActivity;
import com.smartplugapp.R;
import com.smartplugapp.common.Utils;
import com.smartplugapp.receiever.BluetoothLeService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by vinayts on 13-02-2018.
 */

public class Battery_BackgroundService extends Service {
    public static final String TAG = "BLEServiceDemo";
    public Context context;
    UUID readCharUUID, writeCharUUID, notifyCHarUUID;
    boolean isNotifyEnable = false, isWriteEnable = false, isReadEnable = false;

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    String mDeviceAddress = "";
    SharedPreferences preferences;
    Timer timer = new Timer();
    TimerTask updateProfile = new CustomTimerTask(Battery_BackgroundService.this);
    public static final int INTEVAL = 25000;

    public String currentState = "";
    public Battery_BackgroundService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        preferences = this.getSharedPreferences(
                "BLEServiceDemo", Context.MODE_PRIVATE);
        //	timer.scheduleAtFixedRate(updateProfile, 0, INTEVAL);
        clreaTimer();
        startTimer();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void generateNotification(String title, String msg, String macAddress) {

        //skip for now
        Intent intent = new Intent(context, Battery_ON_OFF.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);



        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                //.setSmallIcon(R.drawable.ic_launcher)
                .setTicker("")
                .setContentTitle(title)
                .setContentText(msg)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent);



        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, b.build());


        Log.e(TAG,"at generate notification");
        goForConnection(macAddress);
    }

    private void goForConnection(String macAddress) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled() || mConnected || !Utils.isLocationEnabled(context) || (DeviceScanActivity.currentContext!=null && DeviceScanActivity.currentContext instanceof DeviceScanActivity)) {
            //	Toast.makeText(context,"Can not go for connection",Toast.LENGTH_SHORT).show();
            return;
        }
        mDeviceAddress = macAddress;
        Log.e(TAG,"going for connection with "+mDeviceAddress);
        try {
            //		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            boolean isbind = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            Log.e(TAG,"Bind Success "+isbind);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mConnected = false;
            if (mGattUpdateReceiver!=null) {
                unregisterReceiver(mGattUpdateReceiver);
            }
            if (mServiceConnection!=null) {
                unbindService(mServiceConnection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent restartService = new Intent("RestartService");
        sendBroadcast(restartService);
    }


    public class CustomTimerTask extends TimerTask {


        private Context context;
        private Handler mHandler = new Handler();

        public CustomTimerTask(Context con) {
            this.context = con;
        }
        @Override
        public void run() {
            new Thread(new Runnable() {

                public void run() {
                    mHandler.post(new Runnable() {
                        public void run() {
                            currentState = preferences.getString("state","high");
                            if (mConnected) {
                                return;
                            }
                            //if at scanning screen we'll not go for connection
                            if (DeviceScanActivity.currentContext!=null && DeviceScanActivity.currentContext instanceof DeviceScanActivity) {
                                return;
                            }
                            checkBatteryPercentage();
                            //		goForConnection("F6:FE:EA:7A:76:AA");
                        }
                    });
                }
            }).start();

        }
    }

    private void checkBatteryPercentage() {
        Log.e(TAG, "service is runing");
        if (preferences != null) {
            String lastMac = preferences.getString("LastMacAddress", null);
            if (lastMac != null) {
                Log.e(TAG, "service is runing " + lastMac);
                String deviceName;
                int highBattery = 0, lowBattery = 0;
                highBattery = preferences.getInt("HighBattery_" + lastMac, -1);
                lowBattery = preferences.getInt("LowBattery_" + lastMac, -1);
                int batteryLevel = Utils.batteryLevel(context);
                Log.e(TAG, "percentage  lower " + lowBattery + " higher " + highBattery + " current " + batteryLevel);

                if (highBattery == -1 || lowBattery == -1) {
                    return;
                }
                boolean lowBatteryChecked = preferences.getBoolean("LowBatteryChecked_" + lastMac, false);
                boolean highBatteryChecked = preferences.getBoolean("HighBatteryChecked_" + lastMac, false);

                SharedPreferences.Editor editor = preferences.edit();
                Log.e("status  ","low battery check ? "+lowBatteryChecked+" high battery check ? "+highBatteryChecked);
                if (batteryLevel <= lowBattery && !lowBatteryChecked) {
                    //connect and generate notification
                    currentState = "low";
										/*editor.putBoolean("LowBatteryChecked_" + lastMac, true);
										editor.apply();*/
                    generateNotification("Low Battery", "Battery Level is low " + batteryLevel + "%", lastMac);
                }
                if (batteryLevel >= highBattery && !highBatteryChecked) {
                    //connect and generate notification
                    currentState = "high";
										/*editor.putBoolean("HighBatteryChecked_" + lastMac, true);
										editor.apply();*/
                    generateNotification("High Battery", "Battery Level is high " + batteryLevel + "%", lastMac);
                }
            }
        } else {
            Log.e(TAG, "pref null");
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();

            if (Utils.ACTION_DATA_SAVED.equals(action)) {
                //call the service immediately
                //call the service immediately
                Log.e(TAG,"recieved boradcast go for connection");
                clreaTimer();

                startTimer();

            } else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.e(TAG,"got connected");
                //need some delay because we need to discover services yet
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mBluetoothLeService!=null){
                            Log.e("current state ","state "+currentState);
							/*if (currentState!=null && currentState.equalsIgnoreCase("smpoff")) {
								mBluetoothLeService.IntensityChange("smpon");
							} else if (currentState!=null && currentState.equalsIgnoreCase("smpon")){
								mBluetoothLeService.IntensityChange("smpoff");
							}*/
                            if (currentState!=null && currentState.equalsIgnoreCase("low")) {
                                mBluetoothLeService.ON_OFF_Logic("smpon");
                            } else if (currentState!=null && currentState.equalsIgnoreCase("high")){
                                mBluetoothLeService.ON_OFF_Logic("smpoff");
                            }
                        }
                    }
                },2500);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                try {
                    if (mBluetoothLeService!=null) {
                        //	mBluetoothLeService.disconnect();
                        unbindService(mServiceConnection);
                        mBluetoothLeService = null;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                Log.e(TAG,"got disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //    displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            } else if (BluetoothLeService.ACTION_DATA_WRITTEN.equals(action)) {
                //    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                int status = intent.getIntExtra("status",-1);
                Log.e("status  ","status at background  is "+status);
                if(status == 0) {
                    //success that data is written
                    try {
                        SharedPreferences.Editor editor = preferences.edit();
						/*if (currentState!=null && currentState.equalsIgnoreCase("smpon")) {
							editor.putString("state", "smpoff");
							editor.apply();
						} else if (currentState!=null && currentState.equalsIgnoreCase("smpoff")) {
							Log.e("status  ","high battery check done ");
							editor.putString("state", "smpon");
							editor.apply();
						}*/
                        if (currentState!=null && currentState.equalsIgnoreCase("low")) {
                            Log.e("status  ","low battery check done ");
                            editor.putBoolean("LowBatteryChecked_" + mDeviceAddress, true);
                            editor.apply();
                        } else if (currentState!=null && currentState.equalsIgnoreCase("high")) {
                            Log.e("status  ","high battery check done ");
                            editor.putBoolean("HighBatteryChecked_" + mDeviceAddress, true);
                            editor.apply();
                        }
                        mConnected = false;
                        //disconnect and unbind service
                        if (mBluetoothLeService!=null) {
                            mBluetoothLeService.disconnect();
                            unbindService(mServiceConnection);
                            mBluetoothLeService = null;

                        }
						/*if (mGattUpdateReceiver!=null) {
							unregisterReceiver(mGattUpdateReceiver);
						}*/
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
            Log.e(TAG, "onServiceConnected");
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    private void startTimer() {
        timer = new Timer();
        updateProfile = new CustomTimerTask(Battery_BackgroundService.this);
        if (timer!=null && updateProfile!=null) {
            timer.scheduleAtFixedRate(updateProfile, 0, INTEVAL);
        }
    }

    private void clreaTimer() {
        if (updateProfile != null && timer != null) {
            updateProfile.cancel();
            updateProfile = null;
            timer.cancel();
            timer = null;
        }
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITTEN);
        intentFilter.addAction(Utils.ACTION_DATA_SAVED);
        return intentFilter;
    }
}