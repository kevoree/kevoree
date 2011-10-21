package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.net.InetSocketAddress;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:58
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "JavaSE")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "port" , defaultValue = "8080")
})
@Requires({
        @RequiredPort(name = "handler", type = PortType.MESSAGE)
})
@Provides({
        @ProvidedPort(name = "response", type = PortType.MESSAGE)
})
public class WebServer extends AbstractComponentType {

    ServerBootstrap bootstrap = null;

    @Start
    public void start() {
        bootstrap = new ServerBootstrap(this.getPortByName("handler", MessagePort.class),this);
        bootstrap.startServer(Integer.parseInt(this.getDictionary().get("port").toString()));
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
