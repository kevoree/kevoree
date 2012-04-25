package org.kevoree.library.camel.atom;

import org.apache.abdera.model.Entry;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Entry entry = exchange.getIn().getBody(Entry.class);
                        String msg = entry.getUpdated()+":"+entry.getAuthor().getName()+"@"+entry.getTitle();
                        exchange.getOut().setBody(msg);
                    }
                })
        .to("kport:feeds");
    }

}
