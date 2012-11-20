package org.daum.library.sensors;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import org.daum.common.genmodel.GpsPoint;
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

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 26/09/12
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "Android")
@Requires({
        @RequiredPort(name = "location", type = PortType.MESSAGE,optional = true)
})
@ComponentType
public class GpsComponent extends AbstractComponentType  implements  Runnable {


    private LocationManager locationMgr				= null;
    private LocationListener		onLocationChange=null;
    private Thread cthread = null;
    GpsPoint l1 = SitacFactory.createGpsPoint();

    @Start
    public void start()
    {
        cthread = new Thread(this);
        l1.setMode(-1);
        cthread.start();

        UIServiceHandler.getUIService().getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

		onLocationChange	= new LocationListener() {
            @Override
            public void onLocationChanged(Location location)
            {

                double lat =    location.getLatitude();
                double lon =   location.getLongitude();
                double  alt = location.getAltitude();

                l1.setLat((int)(lat* 1E6));
                l1.setLong((int) (lon * 1E6));
                l1.setAltitude((int)alt);
                l1.setMode(0);

                getPortByName("location", MessagePort.class).process(l1);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onProviderEnabled(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onProviderDisabled(String s) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }  ;


        locationMgr = (LocationManager) UIServiceHandler.getUIService().getRootActivity().getSystemService(Context.LOCATION_SERVICE);
        locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, onLocationChange);
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, onLocationChange);

            }
    } );


    }

    @Stop
    public void stop() {
        locationMgr.removeUpdates(onLocationChange);
    }

    @Update
    public void update() {

    }


    @Override
    public void run() {
      while (Thread.currentThread().isAlive()){

          try
          {
              if(l1.getMode() != -1 && l1.getLat() != 0 && l1.getLong() != 0){
                  getPortByName("location", MessagePort.class).process(l1);
              }


              Thread.sleep(2000);
          } catch (InterruptedException e) {
              e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }
      }
    }
}



