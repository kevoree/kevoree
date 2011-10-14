package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 09:23
 * To change this template use File | Settings | File Templates.
 */

@ComponentType
public class HelloWorldPage extends AbstractPage {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Start
    @Stop
    @Update
    public void handler() {
    }

    @Port(name = "request")
    public void requestHandler(Object param) {
        logger.debug("KevoreeHttpRequest handler triggered");
       if(param instanceof KevoreeHttpRequest){
           KevoreeHttpRequest requestKevoree = (KevoreeHttpRequest) param;
           KevoreeHttpResponse responseKevoree = new KevoreeHttpResponse();
           responseKevoree.setTokenID(requestKevoree.getTokenID());
           responseKevoree.setContent("<b>HelloWorld from Kevoree Cloud server !</b>");
           this.getPortByName("content",MessagePort.class).process(responseKevoree);
       }
    }

}
