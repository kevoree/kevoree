package org.kevoree.library.camel.atom;

import org.apache.camel.builder.RouteBuilder;
import org.kevoree.annotation.*;
import org.kevoree.library.camel.framework.AbstractKevoreeCamelComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 12:00
 */
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "period", defaultValue = "60000", optional = true),
        @DictionaryAttribute(name = "url")
})
@Requires({
        @RequiredPort(name = "feeds", type = PortType.MESSAGE, needCheckDependency = true, optional = true)
})
public class CamelAtomReader extends AbstractKevoreeCamelComponentType {

    protected void buildRoutes(RouteBuilder rb) {
        rb.from("atom://" + getDictionary().get("url") + "?consumer.delay=" + getDictionary().get("period"))
        .to("kport:feeds");
    }

}
