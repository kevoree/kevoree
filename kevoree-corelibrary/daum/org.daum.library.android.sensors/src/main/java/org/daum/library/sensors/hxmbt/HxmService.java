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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import org.daum.library.sensors.ZephyrHM;

/**
 * This class does all the work for setting up and managing the 
 * Bluetooth connection to the HxM device.  It also does the parsing of the
 * packet structure.
 *
 * It has a thread for connecting to the HxM device, and a
 * thread for performing data transmissions when connected.
 */
public class HxmService {
    // Debugging
    private static final String TAG = "HrmService";

    // Unique UUID for use by this application, it is the generic & well-known SPP UID
    @SuppressWarnings("unused")
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final ZephyrHM zephyrhm;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;



    /*
     * Constructor. Prepares a new HxmService session.
     * @param context  A  UI Activity Context
     * @param handler  the Handler to send messages back to the UI Activity
     * 
     * Our constructor doesn't have to do much,  save the information from the activity 
     * instantiating the service and note the state.
     */
    public HxmService(Context context, ZephyrHM handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = hxmDriver.HXM_SERVICE_RESTING;
        zephyrhm = handler;
    }

    /*
     * Set the current state of the HxmService connection to the device
     * @param state  The states are defined in the string resource
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /*
     * Return the current connection state. 
     * 
     */
    public synchronized int getState() {
        return mState;
    }

