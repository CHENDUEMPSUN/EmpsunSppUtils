package com.empsun.cd.empsunspputils;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.empsun.cd.spputils.SppList;
import com.empsun.cd.spputils.SppState;
import com.empsun.cd.spputils.SppStringUtils;
import com.empsun.cd.spputils.SppUtils;

public class MainActivity extends AppCompatActivity {

    private SppUtils mSppUtils;
    private TextView mReciveData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initID();
        mSppUtils = new SppUtils(this);
        mSppUtils.startService();

        if(!mSppUtils.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "手机或者平板蓝牙不可用", Toast.LENGTH_SHORT).show();
        }

        mSppUtils.setBluetoothConnectionListener(new SppUtils.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext(), "连接成功 " + name, Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext(), "断开连接", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
            }
        });

        //数据的回调
        mSppUtils.setOnDataReceivedListener(new SppUtils.OnDataReceivedListener() {
            @Override
            public void onDataReceived(final byte[] data, String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mReciveData.setText(mReciveData.getText()+ SppStringUtils.bytesToHexString(data));
                    }
                });

            }
        });


    }



    private void initID() {
        mReciveData = (TextView) findViewById(R.id.mReciveData);
    }

    public void onDestroy() {
        super.onDestroy();
        mSppUtils.stopService();
    }

    public void onStart() {
        super.onStart();
        if(!mSppUtils.isBluetoothEnabled()) {
            mSppUtils.enable();
        } else {
            if(!mSppUtils.isServiceAvailable()) {
                mSppUtils.setupService();
                mSppUtils.startService();

            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(requestCode == SppState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                mSppUtils.connect(data);
        } else if(requestCode == SppState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                mSppUtils.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    /**
     * 清除已配对的设备
     * @param view
     */
    public void clear(View view) {
        mSppUtils.unPairDevices();
    }

    /**
     * 断开连接
     * @param view
     */
    public void disconnect(View view) {
        if(mSppUtils.getServiceState() == SppState.STATE_CONNECTED) {
            mSppUtils.disconnect();
        }
    }

    /**
     * 连接
     * @param view
     */
    public void connect(View view) {
        //先断开来连接
        if(mSppUtils.getServiceState() == SppState.STATE_CONNECTED) {
            mSppUtils.disconnect();
        }
        //进入设备页面进行选择
        Intent intent = new Intent(getApplicationContext(), SppList.class);
        startActivityForResult(intent, SppState.REQUEST_CONNECT_DEVICE);
    }

    /**
     * 发送数据
     * @param view
     */
    public void writeData(View view) {
        mSppUtils.send("UIH".getBytes(), false);
    }


}
