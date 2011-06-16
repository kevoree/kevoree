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
    @ProvidedPort(name = "datas", type = PortType.MESSAGE)
})
public class BuildingTempMap extends AbstractComponentType {

    @Start
    @Stop
    public void lifeCycle() {
    }

    @Ports({
        @Port(name = "datas")
    })
    public void trigger(Object o) {
    }
}
