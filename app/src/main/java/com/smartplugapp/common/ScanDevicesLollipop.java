package com.smartplugapp.common;

/**
 * Created by vinayts on 12-02-2018.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ScanDevicesLollipop {
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
    public ScanInterface scaninterface;
    private Timer timer;
    private ScheduleTask scheduleTask;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    public boolean isScanning = true;
    public int SCANNING_INTERVAL = 30000;

    @SuppressLint("NewApi")
    public ScanDevicesLollipop(Context mcontext,int timeout) {
        context = mcontext;
        BluetoothManager bluetoothManager =
                (BluetoothManager) mcontext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanFilters = new ArrayList<ScanFilter>();
        scaninterface = (ScanInterface) mcontext;
        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        scanSettings = scanSettingsBuilder.build();
        SCANNING_INTERVAL = timeout;
        //	intComponent();
    }
    private void intandClearTimer() {
        if (scheduleTask != null && timer != null) {
            scheduleTask.cancel();
            scheduleTask = null;
            timer.cancel();
            timer = null;
        }
    }

    public void startScannig() {
        scanLEDevice(true);
    }

    private void scanLEDevice(final boolean isScanReset) {
        intandClearTimer();
        timer = new Timer();
        scheduleTask = new ScheduleTask();
        timer.schedule(scheduleTask, SCANNING_INTERVAL, SCANNING_INTERVAL);

        if (bluetoothLeScanner != null) {
            Log.e("Lollipop","blescanner is not null");
            bluetoothLeScanner.startScan(mScanCallback);
            isScanning = true;
        } else {
            Log.e("Lollipop","blescanner is null");
        }
    }

    public void stopScan() {
        intandClearTimer();
        if (bluetoothLeScanner != null && isScanning && mBluetoothAdapter.isEnabled()) {
            Log.e("Lollipop","stopping it");
            bluetoothLeScanner.stopScan(mScanCallback);
            if (scaninterface!=null) {
                Log.e("Lollipop","to call back");
                scaninterface.onScanStop();
            }
        }
        isScanning = false;
    }

    class ScheduleTask extends TimerTask {
        @Override
        public void run() {
            intandClearTimer();
            stopScan();
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            final byte[] scanRecord = result.getScanRecord().getBytes();
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            if (device == null)
                return;
            //		Log.e("Scanning","device "+device.getName());

            if (scaninterface!=null) {
                //		Log.e("Scanning","interface not null");

                scaninterface.onDeviceDiscovered(device,rssi,scanRecord);
            }
        }
    };

}