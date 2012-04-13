package org.kevoree.library.android.GeneratorGps;

import org.kevoree.annotation.*;
import org.kevoree.common.gps.impl.GpsPoint;
import org.kevoree.common.gps.impl.TracK;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 12/04/12
 * Time: 15:22
 */
@Library(name = "Android", names = {"JavaSE"})
@ComponentType
@DictionaryType(
        {
                @DictionaryAttribute(name="period",defaultValue="2000",optional=true),
                @DictionaryAttribute(name="distance",defaultValue="100",optional=true)
        }
)
@Requires({
        @RequiredPort(name = "generatedtrack", type = PortType.MESSAGE,optional = true)
})

public class GeneratorTrack  extends AbstractComponentType implements  Runnable{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean  alive = true;
    private TracK track = new TracK();
    private int period = 1000;
    private int distance = 100;
    @Start
    public void start()
    {
        new Thread(this). start ();
    }


    @Stop
    public void stop() {
        alive  = false;
    }

    @Update
    public void update() {
        try {
            period =  Integer.parseInt(getDictionary().get("period").toString());
            distance =Integer.parseInt(getDictionary().get("distance").toString());
        }   catch (Exception e){
            logger.error("Update fail ",e);
        }
    }


    @Override
    public void run() {
        int count = 0;
        GpsPoint pt = new GpsPoint();
        pt.randomPoint(500);
        while(alive)
        {
            track.clear();
            track.generatePoints(pt,distance,1);
            getPortByName("generatedtrack", MessagePort.class).process(track);
            try {
                Thread.sleep(period);
            } catch (InterruptedException e) {
                //ignore
            }
        }
    }
}