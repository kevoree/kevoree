package org.kevoree.library.javase.airserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 22/02/12
 * Time: 22:13
 */
@Library(name = "JavaSE")
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "5000", optional = true)
})
@ComponentType
public class AirServer extends AbstractComponentType {

    AirReceiver server = new AirReceiver();

    @Start
    public void start() throws Exception {
        int port = Integer.parseInt(this.getDictionary().get("port").toString());
        server.startAirServer(port);
    }

    @Stop
    public void stop(){
        server.onShutdown();
    }

}
