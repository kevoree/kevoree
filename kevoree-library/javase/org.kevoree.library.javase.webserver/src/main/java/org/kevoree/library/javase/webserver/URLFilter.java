package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import scala.Option;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:40
 * To change this template use File | Settings | File Templates.
 */

@Library(name = "JavaSE")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "urlpattern")
})
@Provides({
    @ProvidedPort(name = "request", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "filtered", type = PortType.MESSAGE)
})
public class URLFilter extends AbstractComponentType {

    URLHandlerScala handler = null;
    
    @Start
    public void startHandler() {
        handler = new URLHandlerScala();
        handler.initRegex(this.getDictionary().get("urlpattern").toString());
    }

    @Stop
    public void stopHandler() {
        handler = null;
    }

    @Update
    public void updateHandler() {
        stopHandler();
        startHandler();
    }
    
    @Port(name = "request")
    public void requestHandler(Object param){
        if(handler != null){
            Option<KevoreeHttpRequest> filtered = handler.check(param);
            if(filtered.isDefined()){
                this.getPortByName("filtered", MessagePort.class).process(filtered.get());
            }
        }
    }

}
