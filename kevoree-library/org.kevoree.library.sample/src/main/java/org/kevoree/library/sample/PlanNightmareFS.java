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
@Requires({
    @RequiredPort(name = "r1", type = PortType.MESSAGE),
    @RequiredPort(name = "r2", type = PortType.MESSAGE)
})
@Library(name = "Kevoree-Samples")
@ComponentType
public class PlanNightmareFS extends AbstractComponentType {

    @Start
    public void startMethod() {
        Bundle bundle = (Bundle) getDictionary().get("osgi.bundle");
        String name = bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME).toString();
        getPortByName("r1", MessagePort.class).process("hello from "+name);
        getPortByName("r2", MessagePort.class).process("hello from "+name);
    }

    @Stop
    public void stopMethod() {
        Bundle bundle = (Bundle) getDictionary().get("osgi.bundle");
        String name = bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME).toString();
        getPortByName("r1", MessagePort.class).process("bye from "+name);
        getPortByName("r2", MessagePort.class).process("bye from "+name);
    }
    
}
