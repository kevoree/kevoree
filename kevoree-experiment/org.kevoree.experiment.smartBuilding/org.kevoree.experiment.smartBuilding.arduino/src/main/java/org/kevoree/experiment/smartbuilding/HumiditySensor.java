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
@Requires({
        @RequiredPort(name = "value", type = PortType.MESSAGE)
})
public class HumiditySensor extends AbstractComponentType {

    @Start
    @Stop
    public void lifeCycle() {
    }
     
}
