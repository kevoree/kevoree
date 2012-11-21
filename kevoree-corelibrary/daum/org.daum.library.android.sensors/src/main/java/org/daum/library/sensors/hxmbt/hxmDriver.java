/*
 * Copyright (C) 2010 Pye Brook Company, Inc.
 *               http://www.pyebrook.com
 *               info@pyebrook.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This software uses information from the document
 *
 *     'Bluetooth HXM API Guide 2010-07-22'
 *
 * which is Copyright (C) Zephyr Technology, and used with the permission
 * of the company. Information on Zephyr Technology products and how to 
 * obtain the Bluetooth HXM API Guide can be found on the Zephyr
 * Technology Corporation website at
 * 
 *      http://www.zephyr-technology.com
 * 
 *
 */


package org.daum.library.sensors.hxmbt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class hxmDriver extends Activity {

    /*
      *  TAG for Debugging Log
      */
    private static final String TAG = "hxmDriver";


    public static final int HXM_SERVICE_MSG_READ =0;
    public static final int HXM_SERVICE_MSG_DEVICE_NAME =  1;
    public static final int HXM_SERVICE_RESTING =  2;
    public static final int HXM_SERVICE_CONNECTING =  3;
    public static final int HXM_SERVICE_CONNECTED =4;
    public static final int HXM_SERVICE_MSG_STATE = 5;
    public static final int HXM_SERVICE_MSG_TOAST = 6;


    public final static Map messages= new HashMap() {
        {
            put(HXM_SERVICE_MSG_STATE,  "MESSAGE_STATE_CHANGE: message from HxM service indicating that the state of the service has been updated, and may have changed");
            put(HXM_SERVICE_MSG_DEVICE_NAME, " HXM_SERVICE_MSG_DEVICE_NAME: message from HxM service indicating the name of the device that a connection is associated with");
            put(HXM_SERVICE_RESTING,"HXM_SERVICE_RESTING: HxM service is at rest");
            put(HXM_SERVICE_CONNECTING, "HXM_SERIVICE_CONNECTING: HxM service is trying to connect to an HxM");
            put(HXM_SERVICE_CONNECTED,  " HXM_SERVICE_CONNECTED: HxM service is connected to a HxM");
            put(HXM_SERVICE_MSG_TOAST,"HXM_SERVICE_MSG_TOAST: message from HxM service requesting a toast be shown to the user");

        }
    };


}