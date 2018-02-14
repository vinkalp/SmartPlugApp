package com.smartplugapp.battery_onoff;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.smartplugapp.DeviceScanActivity;
import com.smartplugapp.R;
import com.smartplugapp.common.Utils;
import com.smartplugapp.manual_onoff.Manual_onoff;

/*public class Battery_ON_OFF extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery__on__off);
    }
}*/

public class Battery_ON_OFF extends Activity {

    EditText edtName, edtLowBat, edtHighBat;
    SharedPreferences pref;
    int lowBattery = 0, highBattery = 0;
    String deviceName = "",deviceAddress = "";

    int currentBatteryLevel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery__on__off);
        DeviceScanActivity.currentContext = Battery_ON_OFF.this;

        edtName = (EditText) findViewById(R.id.edtName);
        edtHighBat = (EditText) findViewById(R.id.edtBatteryHigh);
        edtLowBat = (EditText) findViewById(R.id.edtBatteryLow);
        currentBatteryLevel = Utils.batteryLevel(this);
        Intent data = getIntent();
        if (data.getExtras()!=null) {
            deviceName = data.getStringExtra(Manual_onoff.EXTRAS_DEVICE_NAME);
            deviceAddress = data.getStringExtra(Manual_onoff.EXTRAS_DEVICE_ADDRESS);
            if (deviceName!=null) {
                edtName.setText(deviceName);
            }
        }

        pref = getSharedPreferences("BLEServiceDemo", MODE_PRIVATE);
        if (deviceAddress == null) {
            deviceAddress = pref.getString("LastMacAddress",null);
        }
        if (deviceAddress!=null) {
            highBattery = pref.getInt("HighBattery_"+deviceAddress,-1);
            lowBattery = pref.getInt("LowBattery_"+deviceAddress,-1);
            if (highBattery!=-1 && lowBattery!=-1) {
                edtName.setText(pref.getString("DeviceName_"+deviceAddress,""));
                edtHighBat.setText(""+highBattery);
                edtLowBat.setText(""+lowBattery);
            }
        }
        getActionBar().setTitle("Settings");
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void OnSaveClick(View view) {
        if (isValid()) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("LowBattery_"+deviceAddress, lowBattery);
            editor.putInt("HighBattery_"+deviceAddress, highBattery);
            editor.putString("DeviceName_"+deviceAddress, deviceName);
            editor.putString("LastMacAddress", deviceAddress);
            editor.putBoolean("LowBatteryChecked_"+deviceAddress, false);
            editor.putBoolean("HighBatteryChecked_"+deviceAddress, false);
            editor.apply();
            Toast.makeText(Battery_ON_OFF.this,"Settings Saved",Toast.LENGTH_SHORT).show();

            //send broadcast to background service
            final Intent intent = new Intent(Utils.ACTION_DATA_SAVED);
            Battery_ON_OFF.this.sendBroadcast(intent);
            Log.e("sent","broadcast sent");
        }
    }

    private boolean isValid() {
        String low = edtLowBat.getText().toString().trim();
        String high = edtHighBat.getText().toString().trim();

        lowBattery = 0;
        if (low != null && low.length() > 0) {
            lowBattery = Integer.parseInt(low);
        } else {
            Toast.makeText(Battery_ON_OFF.this, "Please Enter Low Battery Percentage", Toast.LENGTH_SHORT).show();
            return false;
        }

        highBattery = 0;
        if (high != null && high.length() > 0) {
            highBattery = Integer.parseInt(high);
        } else {
            Toast.makeText(Battery_ON_OFF.this, "Please Enter High Battery Percentage", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lowBattery < 0) {
            Toast.makeText(Battery_ON_OFF.this, "Low percentage should not be less than 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (lowBattery > 100) {
            Toast.makeText(Battery_ON_OFF.this, "Low percentage should not greater than 100", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (highBattery < 0) {
            Toast.makeText(Battery_ON_OFF.this, "High percentage should not be less than 0", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (highBattery > 100) {
            Toast.makeText(Battery_ON_OFF.this, "High percentage should not greater than 100", Toast.LENGTH_SHORT).show();
            return false;
        }

		/*if (lowBattery >= highBattery) {
			Toast.makeText(SettingActivity.this, "Low percentage should be lower then high percentage", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (currentBatteryLevel>=highBattery){
			Toast.makeText(SettingActivity.this, "Current Battery Level is already higher than high battery Percenatage", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (currentBatteryLevel<=lowBattery){
			Toast.makeText(SettingActivity.this, "Current Battery Level is already lower than low battery Percenatage", Toast.LENGTH_SHORT).show();
			return false;
		}*/

        return true;
    }

    public void OnCancelClick(View view) {
        onBackPressed();
    }
}
