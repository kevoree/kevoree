/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.platform.osgi.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author ffouquet
 */
public class KevoreeService extends Service {

    private KevoreeAndroidBootstrap kebBoot = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Launch the OSGi framework.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Addresses", "true");
        System.setProperty("java.net.preferIPv4Stack", "true");
        new Thread() {
            @Override
            public void run() {
                kebBoot = new KevoreeAndroidBootstrap();
                File sdDir = Environment.getExternalStorageDirectory();
                File kevoree_cache = new File(sdDir.getAbsolutePath() + "/KEVOREE");
                Log.i("kevoree.android", kevoree_cache.getAbsolutePath());
                if (!kevoree_cache.exists()) {
                    if (!kevoree_cache.mkdirs()) {
                        Log.e("kevoree.M2", "unable to create cache");
                        throw new IllegalStateException("Unable to create kevoree maven repo cache dir");
                    } else {
                        Log.i("kevoree.M2", "cache created");
                    }
                }
                System.setProperty("user.home", kevoree_cache.getAbsolutePath());
                kebBoot.start(getBaseContext(), getClassLoader());

                //File m_cache = new File(sdDir.getAbsolutePath() + "/" + FELIX_CACHE_DIR);
                //System.setProperty("osgi.cache",m_cache.getAbsolutePath());

                //Log.i("kevoree.android", m_cache.getAbsolutePath());
/*
                if (!m_cache.exists()) {
                    if (!m_cache.mkdirs()) {
                        Log.e("kevoree.felix", "unable to create cache");
                        throw new IllegalStateException("Unable to create kevoree osgi cache dir");
                    } else {
                        Log.i("kevoree.felix", "cache created");
                    }
                } else {
                
                    //m_cache.delete();
                    //m_cache.mkdirs();
                    Log.i("kevoree.felix", "cache already exist");
                }
                */


                /*
                try {
                    // Activate WiFi multicast
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    multicastLock = wifiManager.createMulticastLock("ART2-Multicast-Lock");
                    multicastLock.acquire();
                } catch (Exception e) {
                    Log.e("art2.osgi.service.logger", "Exception when creating the multicast lock", e);
                }*/


            }
        }.start();
        //Set the service as foreground, so that the Android OS doesn't kill it
        setServiceAsForeground();
    }

    /* STOP & DESTROY SERVICE */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("kevoree.service.logger", "Stopping the Kevoree framework...");
        kebBoot.stop();
        //multicastLock.release();
        //Unset the service as foreground
        unsetServiceAsForeground();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private static final int ART2SERVICE_NOTIFICATION_ID = 1;

    /**
     * This is a wrapper around the new startForeground method from Android 2.0+,
     * using the older APIs if it is not available.
     */
    private void setServiceAsForeground() {
        // If we have the new startForeground API, then use it.
        try {
            Class[] startForegroundMethodSignature = new Class[]{int.class, Notification.class};
            Method startForegroundMethod = getClass().getMethod("startForeground", startForegroundMethodSignature);

            // Prepare arguments for the method
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(R.drawable.icon, getString(R.string.app_name), System.currentTimeMillis());
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.notification_description), contentIntent);
            Object[] startForegroundMethodArgs = new Object[]{Integer.valueOf(ART2SERVICE_NOTIFICATION_ID), notification};
            try {
                startForegroundMethod.invoke(this, startForegroundMethodArgs);
                notificationManager.notify(ART2SERVICE_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                // Should not happen.
                Log.e("art2.osgi.service.logger", "Unable to invoke startForeground", e);
            }
        } catch (NoSuchMethodException e) {
            // Running on an older platform -> Fall back on the old API.
            setForeground(true);
        }
    }

    /**
     * This is a wrapper around the new stopForeground method from Android 2.0+,
     * using the older APIs if it is not available.
     */
    private void unsetServiceAsForeground() {
        // If we have the new stopForeground API, then use it.
        try {
            Class[] stopForegroundMethodSignature = new Class[]{boolean.class};
            Method stopForegroundMethod = getClass().getMethod("stopForeground", stopForegroundMethodSignature);

            // Prepare arguments for the method
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Object[] stopForegroundMethodArgs = new Object[]{Boolean.TRUE};
            try {
                stopForegroundMethod.invoke(this, stopForegroundMethodArgs);
            } catch (Exception e) {
                // Should not happen.
                Log.e("art2.osgi.service.logger", "Unable to invoke stopForeground", e);
            }
            notificationManager.cancel(ART2SERVICE_NOTIFICATION_ID);
        } catch (NoSuchMethodException e) {
            // Running on an older platform -> Fall back on the old API.
            setForeground(false);
        }
    }

}
