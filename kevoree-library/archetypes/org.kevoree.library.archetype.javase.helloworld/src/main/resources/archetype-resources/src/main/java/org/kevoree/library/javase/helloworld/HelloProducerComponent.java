package org.kevoree.library.javase.helloworld.${package};

import org.kevoree.annotation.*;
import org.kevoree.annotation.ComponentType;
import org.kevoree.framework.*;


/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 27/10/11
 * Time: 13:42
 * To change this template use File | Settings | File Templates.
 */


@Requires({
        @RequiredPort(name = "prod", type = PortType.MESSAGE, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "helloProductionDelay", defaultValue = "2000", optional = true)
})
@ComponentType
public class HelloProducerComponent extends AbstractComponentType implements HelloProductionListener {

    private HelloProducerThread producer;

    @Start
    public void startComponent() {
        if (producer == null || producer.isStopped()) {
            producer = new HelloProducerThread(Long.valueOf((String) getDictionary().get("helloProductionDelay")));
            producer.addHelloProductionListener(this);
            producer.start();
        }
    }

    @Stop
    public void stopComponent() {
        if (producer != null) {
            producer.halt();
        }
    }

    @Update
    public void updateComponent() {
        stopComponent();
        startComponent();
    }

    public void helloProduced(String helloValue) {
        MessagePort prodPort = getPortByName("prod",MessagePort.class);
        if(prodPort != null) {
            prodPort.process(helloValue);
        }
    }
}
