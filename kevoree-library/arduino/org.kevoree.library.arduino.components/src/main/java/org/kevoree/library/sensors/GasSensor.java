package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractPeriodicArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 02/03/12
 * Time: 15:42
 */

@Library(name = "Arduino")
@ComponentType
@Requires({
        @RequiredPort(name = "temp", type = PortType.MESSAGE, needCheckDependency = false)
})
public class GasSensor extends AbstractPeriodicArduinoComponent  {


    @Override
        public void generateClassHeader(ArduinoGenerator gen) {
            gen.appendNativeStatement("char buf[10];");
        }



    @Override
    public void generatePeriodic(ArduinoGenerator arduinoGenerator)
    {

        getGenerator().appendNativeStatement("kmessage * smsg = (kmessage*) malloc(sizeof(kmessage));");
        getGenerator().appendNativeStatement("if (smsg){memset(smsg, 0, sizeof(kmessage));}");
        getGenerator().appendNativeStatement("sprintf(buf,\"%d\",int(celcius));\n");
        getGenerator().appendNativeStatement("smsg->value = buf;\n");
        getGenerator().appendNativeStatement("smsg->metric=\"c\";");
        getGenerator().appendNativeStatement("temp_rport(smsg);");
        getGenerator().appendNativeStatement("free(smsg);");
    }
}