    /*
    * Start the HxM service. Specifically start AcceptThread to begin a
    * session in listening (server) mode. Called by the Activity onResume()
    *
    */
    public synchronized void start() {
        Log.d(TAG, "start()");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel(); mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); mConnectedThread = null;
        }

        setState( hxmDriver.HXM_SERVICE_RESTING);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect with
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect(): starting connection to " + device);

        // If a connection attempt is currently in progress, cancel it!
        if (mState ==  hxmDriver.HXM_SERVICE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel(); mConnectThread = null;
            }
        }

        // If a connection currently active, cancel it!
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Make the connection
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState( hxmDriver.HXM_SERVICE_CONNECTING);
    }

    /*
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected() starting ");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel(); mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel(); mConnectedThread = null;
        }

        // Start the thread to manage the connection and read the data from the device
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState( hxmDriver.HXM_SERVICE_CONNECTED);
        Log.d(TAG, "connected() finished");

    }

    /*
     * Stop all connections, put the HxM service back into the resting state
     */
    public synchronized void stop() {
        Log.d(TAG, "stop() starting ---- ok, it's a little funny:)");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState( hxmDriver.HXM_SERVICE_RESTING);
        Log.d(TAG, "stop() finished");
    }

    /*
     * Reset all connections, put the HxM service back into the resting state
     * functionally equivalent to stop with this version of the bluetooth implementation
     */
    @SuppressWarnings("unused")
    private synchronized void reset() {
        Log.d(TAG, "reset() starting");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState( hxmDriver.HXM_SERVICE_RESTING);
        Log.d(TAG, "reset() finished");
    }


    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.d(TAG, "BEGIN connectionFailed");

        setState( hxmDriver.HXM_SERVICE_RESTING);
        zephyrhm.connectLost();

        Log.d(TAG, "END connectionFailed");

    }

    /*
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost()
    {
        setState( hxmDriver.HXM_SERVICE_RESTING);
        Log.d(TAG, "END connectionFailed");
        zephyrhm.connectLost();
    }


    /*
     * This thread runs while attempting to create the outgoing connection
     * with a HxM. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                /* ****************************************************************************************
                     * IMPORTANT!   IMPORTANT!   IMPORTANT!   IMPORTANT!   IMPORTANT!   IMPORTANT!   IMPORTANT!
                     * ****************************************************************************************
                     *
                     * There are some 'issues' with the Bluetooth issues with some versions of Android, and with
                     * some specific devices.  Ordinarily all you would have to do to create the BLuetooth socket
                     * is use the create call, specifying the UID of the Bluetooth service profile that will be
                     * used for the connection.     In the case of our application that call would look
                     * like this:
                     *                  tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                     *
                     * The problem is, sometimes when you make that call an error will return either from the
                     * attempt to create the socket, or later when the attempt is made to connect to the socket.
                     * The error code most often reported is 'Unable to start service discovery For device'.
                     *
                     * Obviously this is a problem because you can't create a connection to any device on any
                     * known UID if the call does not work.
                     *
                     * There is a technique that has been used to work around this issue.  It is referred to as
                     * 'java reflection'.  If you are not familiar and are interested, an Internet search for
                     * the term will give you lots of interesting reading on the topic.
                     *
                     * The important thing for us is that it gives us a means to call directly into the
                     * Bluetooth rfcomm class/object avoiding whatever problem is present in the current
                     * Android+Bluetooth+Handset implementation.
                     *
                     * Our application creates the connection using this technique as follows:
                     *
                     *   	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                     * 	    tmp = (BluetoothSocket) m.invoke(device, 1);
                     *
                     *
                     * CAUTION:
                     * The problem with doing this is that there isn't any guarantee that the call we are accessing
                     * will be there in future versions of the platform.  And if it is there no guarantee that
                     * it will work in the same manner.  If you look at the current implementation of the rfcomm
                     * object you will find several places where there is embedded commentary warning developers
                     * noting that the class is likely to change in the future.
                     *
                     * When you build your applications, consider if it is appropriate to restrict the allowed
                     * platforms to the ones that you have thoroughly tested.  You may also consider an
                     * implementation that uses both the standard implementation and the workaround implementation
                     * such that if the documented approach does not work, the workaround is attempted as a
                     * fall-back.
                     *
                     * It is also advisable to make sure that the error reporting mechanism within your application is
                     * especially robust so that problems that users have that may end up being related to the bluetooth
                     * issue are quickly isolated.
                     *
                     */
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                tmp = (BluetoothSocket) m.invoke(device, 1);
            } catch (SecurityException e) {
                Log.e(TAG, "ConnectThread() SecurityException");
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "ConnectThread() SecurityException");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "ConnectThread() SecurityException");
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                Log.e(TAG, "ConnectThread() SecurityException");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                Log.e(TAG, "ConnectThread() SecurityException");
                e.printStackTrace();
            }

            mmSocket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "ConnectThread.run(): unable to close() socket during connection failure", e2);
                }

                // Start the service over to restart listening mode
                HxmService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (HxmService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel(): close() of connect socket failed", e);
            }
        }
    }

    /*
     * This thread runs during a connection with the Hxm.
     * It handles all incoming data
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread(): starting");

            mmSocket = socket;
            InputStream tmpIn = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread(): temp sockets not created", e);
            }

            mmInStream = tmpIn;

            Log.d(TAG, "ConnectedThread(): finished");

        }

        /*
         * The code below is a basic implementation of a reader specific to the HxM device.  It is 
         * intended to illustrate the packet structure and field extraction.  Consider if your 
         * implementation should include more robust error detection logic to prevent things like 
         * buffer sizes from causing read overruns, or recomputing the CRC and comparing it to the 
         * contents of the message to detect transmission erros.
         */
        private final int STX = 0x02;
        private final int MSGID = 0x26;
        private final int DLC = 55;
        private final int ETX = 0x03;

        @Override
        public void run() {
            Log.d(TAG, "ConnectedThread.run(): starting");
            byte[] buffer = new byte[1024];
            int b = 0;
            int bufferIndex = 0;
            int payloadBytesRemaining;

            // Keep listening to the InputStream while connected
            while (true) {
                try {

                    bufferIndex = 0;
                    // Read bytes from the stream until we encounter the the start of message character
                    while (( b = mmInStream.read()) != STX )
                        ;

                    buffer[bufferIndex++] = (byte) b;

                    // The next byte must be the message ID, see the basic message format in the document
                    if ((b = mmInStream.read()) != MSGID )
                        continue;

                    buffer[bufferIndex++] = (byte) b;

                    // The next byte must be the expected data length code, we don't handle variable length messages, see the doc
                    if ((b = mmInStream.read()) != DLC )
                        continue;

                    buffer[bufferIndex++] = (byte) b;

                    payloadBytesRemaining = b;

                    while ( (payloadBytesRemaining--) > 0 ) {
                        buffer[bufferIndex++] = (byte) (b = mmInStream.read());
                    }

                    // The next byte should be a CRC
                    buffer[bufferIndex++] = (byte) (b = mmInStream.read());

                    // The next byte must be the end of text indicator, or there was sadness, see the basic message format in the document
                    if ((b = mmInStream.read()) != ETX )
                        continue;

                    buffer[bufferIndex++] = (byte) b;

                    Log.d(TAG, "mConnectedThread: read "+Integer.toString(bufferIndex)+" bytes");

                    zephyrhm.processReading(buffer);


                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }

            Log.d(TAG, "ConnectedThread.run(): finished");

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "ConnectedThread.cancel(): close() of connect socket failed", e);
            }
        }
    }
}