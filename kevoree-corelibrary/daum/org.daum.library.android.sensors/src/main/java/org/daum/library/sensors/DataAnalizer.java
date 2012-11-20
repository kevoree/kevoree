package org.daum.library.sensors;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import org.daum.common.genmodel.Agent;
import org.daum.common.genmodel.Position;
import org.daum.common.genmodel.SMS;
import org.daum.common.genmodel.SitacFactory;
import org.daum.common.genmodel.impl.AgentImpl;
import org.daum.common.genmodel.impl.DatedValueImpl;
import org.daum.common.genmodel.impl.GpsPointImpl;
import org.daum.library.ormH.persistence.PersistenceConfiguration;
import org.daum.library.ormH.persistence.PersistenceSession;
import org.daum.library.ormH.persistence.PersistenceSessionFactoryImpl;
import org.daum.library.ormH.store.ReplicaStore;
import org.daum.library.ormH.utils.PersistenceException;
import org.daum.library.replica.cache.ReplicaService;
import org.daum.library.replica.listener.ChangeListener;
import org.kevoree.ContainerRoot;
import org.kevoree.android.framework.helper.UIServiceHandler;
import org.kevoree.android.framework.service.KevoreeAndroidService;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 27/09/12
 * Time: 16:34
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "Android")
@Provides({
        @ProvidedPort(name = "step", type = PortType.MESSAGE),
        @ProvidedPort(name = "position",type = PortType.MESSAGE) ,
        @ProvidedPort(name = "notify", type = PortType.MESSAGE),
        @ProvidedPort(name = "motion", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "speech", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "vibreur", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "alert", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "service", type = PortType.SERVICE, className = ReplicaService.class, optional = true),
        @RequiredPort(name = "distance", type = PortType.MESSAGE,optional = true)  ,
        @RequiredPort(name = "x", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "y", type = PortType.MESSAGE,optional = true),
        @RequiredPort(name = "z", type = PortType.MESSAGE,optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "idAgent", defaultValue = "jedartois"),
        @DictionaryAttribute(name = "phoneNumber", defaultValue = ""),
        @DictionaryAttribute(name = "mStepLength", defaultValue = "40"),
        @DictionaryAttribute(name = "preAlertTimer", defaultValue = "18") ,
        @DictionaryAttribute(name = "alertTimer", defaultValue = "32") ,
        @DictionaryAttribute(name = "volume", defaultValue = "100")
})
@ComponentType
public class DataAnalizer  extends AbstractComponentType {

    private LRUMap<Date, GpsPointImpl> position = new LRUMap<Date, GpsPointImpl>(10);
    private KevoreeAndroidService uiService = null;
    private float mDistance = 0;
    private float  mStepLength = 100;
    private float lastDistanceP =0;
    private float lastDistanceA =0;
    private Timer t;
    private Timer t2;
    private boolean  alert = false;
    private AudioManager amanager;
    private static ChangeListener singleton=null;
    private static final String TAG = "DataAnalizer";
    public PersistenceConfiguration configuration = null;
    private PersistenceSessionFactoryImpl factory = null;
    private boolean first=true;

    public static ChangeListener getChangeListenerInstance() {
        if (singleton == null) singleton = new ChangeListener();
        return singleton;
    }

    @Start
    public void start()
    {
        uiService = UIServiceHandler.getUIService();        //create the TTS instance
        mStepLength = Float.parseFloat(getDictionary().get("mStepLength").toString());

        amanager = (AudioManager) uiService.getRootActivity().getSystemService(Context.AUDIO_SERVICE);

        amanager.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(getDictionary().get("volume").toString()) , AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        amanager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Integer.parseInt(getDictionary().get("volume").toString()), AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        amanager.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(getDictionary().get("volume").toString()), AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);


        getModelService().registerModelListener(new ModelListener() {
            @Override
            public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                return true;
            }

            @Override
            public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                return true;
            }

