package org.kevoree.library.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.jndi.JndiContext;
import org.kevoree.annotation.ComponentType;
import org.kevoree.library.camel.framework.AbstractKevoreeCamelComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 12:08
 */

@ComponentType
public class CamelBeanTimer extends AbstractKevoreeCamelComponentType {

    /* Create a custom context to add new EndPoint to Camel */

    @Override
    public CamelContext buildCamelContext() {
        JndiContext jcontext = null;
        try {
            jcontext = new JndiContext();
            jcontext.bind("hello" + this.getName(), new BeanHello());
            CamelContext context = new DefaultCamelContext(jcontext);
            return context;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DefaultCamelContext();
    }

    protected void buildRoutes(RouteBuilder rb) {
        rb.from("timer://" + getName() + "?fixedRate=true&period=2000").to("bean:hello"+getName()).to("log:" + getName());
    }

}