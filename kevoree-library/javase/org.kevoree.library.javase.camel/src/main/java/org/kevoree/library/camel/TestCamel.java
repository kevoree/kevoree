package org.kevoree.library.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 10:25
 */
@ComponentType
public class TestCamel extends AbstractComponentType {

    private CamelContext context = null;


    @Start
    public void start() throws Exception {
        context = new DefaultCamelContext();
        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("timer://testCamel"+getName()).to("stream:out");
            }
        });
        context.start();
    }

    @Stop
    public void stop() throws Exception {
        context.stop();
        context = null;

    }

}
