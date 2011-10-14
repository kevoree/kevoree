package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.None;
import scala.Option;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:52
 * To change this template use File | Settings | File Templates.
 */


@Library(name = "JavaSE")
@ComponentFragment
@Provides({
        @ProvidedPort(name = "request", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "content", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "urlpattern")
})
public class AbstractPage extends AbstractComponentType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private URLHandlerScala handler = new URLHandlerScala();
    
        public KevoreeHttpResponse requestResolve(Object param) {
           logger.debug("KevoreeHttpRequest handler triggered");
           Option<KevoreeHttpRequest> parseResult = handler.check(param);
           if(parseResult.isDefined()){
               KevoreeHttpRequest requestKevoree = parseResult.get();
               KevoreeHttpResponse responseKevoree = new KevoreeHttpResponse();
               responseKevoree.setTokenID(requestKevoree.getTokenID());
               return responseKevoree;
           } else {
               return null;
           }
        }
    
}
