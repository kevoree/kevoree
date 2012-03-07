package org.kevoree.library.camel;

import org.apache.camel.builder.RouteBuilder;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.camel.framework.AbstractKevoreeCamelComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/03/12
 * Time: 12:00
 */
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "period", defaultValue = "5000", optional = true)
})
public class CamelTimer extends AbstractKevoreeCamelComponentType {

    protected void buildRoutes(RouteBuilder rb){
        rb.from("timer://" + getName() + "?fixedRate=true&period="+getDictionary().get("period")).to("log:" + getName());
    }

}
