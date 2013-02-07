package org.kevoree.library.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 10/05/12
 * Time: 15:25
 */
@Library(name = "JavaSE")
@ChannelTypeFragment
public class CamelNettyService extends CamelNetty {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Random random = new Random();

    @Override
    protected void buildRoutes(RouteBuilder routeBuilder) {
        routeBuilder.from("kchannel:input")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        if (getBindedPorts().isEmpty() && getOtherFragments().isEmpty()) {
                            logger.debug("No consumer, msg lost=" + exchange.getIn().getBody());
                        } else {
                            // default behavior is round robin
                            int rang = random.nextInt(getBindedPorts().size() + getOtherFragments().size());
                            Message message = (Message) exchange.getIn().getBody();
                            if (rang < getBindedPorts().size()) {
                                logger.debug("select rang: {} for channel {}", new Object[]{rang, CamelNettyService.this.getName()});
                                logger.debug("send message to {}", getBindedPorts().get(rang).getComponentName());
                                Object result = forward(getBindedPorts().get(rang), message);
                                // forward the result
                                exchange.getOut().setBody(result);
                            } else {
                                rang = rang - getBindedPorts().size();
                                logger.debug("select rang: {} for channel {}", new Object[]{rang, CamelNettyService.this.getName()});
                                logger.debug("send message on {}", getOtherFragments().get(rang).getNodeName());
                                Object result = getContext().createProducerTemplate().requestBody("netty:tcp://" + getAddress(getOtherFragments().get(rang).getNodeName()) + ":" + parsePortNumber(getOtherFragments().get(rang).getNodeName()), message);
                                // forward the result
                                exchange.getOut().setBody(result);

                            }
                        }
                    }
                }
                );
        try {
            routeBuilder.from("netty:tcp://0.0.0.0:" + /*parsePortNumber(getNodeName())*/port + "?sync=true").
                    process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            // default behavior is round robin
                            int rang = random.nextInt(getBindedPorts().size());
                            logger.debug("select rang: {} for channel {}", new Object[]{rang, CamelNettyService.this.getName()});
                            logger.debug("send message to {}", getBindedPorts().get(rang).getComponentName());
                            Object result = forward(getBindedPorts().get(rang), (Message) exchange.getIn().getBody());
                            // forward result
                            exchange.getOut().setBody(result);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
