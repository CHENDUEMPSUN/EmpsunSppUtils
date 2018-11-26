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
  
  
   
