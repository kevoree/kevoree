/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.experiment.smartbuilding;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 *
 * @author ffouquet
 */
@Library(name = "SmartBuilding")
@ComponentType
@Provides({
    @ProvidedPort(name = "sdata", type = PortType.MESSAGE),
    @ProvidedPort(name = "synch", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "forward", type = PortType.MESSAGE)
})
public class EmergencyLog extends AbstractComponentType {

    @Start
    @Stop
    public void lifeCycle() {
    }

    @Ports({
        @Port(name = "sdata"),@Port(name = "synch")
    })
    public void trigger(Object o) {
    }
}
