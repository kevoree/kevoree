package org.kevoree.library.camel.framework;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.impl.ProcessorEndpoint;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 14:57
 */
public class KevoreePortEndpoint extends ProcessorEndpoint {

    private AbstractComponentType componentType = null;
    private String portName = null;
    
    public KevoreePortEndpoint(AbstractComponentType ct,String portName){
        this.componentType = ct;
        this.portName = portName;
    }
    
    @Override
    public Producer createProducer() throws Exception {
        return new DefaultProducer(this){
            @Override
            public void process(Exchange exchange) throws Exception {
                if(componentType.isPortBinded(portName)){
                    MessagePort mport = componentType.getPortByName(portName, MessagePort.class);
                    if(mport != null){
                        mport.process(exchange.getIn().getBody());
                    }
                }
            }
        };
    }

    @Override
    protected String createEndpointUri() {
        return "kport:" + portName;
    }

}
