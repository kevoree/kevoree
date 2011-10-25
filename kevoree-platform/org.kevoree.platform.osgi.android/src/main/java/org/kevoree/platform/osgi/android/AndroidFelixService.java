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
import android.content.res.Resources;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.kevoree.ContainerRoot;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.framework.KevoreeXmiHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author ffouquet
 */
public class AndroidFelixService extends Service {

    //protected MulticastLock multicastLock;
    protected static Framework felixFramework;
    public static final String FELIX_BASE = "OSGI";
    public static final String FELIX_CACHE_DIR = "OSGI/cache";
    private static final int ART2SERVICE_NOTIFICATION_ID = 1;
    private static final String ANDROID_FRAMEWORK_PACKAGES = ("org.osgi.framework; version=1.5.0,"
            + "org.osgi.service.packageadmin; version=1.2.0,"
            + "org.osgi.framework.launch; version=1.5.0,"
            + "org.osgi.service.startlevel; version=1.0.0,"
            + "org.osgi.service.url; version=1.0.0,"
            + "org.osgi.util.tracker,"
            + "android; "
            + "android.app;"
            + "android.content;"
            + "android.database;"
            + "android.database.sqlite;"
            + "android.graphics; "
            + "android.graphics.drawable; "
            + "android.graphics.glutils; "
            + "android.hardware; "
            + "android.location; "
            + "android.media; "
            + "android.net; "
            + "android.opengl; "
            + "android.os; "
            + "android.provider; "
            + "android.sax; "
            + "android.speech.recognition; "
            + "android.telephony; "
            + "android.telephony.gsm; "
            + "android.text; "
            + "android.text.method; "
            + "android.text.style; "
            + "android.text.util; "
            + "android.speech; "
            + "android.speech.tts; "
            + "android.util; "
            + "android.view; "
            + "android.view.animation; "
            + "android.webkit; "
            + "android.widget; "
            + "com.google.android.maps; "
            + "com.google.android.xmppService; "
            + "javax.crypto; "
            + "javax.crypto.interfaces; "
            + "javax.crypto.spec; "
            + "javax.microedition.khronos.opengles; "
            + "javax.net; "
            + "javax.net.ssl; "
            + "javax.security.auth; "
            + "javax.security.auth.callback; "
            + "javax.security.auth.login; "
            + "javax.security.auth.x500; "
            + "javax.security.cert; "
            + "javax.sound.midi; "
            + "javax.sound.midi.spi; "
            + "javax.sound.sampled; "
            + "javax.sound.sampled.spi; "
            + "javax.sql; "
            + "javax.xml; "
            + "javax.xml.datatype; "
            + "javax.xml.namespace; "
            + "javax.xml.parsers; "
            + "javax.xml.transform; "
            + "javax.xml.transform.dom; "
            + "javax.xml.transform.sax; "
            + "javax.xml.transform.stream; "
            + "javax.xml.validation; "
            + "javax.xml.xpath; "
            + "junit.extensions; "
            + "junit.framework; "
            + "org.apache.commons.logging; "
            + "org.apache.commons.codec; "
            + "org.apache.commons.codec.binary; "
            + "org.apache.commons.codec.language; "
            + "org.apache.commons.codec.net; "
            + "org.apache.commons.httpclient; "
            + "org.apache.commons.httpclient.auth; "
            + "org.apache.commons.httpclient.cookie; "
            + "org.apache.commons.httpclient.methods; "
            + "org.apache.commons.httpclient.methods.multipart; "
            + "org.apache.commons.httpclient.params; "
            + "org.apache.commons.httpclient.protocol; "
            + "org.apache.commons.httpclient.util; "
            + "org.bluez; "
            + "org.json; "
            + "org.w3c.dom; "
            + "org.xml.sax; "
            + "org.xml.sax.ext; "
            + "org.xml.sax.helpers; "
            + "org.kermeta.art2.platform.android; "
            + "scala.annotation.unchecked; "
            + "scala.reflect.generic; "
            + "scala.collection.interfaces; "
            + "scala.xml.persistent; "
            + "scala.actors.threadpool; "
            + "scala.concurrent; "
            + "scala.util.parsing.combinator.testing; "
            + "scala.annotation; "
            + "scala.concurrent.forkjoin; "
            + "scala.io; "
            + "scala.reflect; "
            + "scala.util.automata; "
            + "scala.ref; "
            + "scala.util.parsing.syntax; "
            + "scala.util.matching; "
            + "scala.util.parsing.combinator; "
            + "scala.compat; "
            + "scala.runtime; "
            + "scala.util.parsing.ast; "
            + "scala.util.parsing.json; "
            + "scala.util.logging; "
            + "scala.xml.dtd; "
            + "scala.util.parsing.combinator.token; "
            + "scala.util.parsing.input; "
            + "scala.testing; "
            + "scala.util.grammar; "
            + "scala.collection; "
            + "scala.collection.immutable; "
            + "scala.text; "
            + "scala.actors.scheduler; "
            + "scala; "
            + "scala.actors; "
            + "scala.util.continuations; "
            + "scala.xml.include; "
            + "scala.collection.script; "
            + "scala.math; "
            + "scala.util; "
            + "scala.util.control; "
            + "scala.actors.threadpool.helpers; "
            + "scala.xml.parsing; "
            + "scala.annotation.target; "
            + "scala.xml; "
            + "scala.actors.remote; "
            + "scala.collection.mutable; "
            + "scala.actors.threadpool.locks; "
            + "scala.util.regexp; "
            + "scala.xml.include.sax; "
            + "scala.xml.factory; "
            + "scala.xml.transform; "
            + "scala.util.parsing.combinator.lexical; "
            + "scala.util.parsing.combinator.syntactical; "
            + "scala.xml.pull; "
            + "scala.mobile; "
            + "scala.collection.generic; "
            + "org.kevoree.tools.android.framework.helper; "
            + "org.kevoree.tools.android.framework.service; ").intern();

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
        //System.setProperty("node.name", KevoreeActivity.nodeName);
        new Thread() {
            @Override
            public void run() {
                /* Redirect Output stream */
                PrintStream m_out = new PrintStream(new OutputStream() {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();

                    @Override
                    public void write(int oneByte) throws IOException {
                        output.write(oneByte);
                        if (oneByte == '\n') {
                            Log.i("kevoree.osgi.service.logger", new String(output.toByteArray()));
                            output = new ByteArrayOutputStream();
                        }
                    }
                });
                System.setErr(m_out);
                System.setOut(m_out);

                File sdDir = Environment.getExternalStorageDirectory();
                File m_cache = new File(sdDir.getAbsolutePath() + "/" + FELIX_CACHE_DIR);
                File kevoree_cache = new File(sdDir.getAbsolutePath() + "/KEVOREE");
                Log.i("kevoree.android", m_cache.getAbsolutePath());
                if (!m_cache.exists()) {
                    if (!m_cache.mkdirs()) {
                        Log.e("kevoree.felix", "unable to create cache");
                        throw new IllegalStateException("Unable to create cache dir");
                    } else {
                        Log.i("kevoree.felix", "cache created");
                    }
                } else {
                    m_cache.delete();
                    m_cache.mkdir();
                    Log.i("kevoree.felix", "cache already exist");
                }
                if (!kevoree_cache.exists()) {
                    if (!kevoree_cache.mkdirs()) {
                        Log.e("kevoree.M2", "unable to create cache");
                        throw new IllegalStateException("Unable to create cache dir");
                    } else {
                        Log.i("kevoree.M2", "cache created");
                    }
                }
                System.setProperty("user.home",kevoree_cache.getAbsolutePath());


                /*
                try {
                    // Activate WiFi multicast
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    multicastLock = wifiManager.createMulticastLock("ART2-Multicast-Lock");
                    multicastLock.acquire();
                } catch (Exception e) {
                    Log.e("art2.osgi.service.logger", "Exception when creating the multicast lock", e);
                }*/

                // Launch the Felix OSGi framework
                HashMap<String, Object> configMap = new HashMap<String, Object>();
                configMap.put(FelixConstants.LOG_LEVEL_PROP, String.valueOf(Logger.LOG_DEBUG));
                configMap.put(org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES, generated.SysPackageConstants.getProperty());//ANDROID_FRAMEWORK_PACKAGES);
                configMap.put(org.osgi.framework.Constants.FRAMEWORK_STORAGE, m_cache.getAbsolutePath());
                configMap.put(org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN, org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

                //InputStream baseModel = getResources().openRawResource(R.raw.basemodel);
                //ContainerRoot baseModelLoaded = KevoreeXmiHelper.loadStream(baseModel);



             //   NodeTypeBootStrapModel.checkAndCreate(baseModelLoaded,KevoreeActivity.nodeName,"AndroidNode",new Properties());




                configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, EmbeddedActivators.getActivators());

                try {
                    //   logger.info("Starting the OSGi framework");
                    long initial_time = System.currentTimeMillis();
                    // Create an instance of the framework.
                    FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
                    felixFramework = factory.newFramework(configMap);
                    // Initialize the framework, but don't start it yet.
                    felixFramework.init();
                    // Use the system bundle context to process the auto-deploy and auto-install/auto-start properties.
                    //AutoProcessor.process(configProps, felixFramework.getBundleContext());
                    // Start the framework.
                    //AutoProcessor.process(configMap, felixFramework.getBundleContext());
                    felixFramework.start();

                    //TODO UI IDSCONNECT GESTURE

                    System.out.println("KevoreeActivity=" + KevoreeActivity.last);

                    felixFramework.getBundleContext().registerService(KevoreeAndroidService.class.getName(), KevoreeActivity.last, new Properties());

                    /*
            startRawBundle(felixFramework.getBundleContext(), "file://paxurl.jar", R.raw.paxurl, true);
            startRawBundle(felixFramework.getBundleContext(), "file://paxassembly.jar", R.raw.paxassembly, true);
            startRawBundle(felixFramework.getBundleContext(), "file://shell.jar", R.raw.shell, true);
            //startRawBundle(context,"file://shelltui.jar", R.raw.shelltui);
            startRawBundle(felixFramework.getBundleContext(), "file://shellremote.jar", R.raw.shellremote, true);
            startRawBundle(felixFramework.getBundleContext(), "file://osgi_compendium.jar", R.raw.osgi_compendium, true);
            startRawBundle(felixFramework.getBundleContext(), "file://slf4jandroid.jar", R.raw.slf4jandroid, true);
                    */
                    String defaultBundlePath = sdDir.getAbsolutePath() + "/" + FELIX_BASE + "/bundle/";

                    /*
                    try {
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "emf.lib-2.6.0.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.model-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.adaptation.model-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.api-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.framework-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.kompare-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.framework.bus.netty-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.deploy.osgi-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.core-2.2.1-SNAPSHOT.jar", true);
                        startDefaultBundle(felixFramework.getBundleContext(), defaultBundlePath + "art2.framework.bus.jmdns-2.2.1-SNAPSHOT.jar", true);
                    } catch (Exception e) {
                        Log.e("art2.osgi.service.logger", "Error deploying base ART2 bundles", e);
                    }   */


                    Log.i("kevoree.android.osgi.service.logger", "OSGi framework started in: " + (System.currentTimeMillis() - initial_time) / 1000 + " seconds");

                    // Save the framework as a system property
                    System.getProperties().put(Constants.getArt2FrameworkProperty(), felixFramework);

                    // Register the service activity
                    Hashtable<String, String> properties = new Hashtable<String, String>();
                    properties.put("platform", "android");
                    felixFramework.getBundleContext().registerService(Context.class.getName(), AndroidFelixService.this, properties);
                } catch (Throwable t) {
                    Log.e("", "The OSGi framework could not be started", t);
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
        Log.i("art2.osgi.service.logger", "Stopping the OSGi framework...");
        try {
            //Stop the framework
            felixFramework.stop();
        } catch (BundleException e) {
            Log.e("art2.osgi.service.logger", "Exception when stopping the OSGi framework", e);
        }

        //multicastLock.release();
        //Unset the service as foreground
        unsetServiceAsForeground();

        android.os.Process.killProcess(android.os.Process.myPid());

    }

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


    /*
    private Bundle startRawBundle(BundleContext context, String name, Integer id, Boolean start) {
        InputStream is = getResources().openRawResource(id);
        Bundle bundle = null;
        try {
            bundle = context.installBundle(name, is);
            if (start) {
                bundle.start();
            }
        } catch (BundleException ex) {
            Log.e("art2.bootstrap", ex.getMessage(), ex);
        }
        return bundle;
    }


    private Bundle startDefaultBundle(BundleContext context, String fileName, Boolean start) {
        Bundle bundle = null;
        try {
            bundle = context.installBundle("file:" + fileName);
            if (start) {
                bundle.start();
            }
        } catch (BundleException ex) {
            Log.e("art2.bootstrap", ex.getMessage(), ex);
        }
        return bundle;
    }  */

    public static BundleContext getBundleContext() {
        if (felixFramework == null) {
            return null;
        }
        return felixFramework.getBundleContext();
    }
}
