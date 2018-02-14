package com.smartplugapp.common;

/**
 * Created by vinayts on 12-02-2018.
 */

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;


import java.util.Timer;
import java.util.TimerTask;

public class ScanDevicesKitkat {
    private BluetoothAdapter mBluetoothAdapter;
    private Context context;
    public ScanInterface scaninterface;
    private Timer timer;
    private ScheduleTask scheduleTask;
    public boolean isScanning = true;
    public int SCANNING_INTERVAL = 30000;

    @SuppressLint("NewApi")
    //this is for bda scanning
    public ScanDevicesKitkat(Context mcontext,int timeOut) {
        context = mcontext;
        BluetoothManager bluetoothManager =
                (BluetoothManager) mcontext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        scaninterface = (ScanInterface)mcontext;
        SCANNING_INTERVAL = timeOut;
//        intComponent();
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

    @SuppressWarnings("deprecation")
    private void scanLEDevice(final boolean isScanReset) {
        intandClearTimer();
        timer = new Timer();
        scheduleTask = new ScheduleTask();
        timer.schedule(scheduleTask, SCANNING_INTERVAL, SCANNING_INTERVAL);

        if (mBluetoothAdapter != null) {
            boolean startScan = mBluetoothAdapter.startLeScan(mLeScanCallback);
            isScanning = true;
        }
    }

    @SuppressWarnings("deprecation")
    public void stopScan() {
        intandClearTimer();
        if (mBluetoothAdapter != null && isScanning && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (scaninterface!=null) {
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

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            if (device == null)
                return;
            if (scaninterface!=null) {
                scaninterface.onDeviceDiscovered(device,rssi,scanRecord);
            }
        }
    };

}