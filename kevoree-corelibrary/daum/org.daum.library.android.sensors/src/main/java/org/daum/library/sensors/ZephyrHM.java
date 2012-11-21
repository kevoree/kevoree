package org.daum.library.sensors;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import org.daum.library.sensors.hxmbt.HxmService;
import org.daum.library.sensors.hxmbt.hxmDriver;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import org.kevoree.framework.MessagePort;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 25/10/12
 * Time: 15:23
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "Android")
@Requires({
        @RequiredPort(name = "heartRate", type = PortType.MESSAGE,optional = true)
})
@ComponentType
public class ZephyrHM  extends AbstractComponentType {

    private static final String TAG = "ZephyrHM";

    /*
    * Name of the connected device, and it's address
    */
    private String mHxMName = null;
    private String mHxMAddress = null;

    /*
    * Local Bluetooth adapter
    */
    private BluetoothAdapter mBluetoothAdapter = null;

    /*
     * Member object for the chat services
     */
    private  HxmService mHxmService = null;

    @Start
    public void start()
    {
        connect();
    }

    public  void connect(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            /*
                * Blutoooth needs to be available on this device, and also enabled.
                */
            Toast.makeText( UIServiceHandler.getUIService().getRootActivity(), "Bluetooth is not available or not enabled", Toast.LENGTH_LONG).show();
        }


        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText( UIServiceHandler.getUIService().getRootActivity(), "Blueooth adapter detected, but it's not enabled", Toast.LENGTH_LONG).show();
            Log.d(TAG, "onStart: Blueooth adapter detected, but it's not enabled ");
        } else {


            /*
            * Setup the service that will talk with the Hxm
            */
            if (mHxmService == null)
                mHxmService = new HxmService( UIServiceHandler.getUIService().getRootActivity(), this);

            /*
            * Look for an Hxm to connect to, if none is found tell the user
            * about it
            */
            if ( getFirstConnectedHxm() ) {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mHxMAddress);
                mHxmService.connect(device); 	// Attempt to connect to the device
            } else {
                //   mStatus.setText(hxmDriver.nonePaired);
            }


        }

    }

    @Stop
    public void stop()
    {
        if(mHxmService !=null)
            mHxmService.stop();

    }

    public void connectLost()
    {
        if(mHxmService !=null)
            mHxmService.stop();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        connect();
    }

    public void processReading(byte[] msg){
        byte[] readBuf = (byte[]) msg;
        HrmReading hrm = new HrmReading( readBuf );
        hrm.displayRaw();
    }
    @Update
    public void update()
    {

    }


    private boolean getFirstConnectedHxm() {

        /*
           * Initialize the global device address to null, that means we haven't
           * found a HxM to connect to yet
           */
        mHxMAddress = null;
        mHxMName = null;


        /*
           * Get the local Bluetooth adapter
           */
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        /*
           *  Get a set of currently paired devices to cycle through, the Zephyr HxM must
           *  be paired to this Android device, and the bluetooth adapter must be enabled
           */
        Set<BluetoothDevice> bondedDevices = mBtAdapter.getBondedDevices();

        /*
           * For each device check to see if it starts with HXM, if it does assume it
           * is the Zephyr HxM device we want to pair with
           */
        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                String deviceName = device.getName();
                if ( deviceName.startsWith("HXM") ) {
                    /*
                          * we found an HxM to try to talk to!, let's remember its name and
                          * stop looking for more
                          */
                    mHxMAddress = device.getAddress();
                    mHxMName = device.getName();
                    Log.d(TAG,"getFirstConnectedHxm() found a device whose name starts with 'HXM', its name is "+mHxMName+" and its address is ++mHxMAddress");
                    break;
                }
            }
        }

        /*
           * return true if we found an HxM and set the global device address
           */
        return (mHxMAddress != null);
    }



    public class HrmReading {
        public final int STX = 0x02;
        public final int MSGID = 0x26;
        public final int DLC = 55;
        public final int ETX = 0x03;

        private static final String TAG = "HrmReading";

        int serial;
        byte stx;
        byte msgId;
        byte dlc;
        int firmwareId;
        int firmwareVersion;
        int hardWareId;
        int hardwareVersion;
        int batteryIndicator;
        int heartRate;
        int heartBeatNumber;
        long hbTime1;
        long hbTime2;
        long hbTime3;
        long hbTime4;
        long hbTime5;
        long hbTime6;
        long hbTime7;
        long hbTime8;
        long hbTime9;
        long hbTime10;
        long hbTime11;
        long hbTime12;
        long hbTime13;
        long hbTime14;
        long hbTime15;
        long reserved1;
        long reserved2;
        long reserved3;
        long distance;
        long speed;
        byte strides;
        byte reserved4;
        long reserved5;
        byte crc;
        byte etx;

        public  HrmReading (byte[] buffer) {
            int bufferIndex = 0;

            Log.d ( TAG, "HrmReading being built from byte buffer");

            try {
                stx 				= buffer[bufferIndex++];
                msgId 				= buffer[bufferIndex++];
                dlc 				= buffer[bufferIndex++];
                firmwareId 			= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                firmwareVersion 	= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hardWareId 			= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hardwareVersion		= (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                batteryIndicator  	= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
                heartRate  			= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
                heartBeatNumber  	= (int)(0x000000FF & (int)(buffer[bufferIndex++]));
                hbTime1				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime2				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime3				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime4				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime5				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime6				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime7				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime8				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime9				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime10			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime11			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime12			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime13			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime14			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                hbTime15			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                reserved1			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                reserved2			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                reserved3			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                distance			= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                speed				= (long) (int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                strides 			= buffer[bufferIndex++];
                reserved4 			= buffer[bufferIndex++];
                reserved5 			= (long)(int)((0x000000FF & (int)buffer[bufferIndex++]) | (int)(0x000000FF & (int)buffer[bufferIndex++])<< 8);
                crc 				= buffer[bufferIndex++];
                etx 				= buffer[bufferIndex];
            } catch (Exception e) {
                /*
                     * An exception should only happen if the buffer is too short and we walk off the end of the bytes,
                     * because of the way we read the bytes from the device this should never happen, but just in case
                     * we'll catch the exception
                     */
                Log.d(TAG, "Failure building HrmReading from byte buffer, probably an incopmplete or corrupted buffer");
            }


            Log.d(TAG, "Building HrmReading from byte buffer complete, consumed " + bufferIndex + " bytes in the process");

            /*
            * One simple check to see if we parsed the bytes properly is to check if the ETX
            * character was found where we expected it,  a more robust implementation would be
            * to calculate the CRC from the message contents and compare it to the CRC from
            * the packet.
            */
            if ( etx != ETX )
                Log.e(TAG,"...ETX mismatch!  The HxM message was not parsed properly");

            /*
            * log the contents of the HrmReading, use logcat to watch the data as it arrives
            */

            getPortByName("heartRate", MessagePort.class).process(heartRate);
            //  dump();
        }


        /*
        * Display the HRM reading into the layout
        */
        private void displayRaw() {  /*
            display	( R.id.stx,  stx );
            display ( R.id.msgId,  msgId );
            display ( R.id.dlc,  dlc );
            display ( R.id.firmwareId,   firmwareId );
            display ( R.id.firmwareVersion,   firmwareVersion );
            display ( R.id.hardwareId,   hardWareId );
            display ( R.id.hardwareVersion,   hardwareVersion );
            display ( R.id.batteryChargeIndicator,  (int)batteryIndicator );
            display ( R.id.heartRate, (int)heartRate );
            display ( R.id.heartBeatNumber,  (int)heartBeatNumber );
            display ( R.id.hbTimestamp1,   hbTime1 );
            display ( R.id.hbTimestamp2,   hbTime2 );
            display ( R.id.hbTimestamp3,   hbTime3 );
            display ( R.id.hbTimestamp4,   hbTime4 );
            display ( R.id.hbTimestamp5,   hbTime5 );
            display ( R.id.hbTimestamp6,   hbTime6 );
            display ( R.id.hbTimestamp7,   hbTime7 );
            display ( R.id.hbTimestamp8,   hbTime8 );
            display ( R.id.hbTimestamp9,   hbTime9 );
            display ( R.id.hbTimestamp10,   hbTime10 );
            display ( R.id.hbTimestamp11,   hbTime11 );
            display ( R.id.hbTimestamp12,   hbTime12 );
            display ( R.id.hbTimestamp13,   hbTime13 );
            display ( R.id.hbTimestamp14,   hbTime14 );
            display ( R.id.hbTimestamp15,   hbTime15 );
            display ( R.id.reserved1,   reserved1 );
            display ( R.id.reserved2,   reserved2 );
            display ( R.id.reserved3,   reserved3 );
            display ( R.id.distance,   distance );
            display ( R.id.speed,   speed );
            display ( R.id.strides,  (int)strides );
            display ( R.id.reserved4,  reserved4 );
            display ( R.id.reserved5,  reserved5 );
            display ( R.id.crc,  crc );
            display ( R.id.etx,  etx );   */

        }



        /*
        * dump() sends the contents of the HrmReading object to the log, use 'logcat' to view
        */
        public void dump() {
            Log.d(TAG,"HrmReading Dump");
            Log.d(TAG,"...serial "+ ( serial ));
            Log.d(TAG,"...stx "+ ( stx ));
            Log.d(TAG,"...msgId "+( msgId ));
            Log.d(TAG,"...dlc "+ ( dlc ));
            Log.d(TAG,"...firmwareId "+ ( firmwareId ));
            Log.d(TAG,"...sfirmwareVersiontx "+ (  firmwareVersion ));
            Log.d(TAG,"...hardWareId "+ (  hardWareId ));
            Log.d(TAG,"...hardwareVersion "+ (  hardwareVersion ));
            Log.d(TAG,"...batteryIndicator "+ ( batteryIndicator ));
            Log.d(TAG,"...heartRate "+ ( heartRate ));



            Log.d(TAG, "...heartBeatNumber " + (heartBeatNumber));
            Log.d(TAG,"...shbTime1tx "+ (  hbTime1 ));
            Log.d(TAG,"...hbTime2 "+ (  hbTime2 ));
            Log.d(TAG,"...hbTime3 "+ (  hbTime3 ));
            Log.d(TAG,"...hbTime4 "+ (  hbTime4 ));
            Log.d(TAG,"...hbTime4 "+ (  hbTime5 ));
            Log.d(TAG,"...hbTime6 "+ (  hbTime6 ));
            Log.d(TAG,"...hbTime7 "+ (  hbTime7 ));
            Log.d(TAG,"...hbTime8 "+ (  hbTime8 ));
            Log.d(TAG,"...hbTime9 "+ (  hbTime9 ));
            Log.d(TAG,"...hbTime10 "+ (  hbTime10 ));
            Log.d(TAG,"...hbTime11 "+ (  hbTime11 ));
            Log.d(TAG,"...hbTime12 "+ (  hbTime12 ));
            Log.d(TAG,"...hbTime13 "+ (  hbTime13 ));
            Log.d(TAG,"...hbTime14 "+ (  hbTime14 ));
            Log.d(TAG,"...hbTime15 "+ (  hbTime15 ));
            Log.d(TAG,"...reserved1 "+ (  reserved1 ));
            Log.d(TAG,"...reserved2 "+ (  reserved2 ));
            Log.d(TAG,"...reserved3 "+ (  reserved3 ));
            Log.d(TAG,"...distance "+ (  distance ));
            Log.d(TAG,"...speed "+ (  speed ));
            Log.d(TAG,"...strides "+ ( strides ));
            Log.d(TAG,"...reserved4 "+ ( reserved4 ));
            Log.d(TAG,"...reserved5 "+ ( reserved5 ));
            Log.d(TAG,"...crc "+ ( crc ));
            Log.d(TAG,"...etx "+ ( etx ));
        }



        /****************************************************************************
         * Some utility functions to control the formatting of HxM fields into the
         * activity's view
         ****************************************************************************/


        /*
        * display a byte value
        */
        private void display  ( int nField, byte d ) {
            String INT_FORMAT = "%x";

            String s = String.format(INT_FORMAT, d);

            display( nField, s  );
        }

        /*
           * display an integer value
           */
        private void display  ( int nField, int d ) {
            String INT_FORMAT = "%d";

            String s = String.format(INT_FORMAT, d);

            display( nField, s  );
        }

        /*
           * display a long integer value
           */
        private void display  ( int nField, long d ) {
            String INT_FORMAT = "%d";

            String s = String.format(INT_FORMAT, d);

            display( nField, s  );
        }

        /*
           * display a character string
           */
        private void display ( int nField, CharSequence  str  ) {
            //  TextView tvw = (TextView) findViewById(nField);
            //  if ( tvw != null )
            //    tvw.setText(str);
        }
    }

}
