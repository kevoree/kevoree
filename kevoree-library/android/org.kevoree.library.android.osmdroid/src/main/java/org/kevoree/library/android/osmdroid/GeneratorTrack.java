package org.kevoree.library.android.osmdroid;

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

@Library(name = "Android")
@ComponentType

@Requires({
        @RequiredPort(name = "generatedtrack", type = PortType.MESSAGE,optional = true)
})

public class GeneratorTrack  extends AbstractComponentType implements  Runnable{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean  alive = true;
    private TracK track = new TracK();

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

    }


    @Override
    public void run() {
        int count = 0;
        GpsPoint pt;
        while(alive)
        {
            if(count > 10)
            {
                getPortByName("generatedtrack", MessagePort.class).process(track);
                count =0;
                track.clear();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                  //ignore
                }
            }
            else
            {
                pt = new GpsPoint();
                logger.info("Generate "+pt);
                track.addPoint(pt);
                count ++;
            }

        }
    }
}
