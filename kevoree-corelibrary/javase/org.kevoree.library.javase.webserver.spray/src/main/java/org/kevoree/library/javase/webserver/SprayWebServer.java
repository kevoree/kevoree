package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:58
 */
@Library(name = "JavaSE")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "port" , defaultValue = "8080"),
        @DictionaryAttribute(name = "timeout" , defaultValue = "5000", optional = true)
})
@Requires({
        @RequiredPort(name = "handler", type = PortType.MESSAGE)
})
@Provides({
        @ProvidedPort(name = "response", type = PortType.MESSAGE)
})
public class SprayWebServer extends AbstractComponentType {

    ServerBootstrap bootstrap = null;

    @Start
    public void start() {
        bootstrap = new ServerBootstrap(this.getPortByName("handler", MessagePort.class),this);
        bootstrap.startServer(Integer.parseInt(this.getDictionary().get("port").toString()),
                Long.parseLong(this.getDictionary().get("timeout").toString())
                );
    }

    @Stop
    public void stop() {
        if (bootstrap != null) {
            bootstrap.stop();
        }
    }

    @Update
    public void update() {
        stop();
        start();
    }

    @Port(name = "response")
    public void responseHandler(Object param) {
        if (bootstrap != null) {
            bootstrap.responseHandler(param);
        }
    }


}
