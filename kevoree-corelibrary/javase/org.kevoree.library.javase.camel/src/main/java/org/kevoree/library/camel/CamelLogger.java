package org.kevoree.library.camel;

import org.apache.camel.builder.RouteBuilder;
import org.kevoree.annotation.*;
import org.kevoree.library.camel.framework.AbstractKevoreeCamelComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 15:34
 */
@ComponentType
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
public class CamelLogger extends AbstractKevoreeCamelComponentType {

    protected void buildRoutes(RouteBuilder rb) {
        rb.from("kport:input").to("log:" + getName());
    }

}
