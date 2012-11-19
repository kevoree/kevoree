/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
package org.kevoree.platform.android.boot;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.kevoree.platform.android.boot.controller.ControllerImpl;
import org.kevoree.platform.android.core.KevoreeAndroidBootStrap;
import org.kevoree.platform.android.ui.KevoreeAndroidUIScreen;

import java.lang.reflect.Method;

/**
 * @author ffouquet
 */
public class KevoreeService extends Service {


    private String nodeName;

    private String groupName;

    private KevoreeAndroidBootStrap bootObj = null;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        nodeName =  intent.getExtras().getString("nodeName");
        groupName = intent.getExtras().getString("groupName");

        Log.i("KevoreeService onStartCommand",""+nodeName+"-"+groupName);
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.net.preferIPv4Addresses", "true");
        System.setProperty("java.net.preferIPv4Stack", "true");

        new Thread() {
            @Override
            public void run() {
                try
                {
                    ControllerImpl.initKCL(getBaseContext());
                    //Class bootClazz =  org.kevoree.platform.android.core.KevoreeAndroidBootStrap.class;//ControllerImpl.tkcl.getClusterKCL().loadClass("org.kevoree.platform.android.core.KevoreeAndroidBootStrap");
                    //bootObj = bootClazz.newInstance();
                    bootObj = new KevoreeAndroidBootStrap();
                    //Method startM = bootClazz.getMethod("start",Activity.class, android.content.Context.class, ClassLoader.class, KevoreeAndroidUIScreen.class, String.class);
                    bootObj.start(KevoreeActivity.controller.getViewManager().getCtx(),getBaseContext(), this.getClass().getClassLoader(),KevoreeActivity.controller,nodeName,groupName);

                   // startM.invoke(bootObj,KevoreeActivity.controller.getViewManager().getCtx(),getBaseContext(), bootClazz.getClassLoader(),KevoreeActivity.controller,nodeName);
                } catch (Exception e) {
                    Log.e("KevBoot","KevBoot",e);
                    e.printStackTrace();
                }

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

        try {
            bootObj.getClass().getMethod("stop").invoke(bootObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            Notification notification = new Notification(R.drawable.kicon, getString(R.string.app_name), System.currentTimeMillis());
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), getString(R.string.notification_description), contentIntent);
            Object[] startForegroundMethodArgs = new Object[]{ART2SERVICE_NOTIFICATION_ID, notification};
            try {
                startForegroundMethod.invoke(this, startForegroundMethodArgs);
                notificationManager.notify(ART2SERVICE_NOTIFICATION_ID, notification);
            } catch (Exception e) {
                // Should not happen.
                Log.e("kevoree.service.logger", "Unable to invoke startForeground", e);
            }
        } catch (NoSuchMethodException e) {
            // Running on an older platform -> Fall back on the old API.
            //setForeground(true);
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
                Log.e("kevoree.service.logger", "Unable to invoke stopForeground", e);
            }
            notificationManager.cancel(ART2SERVICE_NOTIFICATION_ID);
        } catch (NoSuchMethodException e) {
            // Running on an older platform -> Fall back on the old API.
            //setForeground(false);
        }
    }

}
