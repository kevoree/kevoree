/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 *
 * @author ffouquet
 */
@Library(name = "Arduino")
@ComponentType
@Requires({
    @RequiredPort(name = "tick", type = PortType.MESSAGE, needCheckDependency = false)
})
public class Timer extends AbstractPeriodicArduinoComponent {

    @Override
    public void generatePeriodic(ArduinoGenerator gen) {
        gen.declareStaticKMessage("msg","t");
        gen.appendNativeStatement("msg->value = \"tick\";");
        gen.sendKMessage("msg","tick");
        gen.freeStaticKMessage("msg");
    }

}
