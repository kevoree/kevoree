package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:58
 */
@Library(name = "JavaSE")
@ComponentFragment
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
public abstract class AbstractWebServer extends AbstractComponentType {

    @Start
    abstract public void start();

    @Stop
    abstract public void stop();

    @Update
    abstract public void update();

    @Port(name = "response")
    abstract public void responseHandler(Object param);


}
