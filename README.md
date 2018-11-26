# EmpsunSppUtils
### 使用集成:

```
allprojects {
    repositories {
        google()
        jcenter()
        //在Project的grade文件中添加
        maven { url 'https://jitpack.io' }
    }
}
```
```
//在Module添加依赖
implementation 'com.github.CHENDUEMPSUN:EmpsunSppUtils:v1.0.0'
```

### Step1:onCreat()

```
//获得工具操作对象
SppUtils sppUtils = new SppUtils(this);
//开启蓝牙服务
mSppUtils.setupService();
mSppUtils.startService();
```

### Step2:onStart()

```
public void onStart() {
     super.onStart();
     if(!mSppUtils.isBluetoothEnabled()) {
        //开启蓝牙
        mSppUtils.enable();
     } else {
        //开启服务
        if(!mSppUtils.isServiceAvailable()) {
            mSppUtils.setupService();
            mSppUtils.startService();
         }
    }
}
```

### Step3:onDestroy()

```
public void onDestroy() {
    super.onDestroy();
    //断开蓝牙连接
    if(mSppUtils.getServiceState() == SppState.STATE_CONNECTED) {
       mSppUtils.disconnect();
    }
    //停止服务
    mSppUtils.stopService();
}
```

### 搜索

```
//开启搜索
sppUtils.startDiscovery();
//搜索的回调
sppUtils.setOnDeviceCallBack(new SppUtils.OnDeviceCallBack() {
      @Override
      public void onDeviceCallBack(BluetoothDevice bluetoothDevice) {
            //search some BluetoothDevice
            
            }
        });
 ```
 
 ### 连接
 ```
 mSppUtils.connect("蓝牙地址");
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
  ```      
  
   ### 发送数据
   
   ```
   mSppUtils.send("byte数组", false);
   ```
   
   ### 接收数据
   ```
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
   ```
  ### 断开连接
  ```
  //断开蓝牙连接
  if(mSppUtils.getServiceState() == SppState.STATE_CONNECTED) {
       mSppUtils.disconnect();
    }
  ```
  ### 清除已配对的设备
  ```
  mSppUtils.unPairDevices();
  ```
  ### 16进制字符串与byte数组的转换
  
  ```
  mSppUtils.bytesToHexString();
  mSppUtils.hexStringToBytes();
  ```
  ### 备注：
  >1、在Library中加入定位权限，不然在Android6.0以上系统上使用，搜索不到蓝牙
  
  >2、本Module采用通信方式为非安全模式 不需要通过输入pin码进行配对
  
  ```
  private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                //Android2.3以下的API 这种方式会需要pin码 安全通信
                //tmp = device.createRfcommSocketToServiceRecord(UUID_DEVICE);
                //这种方式会不需要pin码 非安全通信
                tmp = device.createInsecureRfcommSocketToServiceRecord(UUID_DEVICE);

            } catch (IOException e) {

            }
            mmSocket = tmp;
        }

        public void run() {
            //总是取消搜索，因为它会减慢连接速度
            mAdapter.cancelDiscovery();
            try {
                //这是一个阻塞调用，只会在连接成功或异常时返回
                mmSocket.connect();
            } catch (IOException e) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {

                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (SppService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
                Log.e(TAG,"ConnectThread: mmSocket.close()");
            } catch (IOException e) {

            }
        }
    }
  ```
  
  >3、市面上有很多通过反射拿到BlueDevice的方法设置pin码,进行自动配对。但是这种只适用于Androidx.x以下系统，x.x以上系统同样会弹出输入pin码的Dialog，因为x.x之后：@RequiresPermission(Manifest.permission.BLUETOOTH_PRIVILEGED)这个时系统应用权限，第三方应用根本没有这个权限，在5.0上反射是拿不到这个方法的，所以x.x以上系统实现不了自动配对，依然是弹出对话框输入pin码。(具体是哪个系统版本暂未查证)
  除非：采用非安全通信方式就不需要输入pin码
  
  ```
      /**
     * Confirm passkey for {@link #PAIRING_VARIANT_PASSKEY_CONFIRMATION} pairing.
     *
     * @return true confirmation has been sent out
     *         false for error
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_PRIVILEGED)
    public boolean setPairingConfirmation(boolean confirm) {
        if (sService == null) {
            Log.e(TAG, "BT not enabled. Cannot set pairing confirmation");
            return false;
        }
        try {
            return sService.setPairingConfirmation(this, confirm);
        } catch (RemoteException e) {Log.e(TAG, "", e);}
        return false;
    }

  ```
  
  
  
   
