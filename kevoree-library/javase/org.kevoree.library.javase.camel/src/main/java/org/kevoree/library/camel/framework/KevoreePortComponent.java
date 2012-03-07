package org.kevoree.library.camel.framework;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.kevoree.framework.AbstractComponentType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 14:45
 */
public class KevoreePortComponent extends DefaultComponent {

    AbstractComponentType c = null;
    public HashMap<String,KevoreePortConsumer> consumerInput = new HashMap<String,KevoreePortConsumer>();

    public KevoreePortComponent(AbstractComponentType ct){
        c = ct;
    }
    
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return new KevoreePortEndpoint(c,remaining,this);
    }
}
