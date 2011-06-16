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
        @ProvidedPort(name = "on", type = PortType.MESSAGE),
        @ProvidedPort(name = "off", type = PortType.MESSAGE)
})
public class Light extends AbstractComponentType {

    @Start
    @Stop
    public void lifeCycle() {
    }
    
     @Ports({
        @Port(name="on"),@Port(name="off")
    })   
    public void trigger(Object o){
        
    }
     
}
