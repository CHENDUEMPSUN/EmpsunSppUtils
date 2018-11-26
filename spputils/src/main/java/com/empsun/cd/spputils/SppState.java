/*
 * Copyright 2014 Akexorcist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.empsun.cd.spputils;

public class SppState {
    //当前连接状态
    public static final int STATE_NONE = 0;       	//没做任何事
    public static final int STATE_LISTEN = 1;     	//当前监听刚传进来的连接
    public static final int STATE_CONNECTING = 2; 	//当前开始连接
    public static final int STATE_CONNECTED = 3;  	//当前已经连接到远程设备

    //Handler发送消息的Type
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    //Intent请求码
    public static final int REQUEST_CONNECT_DEVICE = 384;
    public static final int REQUEST_ENABLE_BT = 385;


    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    
}
