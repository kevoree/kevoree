/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.sample;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.osgi.framework.Bundle;

/**
 *
 * @author ffouquet
 */
@Provides({
    @ProvidedPort(name = "p1", type = PortType.MESSAGE),
    @ProvidedPort(name = "p2", type = PortType.MESSAGE)
})
@Requires({
    @RequiredPort(name = "r1", type = PortType.MESSAGE, optional = false, noDependency = true),
    @RequiredPort(name = "r2", type = PortType.MESSAGE, optional = false, noDependency = true)
})
@Library(name = "Kevoree-Samples")
@ComponentType
public class PlanNightmarePipe extends AbstractComponentType {

    @Port(name = "p1")
    public void p1trigger(Object o) {
        if (isStarted) {
            System.out.println("rec " + name + " = " + o.toString());

            getPortByName("r1", MessagePort.class).process(o.toString() + "-" + name);
            getPortByName("r2", MessagePort.class).process(o.toString() + "-" + name);

        } else {
            System.out.println("Error, call on stop component");
        }
    }

    @Port(name = "p2")
    public void p2trigger(Object o) {
        if (isStarted) {
            System.out.println("rec " + name + " = " + o.toString());

            getPortByName("r1", MessagePort.class).process(o.toString() + "-" + name);
            getPortByName("r2", MessagePort.class).process(o.toString() + "-" + name);

        } else {
            System.out.println("Error, call on stop component");
        }
    }
    private Boolean isStarted = false;
    private String name = "defName";

    @Start
    public void startMethod() {
        isStarted = true;
        Bundle bundle = (Bundle) getDictionary().get("osgi.bundle");
        name = bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME).toString();
    }

    @Stop
    public void stopMethod() {
        isStarted = false;
    }
}
