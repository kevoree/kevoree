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

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected URLHandlerScala handler = new URLHandlerScala();

    public String getLastParam(String url){
        Option<String> result = handler.getLastParam(url,this.getDictionary().get("urlpattern").toString());
        if(result.isDefined()){
            return result.get();
        } else {
            return null;
        }
    }
    
    @Start
    public void startPage() {
        handler.initRegex(this.getDictionary().get("urlpattern").toString());
        logger.debug("Abstract page start2");
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

    @Port(name = "request")
    public void requestHandler(Object param) {
        KevoreeHttpRequest request = resolveRequest(param);
        if (request != null) {
            KevoreeHttpResponse response = buildResponse(request);
            response = process(request,response);
            this.getPortByName("content", MessagePort.class).process(response);//SEND MESSAGE
        }
    }


    public KevoreeHttpResponse process(KevoreeHttpRequest request,KevoreeHttpResponse response){
        //TO OVERRIDE
        return response;
    }

}
