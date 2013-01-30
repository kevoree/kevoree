package org.kevoree.library.camel.http;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.framework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 29/01/13
 * Time: 18:32
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@Library(name = "JavaSE")
@ChannelTypeFragment
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "10000", optional = true, fragmentDependant = true)
})
// TODO define upper bounds to 1
public class CamelHTTPChannelService extends CamelHTTPChannelMessage {
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
                                logger.debug("select rang: {} for channel {}", new Object[]{rang, CamelHTTPChannelService.this.getName()});
                                logger.debug("send message to {}", getBindedPorts().get(rang).getComponentName());
                                Object result = forward(getBindedPorts().get(rang), message);
                                // forward the result
                                exchange.getOut().setBody(result);
                            } else {
                                rang = rang - getBindedPorts().size();
                                logger.debug("select rang: {} for channel {}", new Object[]{rang, CamelHTTPChannelService.this.getName()});
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
            routeBuilder.from("http://0.0.0.0:" + parsePortNumber(getNodeName())).
                    process(new Processor() {
                        public void process(Exchange exchange) throws Exception {
                            // default behavior is round robin
                            int rang = random.nextInt(getBindedPorts().size());
                            logger.debug("select rang: {} for channel {}", new Object[]{rang, CamelHTTPChannelService.this.getName()});
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

    public String getAddress(String remoteNodeName) {
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(this.getModelService().getLastModel(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            return ipOption.get();
        } else {
            return "127.0.0.1";
        }
    }

    public int parsePortNumber(String nodeName) {
        Option<String> portOption = KevoreePropertyHelper.getProperty(getModelElement(), "port", true, nodeName);
        int port = 9000;
        if (portOption.isDefined()) {
            try {
                port = Integer.parseInt(portOption.get());
            } catch (NumberFormatException e) {
                logger.warn("Attribute \"port\" of {} is not an Integer, Default value ({}) is returned", getName(), port);
            }
        } else {
            logger.info("Attribute \"port\" of {} is not set for {}, Default value ({}) is returned", new String[]{getName(), nodeName, port + ""});
        }
        return port;
    }

}
