package com.empsun.cd.spputils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * @author chen
 * @version 2018/11/20/14:18
 */

public class SppService {
    /*打印标记*/
    private static final String TAG = "SppService";
    private static final String NAME_SECURE = "SppService_Secure";
    //蓝牙2.0通信UUID
    private static final UUID UUID_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //成员属性
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public SppService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = SppState.STATE_NONE;
        mHandler = handler;
    }

    //设置当前连接状态
    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(SppState.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    //返回当前连接状态
    public synchronized int getState() {
        return mState;
    }

    /**
     * 开启通信服务
     */
    public synchronized void start() {
        //取消一个尝试连接的线程
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        //取消一个已经连接的线程
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        //设置状态
        setState(SppState.STATE_LISTEN);
        //开启一个线程监听BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }
    }


    public synchronized void connect(BluetoothDevice device) {

        if (mState == SppState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(SppState.STATE_CONNECTING);
    }

    /**
     * 启动ConnectedThread以开始管理一个蓝牙连接
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }


        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }


        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
        Message msg = mHandler.obtainMessage(SppState.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(SppState.DEVICE_NAME, device.getName());
        bundle.putString(SppState.DEVICE_ADDRESS, device.getAddress());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(SppState.STATE_CONNECTED);
    }

    /**
     * 停止所有的线程
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread.kill();
            mSecureAcceptThread = null;
        }
        setState(SppState.STATE_NONE);
    }

    /**
     * 发送数据
     * @param out
     */
    public void write(byte[] out) {
        //创建一个临时对象
        ConnectedThread r;
        //同步复制mConnectedThread
        synchronized (this) {
            if (mState != SppState.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    /**
     * 指示连接尝试失败并通知UI活动
     */
    private void connectionFailed() {
        //重新启动服务以重新启动监听模式
        SppService.this.start();
    }

    /**
     * 断开连接
     */
    private void connectionLost() {
        SppService.this.start();
    }

    private class AcceptThread extends Thread {

        private BluetoothServerSocket mmServerSocket;
        private String mSocketType;
        boolean isRunning = true;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_DEVICE);
            } catch (IOException e) {
                Log.e(TAG+"+e",e.toString());
            }
            mmServerSocket = tmp;
        }

        public void run() {
            //设置线程名字
            setName("AcceptThread" + mSocketType);
            BluetoothSocket socket = null;
            while (mState != SppState.STATE_CONNECTED &&isRunning) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG+"+e",e.toString());
                    break;
                }

                if (socket != null) {
                    synchronized (SppService.this) {
                        switch (mState) {

                            case SppState.STATE_LISTEN:

                            case SppState.STATE_CONNECTING:
                                //情况正常。启动连接的线程。
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;
                            case SppState.STATE_NONE:

                            case SppState.STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                if (mmServerSocket!=null){
                    mmServerSocket.close();
                    Log.e(TAG,"AcceptThread: mSocket.close()");
                    mmServerSocket = null;
                }
            } catch (IOException e) {
                Log.e(TAG+"+e",e.toString());
            }
        }

        public void kill() {
            isRunning = false;
        }
    }
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
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;
            ArrayList<Integer> arr_byte = new ArrayList<Integer>();
            int length = 0;
            while (true) {
                try {
                    int data = mInStream.read();
                    arr_byte.add(data);
                    buffer = new byte[arr_byte.size()];
                    for(int i = 0 ; i < arr_byte.size() ; i++) {
                        buffer[i] = arr_byte.get(i).byteValue();
                    }
                    Log.e(TAG,data+"");
                    mHandler.obtainMessage(SppState.MESSAGE_READ, buffer.length, -1, buffer).sendToTarget();
                    arr_byte.clear();
                } catch (IOException e) {
                    connectionLost();
                    SppService.this.start();
                    break;
                }
            }
        }

        // Write to the connected OutStream.
        public void write(byte[] buffer) {
            try {
                mOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(SppState.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) { }
        }

        public void cancel() {
            try {
                mSocket.close();
                mInStream.close();
                mOutStream.close();
                Log.e(TAG,"ConnectedThread: mSocket.close()");
            } catch (IOException e) {

            }
        }
    }







}
