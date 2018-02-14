package com.smartplugapp.common;

import android.bluetooth.BluetoothDevice;

/**
 * Created by vinayts on 12-02-2018.
 */

public interface  ScanInterface {
    public void onScanStop();
    public void onDeviceDiscovered(BluetoothDevice device, int rssi, byte[] scanRecord);
}
