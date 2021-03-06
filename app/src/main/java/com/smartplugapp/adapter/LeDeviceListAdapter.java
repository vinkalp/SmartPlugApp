package com.smartplugapp.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import com.smartplugapp.R;
/**
 * Created by vinayts on 12-02-2018.
 */

public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BluetoothDevice> mLeDevices;
    private LayoutInflater mInflator;

    public LeDeviceListAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BluetoothDevice>();
        mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addDevice(BluetoothDevice device) {
        if (!mLeDevices.contains(device)) {
            synchronized (device) {

               // mLeDevices.add(device);

                if(device==null|| device.getName()==null)
                    return;

                if(!mLeDevices.contains(device)&&device.getName().startsWith("Smart") ) {

                    mLeDevices.add(device);


                }



                notifyDataSetChanged();
            }
			/*mLeDevices.add(device);
			notifyDataSetChanged();*/
        }
    }

    public BluetoothDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BluetoothDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());

        return view;
    }
    public class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}