package com.smartplugapp.enable_disable;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.smartplugapp.R;
import com.smartplugapp.battery_onoff.Battery_ON_OFF;
import com.smartplugapp.common.Utils;
import com.smartplugapp.manual_onoff.Manual_onoff;

public class Enable_Disable extends Activity {


    Switch manual_on_off, battery_on_off,cloud_on_off;

    private String mDeviceName;
    private String mDeviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enable__disable);

        battery_on_off = (Switch) findViewById(R.id.batery_id_ON_OFF);
        manual_on_off = (Switch) findViewById(R.id.manual_id_ON_OFF);
        cloud_on_off=(Switch)findViewById(R.id.cloud_id_ON_OFF);



        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(Utils.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(Utils.EXTRAS_DEVICE_ADDRESS);



        manual_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(getApplication(), Manual_onoff.class);
                    intent.putExtra(Utils.EXTRAS_DEVICE_NAME, mDeviceName);
                    intent.putExtra(Utils.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    startActivity(intent);
                }
            }
        });



        battery_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent intent = new Intent(getApplication(), Battery_ON_OFF.class);
                    intent.putExtra(Utils.EXTRAS_DEVICE_NAME, mDeviceName);
                    intent.putExtra(Utils.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    startActivity(intent);
                }
            }
        });

    }
}
