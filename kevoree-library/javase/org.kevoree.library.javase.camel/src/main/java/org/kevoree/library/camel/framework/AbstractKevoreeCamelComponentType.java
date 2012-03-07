package org.kevoree.library.camel.framework;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 11:50
 */

@Library(name = "JavaSE")
@ComponentFragment
public abstract class AbstractKevoreeCamelComponentType extends AbstractComponentType {

    private CamelContext context = null;

    public CamelContext getContext() {
        return context;
    }

    public CamelContext buildCamelContext(){
        return new DefaultCamelContext();
    }

    @Start
    public void start() throws Exception {
        context = buildCamelContext();
        context.setClassResolver(new ClassLoaderClassResolver(this.getClass().getClassLoader()));
        KevoreePortComponent cc = new KevoreePortComponent(this);
        context.addComponent("kport",cc);
        RouteBuilder rb = new RouteBuilder() {
            public void configure() {
                buildRoutes(this);
            }
        };
        context.addRoutes(rb);
        context.start();
    }

    @Stop
    public void stop() throws Exception {
        if (context != null) {
            context.stop();
        }
        context = null;
    }

    @Update
    public void update() throws Exception {
        stop();
        start();
    }

    protected abstract void buildRoutes(RouteBuilder rb);

    @Port(name = "*")
    public void globalInput(Object o){
        System.out.println("WTF "+o);
    }

}
