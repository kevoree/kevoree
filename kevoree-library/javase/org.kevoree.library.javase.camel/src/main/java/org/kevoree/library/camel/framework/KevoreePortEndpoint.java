package org.kevoree.library.camel.framework;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.timer.TimerConsumer;
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

    private KevoreePortComponent origin = null;

    public KevoreePortEndpoint(AbstractComponentType ct, String portName,KevoreePortComponent origin) {
        this.componentType = ct;
        this.portName = portName;
        this.origin = origin;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new DefaultProducer(this) {
            @Override
            public void process(Exchange exchange) throws Exception {
                if (componentType.isPortBinded(portName)) {
                    MessagePort mport = componentType.getPortByName(portName, MessagePort.class);
                    if (mport != null) {
                        mport.process(exchange.getIn().getBody());
                    }
                }
            }
        };
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        KevoreePortConsumer newc = new KevoreePortConsumer(this, processor);
        origin.consumerInput.put(portName, newc);
        return newc;
    }

    @Override
    protected String createEndpointUri() {
        return "kport:" + portName;
    }

}
