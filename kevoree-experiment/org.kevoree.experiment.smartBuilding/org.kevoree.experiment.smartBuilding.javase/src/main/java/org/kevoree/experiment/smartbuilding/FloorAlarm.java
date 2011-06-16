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
    @ProvidedPort(name = "sdata", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "forward", type = PortType.MESSAGE)
})
public class FloorAlarm extends AbstractComponentType {

    @Start
    @Stop
    public void lifeCycle() {
    }

    @Ports({
        @Port(name = "sdata")
    })
    public void trigger(Object o) {
    }
}
