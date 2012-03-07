package org.kevoree.library.camel.framework;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 17:57
 */
public class KevoreePortConsumer extends DefaultConsumer {

    public KevoreePortConsumer(Endpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    public void forwardMessage(Object msg) {

        Exchange exchange = getEndpoint().createExchange();
        exchange.getIn().setBody(msg);
        try {
            getProcessor().process(exchange);

            // log exception if an exception occurred and was not handled
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        } catch (Exception e) {
            getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
        }
    }

}
