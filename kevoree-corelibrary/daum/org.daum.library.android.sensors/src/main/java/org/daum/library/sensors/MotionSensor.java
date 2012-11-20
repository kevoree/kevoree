package org.daum.library.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import org.daum.common.genmodel.GpsPoint;
import org.daum.common.genmodel.Motion;
import org.daum.common.genmodel.SitacFactory;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 26/09/12
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "Android")
@Requires({
        @RequiredPort(name = "motion", type = PortType.MESSAGE,optional = true) ,
        @RequiredPort(name = "step", type = PortType.MESSAGE,optional = true)
})

@DictionaryType({
        @DictionaryAttribute(name = "sensitivity", defaultValue = "6.66", optional = true,vals={"1.97","2.96","4.44","6.66","10.00","15.00","22.50","33.75","50.62"})
})
@ComponentType
public class MotionSensor  extends AbstractComponentType   {


    private SensorManager mSensorManager=null;
    private Sensor mSensor=null;
    private   SensorEventListener mSensorListener=null;

    private final static String TAG = "StepDetector";
    private float   mLimit = 15;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;

    public void setSensitivity(float sensitivity) {
        mLimit = sensitivity; // 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
    }

    @Start
    public void start()
    {

        mLimit = Float.parseFloat(getDictionary().get("sensitivity").toString());
        int h = 480; // TODO: remove this constant
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        mSensorManager = (SensorManager) UIServiceHandler.getUIService().getRootActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorListener = new SensorEventListener()
        {
            public void onSensorChanged(SensorEvent se)
            {
                Sensor sensor = se.sensor;
                synchronized (this) {
                    float x = se.values[0];
                    float y = se.values[1];
                    float z = se.values[2];
                    Motion motion  = new Motion();
                    motion.setX(x);
                    motion.setY(y);
                    motion.setZ(z);


                    getPortByName("motion", MessagePort.class).process(motion);


                    if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    }
                    else {
                        int j = (sensor.getType() == Sensor.TYPE_ACCELEROMETER) ? 1 : 0;
                        if (j == 1) {
                            float vSum = 0;
                            for (int i=0 ; i<3 ; i++) {
                                final float v = mYOffset + se.values[i] * mScale[j];
                                vSum += v;
                            }
                            int k = 0;
                            float v = vSum / 3;

                            float direction = (v > mLastValues[k] ? 1 : (v < mLastValues[k] ? -1 : 0));
                            if (direction == - mLastDirections[k]) {
                                // Direction changed
                                int extType = (direction > 0 ? 0 : 1); // minumum or maximum?
                                mLastExtremes[extType][k] = mLastValues[k];
                                float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                                if (diff > mLimit) {

                                    boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k]*2/3);
                                    boolean isPreviousLargeEnough = mLastDiff[k] > (diff/3);
                                    boolean isNotContra = (mLastMatch != 1 - extType);

                                    if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                        Log.i(TAG, "step");

                                        getPortByName("step", MessagePort.class).process("step");

                                        mLastMatch = extType;
                                    }
                                    else {
                                        mLastMatch = -1;
                                    }
                                }
                                mLastDiff[k] = diff;
                            }
                            mLastDirections[k] = direction;
                            mLastValues[k] = v;
                        }
                    }

                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }


        };
        mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);


    }
    @Stop
    public void stop()
    {
        if(mSensorListener != null && mSensorManager !=null)
            mSensorManager.unregisterListener(mSensorListener);
    }

    @Update
    public void update()
    {
        mLimit = Float.parseFloat(getDictionary().get("sensitivity").toString());
    }







}



