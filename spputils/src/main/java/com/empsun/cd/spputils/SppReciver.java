package com.empsun.cd.spputils;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author chen
 * @version 2018/11/20/12:28
 */

public class SppReciver extends BroadcastReceiver {
    private String TAG = "SppReciver";
    private Context context;


    public SppReciver(Context context) {
        this.context = context;

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        //监听蓝牙状态
        String action = intent.getAction();

        //蓝牙绑定状态改变的广播
        if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch(device.getBondState()) {
                //正在配对
                case BluetoothDevice.BOND_BONDING:
                    Log.e(TAG,"BOND_BONDING");
                    break;
                //配对结束
                case BluetoothDevice.BOND_BONDED:
                    Log.e(TAG,"BOND_BONDED");
                    break;
                //取消配对
                case BluetoothDevice.BOND_NONE:
                    Log.e(TAG,"BOND_NONE");
                    break;
            }
        }
        //发现可连接设备
        if (action.equals(BluetoothDevice.ACTION_FOUND)){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device!=null){
                Log.e(TAG,"发现设备："+device.getAddress());
                if (onDeviceCallBack!=null)onDeviceCallBack.onDeviceCallBack(device);
            }
        }
    }
    private OnDeviceCallBack onDeviceCallBack;
    public interface OnDeviceCallBack{
        void onDeviceCallBack(BluetoothDevice device);
    }
    public void setOnDeviceCallBack(OnDeviceCallBack onDeviceCallBack) {
        this.onDeviceCallBack = onDeviceCallBack;
    }
}
