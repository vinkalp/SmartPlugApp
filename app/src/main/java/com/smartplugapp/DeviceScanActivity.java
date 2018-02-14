package com.smartplugapp;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.smartplugapp.adapter.LeDeviceListAdapter;
import com.smartplugapp.battery_onoff.Battery_BackgroundService;
import com.smartplugapp.common.ScanDevicesKitkat;
import com.smartplugapp.common.ScanDevicesLollipop;
import com.smartplugapp.common.ScanInterface;
import com.smartplugapp.common.Utils;
import com.smartplugapp.enable_disable.Enable_Disable;
import com.smartplugapp.manual_onoff.Manual_onoff;

/**
 * Created by vinayts on 12-02-2018.
 */

public class DeviceScanActivity extends ListActivity implements ScanInterface {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    //    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
//    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final int SCAN_PERIOD = 20000;
    ScanDevicesKitkat scanDevicesKitkat;
    ScanDevicesLollipop scanDevicesLollipop;
    public static Context currentContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getActionBar().setTitle(R.string.title_devices);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //check permission and start scanning
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        setListAdapter(mLeDeviceListAdapter);
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            chedkForPermission();
        }

        currentContext = DeviceScanActivity.this;
        startService(new Intent(this, Battery_BackgroundService.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                if (mLeDeviceListAdapter != null) {
                    mLeDeviceListAdapter.clear();
                }
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //    chedkForPermission();
        if (mLeDeviceListAdapter!=null) {
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    }

    private void chedkForPermission() {
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeviceScanActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        101);
            } else {
                // Initializes list view adapter.
                if (!Utils.isLocationEnabled(DeviceScanActivity.this)) {
                    Toast.makeText(DeviceScanActivity.this, "Please Turn On location to allow scanning", Toast.LENGTH_SHORT).show();
                } else {
                    scanLeDevice(true);
                }
            }
        } else {
            // Initializes list view adapter.
            if (!Utils.isLocationEnabled(DeviceScanActivity.this)) {
                Toast.makeText(DeviceScanActivity.this, "Please Turn On location to allow scanning", Toast.LENGTH_SHORT).show();
            } else {
                scanLeDevice(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Initializes list view adapter.
                    scanLeDevice(true);
                } else {
                    if (!Utils.isLocationEnabled(DeviceScanActivity.this)) {
                        Toast.makeText(DeviceScanActivity.this, "Please Turn On location to allow scanning", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DeviceScanActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            chedkForPermission();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            currentContext = null;
            int MyVersion = Build.VERSION.SDK_INT;
            if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            if (!Utils.isLocationEnabled(DeviceScanActivity.this)) {
                return;
            }
            scanLeDevice(false);
            mLeDeviceListAdapter.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        //	final Intent intent = new Intent(this, DeviceControlActivity.class);

        final Intent intent = new Intent(this, Enable_Disable.class);
        intent.putExtra(Manual_onoff.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(Manual_onoff.EXTRAS_DEVICE_ADDRESS, device.getAddress());

        if (mScanning) {
            scanLeDevice(false);
            mScanning = false;
        }
        startActivity(intent);
    }

    private void scanLeDevice(final boolean enable) {
        if (mLeDeviceListAdapter == null) {
            mLeDeviceListAdapter = new LeDeviceListAdapter(DeviceScanActivity.this);
            setListAdapter(mLeDeviceListAdapter);
        }
        if (enable) {
            //ask Reason for Kalpesh for commenting this code and it scan the devices again.

            // Stops scanning after a pre-defined scan period.
			/*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
				scanDevicesLollipop = new ScanDevicesLollipop(this, SCAN_PERIOD);
				scanDevicesLollipop.startScannig();
			} else {
				scanDevicesKitkat = new ScanDevicesKitkat(this, SCAN_PERIOD);
				scanDevicesKitkat.startScannig();
			}*/
            scanDevicesKitkat = new ScanDevicesKitkat(this, SCAN_PERIOD);
            scanDevicesKitkat.startScannig();
            mScanning = true;
        } else {
            mScanning = false;
            Log.e("STOPPING", "stopping it");
			/*if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
				if (scanDevicesLollipop != null) {
					scanDevicesLollipop.stopScan();
				}
			} else {
				if (scanDevicesKitkat != null) {
					scanDevicesKitkat.stopScan();
				}
			}*/
            if (scanDevicesKitkat != null) {
                scanDevicesKitkat.stopScan();
            }
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onScanStop() {
        mScanning = false;
        invalidateOptionsMenu();
        //scanLeDevice(false);
    }

    @Override
    public void onDeviceDiscovered(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLeDeviceListAdapter != null && device !=null) {
                    mLeDeviceListAdapter.addDevice(device);
                }
            }
        });
    }
}