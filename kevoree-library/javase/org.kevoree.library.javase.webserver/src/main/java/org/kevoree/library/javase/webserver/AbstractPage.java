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
        @DictionaryAttribute(name = "urlpattern",optional = true, defaultValue = "/")
})
public class AbstractPage extends AbstractComponentType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private URLHandlerScala handler = new URLHandlerScala();

    @Start
    public void startPage() {
        handler.initRegex(this.getDictionary().get("urlpattern").toString());
    }

    @Stop
    public void stopPage() {
       //NOOP
    }

    @Update
    public void updatePage() {
        handler.initRegex(this.getDictionary().get("urlpattern").toString());
    }

    public KevoreeHttpRequest resolveRequest(Object param) {
        logger.debug("KevoreeHttpRequest handler triggered");
        Option<KevoreeHttpRequest> parseResult = handler.check(param);
        if (parseResult.isDefined()) {
            KevoreeHttpRequest requestKevoree = parseResult.get();
            return requestKevoree;
        } else {
            return null;
        }
    }

    public KevoreeHttpResponse buildResponse(KevoreeHttpRequest request){
          KevoreeHttpResponse responseKevoree = new KevoreeHttpResponse();
          responseKevoree.setTokenID(request.getTokenID());
          return responseKevoree;
    }

}
