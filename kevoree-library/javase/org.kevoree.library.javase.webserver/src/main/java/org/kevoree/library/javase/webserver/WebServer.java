package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

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
        @DictionaryAttribute(name = "port")
})
public class WebServer extends AbstractComponentType {

    ServerBootstrap bootstrap = null;

    @Start
    public void start() {
        bootstrap = new ServerBootstrap();
        bootstrap.startServer(Integer.parseInt(this.getDictionary().get("port").toString()));
    }

    @Stop
    public void stop() {
        if(bootstrap != null){
            bootstrap.stop();
        }
    }

    @Update
    public void update() {
        stop();
        start();
    }


}
