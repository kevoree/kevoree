package org.kevoree.library.android.fakeDomo;

import android.util.Log;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 09/11/11
 * Time: 14:59
 * To change this template use File | Settings | File Templates.
 */


@DictionaryType(
        {
                @DictionaryAttribute(name="frequence",defaultValue="2000",optional=true),
                @DictionaryAttribute(name="range",defaultValue="100",optional=true)
        }
)
@ComponentType
@Requires({
        @RequiredPort(name = "out", type = PortType.MESSAGE,optional = true)
})
@GroupType
@Library(name = "Android")
public class Random extends AbstractComponentType implements  Runnable{

    Boolean alive=false;
    Thread t=null;

    @Start
    public void start() throws IOException {
        Log.i("Random ","starting");
        alive=true;
        t =new Thread(this);
        t.start();
    }

    @Stop
    public void stop() {
        Log.i("Random ","closing");
        alive = false;
    }


    @Override
    public void run() {
        while(alive)
        {
            java.util.Random rand = new java.util.Random();
            MessagePort port =   (MessagePort)     this.getPortByName("out");
            port.process(rand.nextInt(Integer.parseInt(getDictionary().get("range").toString())));
            try {
                Thread.sleep(Integer.parseInt(getDictionary().get("range").toString()));
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}