            @Override
            public boolean afterLocalUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                return true;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void modelUpdated() {
                if(first){
                    first = false;
                    uiService.getRootActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                configuration = new PersistenceConfiguration(getNodeName());
                                for (Class c : SitacFactory.classes()) configuration.addPersistentClass(c);

                                ReplicaService replicatingService = getPortByName("service", ReplicaService.class);
                                ReplicaStore store = new ReplicaStore(replicatingService);

                                configuration.setStore(store);
                                factory = configuration.getPersistenceSessionFactory();


                                getPortByName("speech", MessagePort.class).process("The system is started");

                                t = new Timer();
                                t2 = new Timer();

                                t.schedule(new preAlert(),4000, (Integer.parseInt(getDictionary().get("preAlertTimer").toString()) * 1000));
                                t2.schedule(new AlertDetection(),5000,(Integer.parseInt(getDictionary().get("alertTimer").toString()) * 1000));



                            } catch (PersistenceException e) {
                                Log.e(TAG, "Error on component startup", e);
                            }
                        }


                    });    }
            }

            @Override
            public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });



    }

    @Stop
    public void stop(){

    }


    @Port(name = "notify")
    public void notifiedByReplica(final Object m) {
        uiService.getRootActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getChangeListenerInstance().receive(m);
            }
        });
    }


    class AlertDetection extends TimerTask {
        public void run()
        {


            if(mDistance <= lastDistanceP){
                if(alert)
                {
                    try
                    {
                        if(mDistance <= lastDistanceP)
                        {
                            Thread.sleep(5000);
                            getPortByName("speech", MessagePort.class).process("if you do still not move, an alert will be sent");
                            Thread.sleep(8000);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(mDistance <= lastDistanceP){

                        t.cancel();
                        t2.cancel();

                        String phonen = getDictionary().get("phoneNumber").toString();

                        System.out.println("Sending msg to "+phonen);
                        SMS t = new SMS();
                        t.setNumber(phonen);
                        String msg = "alert:"+getDictionary().get("idAgent").toString()+":position="+position.getLast();
                        t.setMsg(msg);

                        getPortByName("alert", MessagePort.class).process(t);

                        uiService.getRootActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        while(true)
                                        {
                                            getPortByName("vibreur", MessagePort.class).process("tick");
                                            getPortByName("speech", MessagePort.class).process("I need help !");

                                            if(position != null && position.getLast() != null){
                                                getPortByName("alert", MessagePort.class).process(position.getLast());
                                            }

                                            try
                                            {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                            }

                                        }

                                    }
                                }).start();

                            } });
                    }
                }
            }
            lastDistanceP =  mDistance;


        }

    }

    class preAlert extends TimerTask {
        public void run()
        {
            if(mDistance <= lastDistanceA){
                if(!alert)
                {
                    getPortByName("vibreur", MessagePort.class).process("tick");
                    getPortByName("speech", MessagePort.class).process("The system has detected that you have not moved ,      How are you ?");
                }
                alert  = true;
            } else
            {
                alert  = false;
            }
            lastDistanceA =  mDistance;
        }
    }



    @Update
    public void update(){
        mStepLength = Float.parseFloat(getDictionary().get("mStepLength").toString());
        if(t != null ){
            t.cancel();
            t.purge();
        }
        t = new Timer();
        t.schedule(new preAlert(),0, (Integer.parseInt(getDictionary().get("preAlertTimer").toString()) * 1000));

        if(t2 != null)  {
            t2.cancel();
            t2.purge();
        }

        t2 = new Timer();
        t2.schedule(new AlertDetection(),0, (Integer.parseInt(getDictionary().get("alertTimer").toString()) * 1000));


        if(amanager != null){
            amanager.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(getDictionary().get("volume").toString()) , AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
            amanager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, Integer.parseInt(getDictionary().get("volume").toString()), AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
            amanager.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(getDictionary().get("volume").toString()) , AudioManager.FLAG_SHOW_UI + AudioManager.FLAG_PLAY_SOUND);
        }
    }

    @Port(name = "step")
    public void x(Object o)
    {
        //getPortByName("speach", MessagePort.class).process("a step is detected");
        mDistance += (float)   mStepLength/100;
        System.out.println("distance "+mDistance);
        getPortByName("distance", MessagePort.class).process(mDistance);
    }




    @Port(name = "motion")
    public void motion(Object o)
    {
         if(o instanceof  org.daum.common.genmodel.Motion){

             org.daum.common.genmodel.Motion t = (org.daum.common.genmodel.Motion)o;

             getPortByName("x", MessagePort.class).process( t.getX());
             getPortByName("y", MessagePort.class).process( t.getY());
             getPortByName("z", MessagePort.class).process( t.getZ());
         }


    }
    @Port(name = "position")
    public void position(Object o)
    {
        if(o instanceof GpsPointImpl){
            position.put(new Date(),(GpsPointImpl)o);

            if(factory != null){
                PersistenceSession s = null;
                try {
                    s = factory.getSession();
                    Map<String, AgentImpl> agents = s.getAll(AgentImpl.class);
                    for (Agent agent : agents.values()) {
                        if (agent.getMatricule().equals(getDictionary().get("agent").toString()))
                        {
                            agent.setposRef((Position)o);
                            s.update(agent);
                        }
                    }

                } catch (PersistenceException e) {
                    Log.e(TAG, "PersistenceException ",e);
                }
            }
        }else {
            Log.e(TAG, "Factor is null");
        }
    }


}
