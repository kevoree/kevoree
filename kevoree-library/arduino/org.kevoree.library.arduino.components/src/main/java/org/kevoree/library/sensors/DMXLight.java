package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "pin", defaultValue = "0", optional = true)
})
@Provides({
        @ProvidedPort(name = "intensity", type = PortType.MESSAGE),
        @ProvidedPort(name = "color", type = PortType.MESSAGE)
})
public class DMXLight extends AbstractArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.addLibrary("DmxSimple.h",this.getClass().getClassLoader().getResourceAsStream("DmxSimple/DmxSimple.h"));
        gen.addLibrary("DmxSimple.cpp",this.getClass().getClassLoader().getResourceAsStream("DmxSimple/DmxSimple.cpp"));
        gen.appendNativeStatement("#include <DmxSimple.h>");
    }

    @Override
    public void generateInit(ArduinoGenerator gen) {
        gen.appendNativeStatement("DmxSimple.usePin(atoi(pin));");
        gen.appendNativeStatement("DmxSimple.maxChannel(4);");
        gen.appendNativeStatement("DmxSimple.write(2,255);");
        gen.appendNativeStatement("DmxSimple.write(3,255);");
        gen.appendNativeStatement("DmxSimple.write(4,255);");
        gen.appendNativeStatement("DmxSimple.write(1,127);");
    }

    @Port(name = "intensity")
    public void triggerOn(Object o) {
        getGenerator().appendNativeStatement("int value = atoi(msg->value);");
        getGenerator().appendNativeStatement("int outputIntensity = (127 * value) / 100 ;");
        getGenerator().appendNativeStatement("DmxSimple.write(1,outputIntensity);");
    }

    @Port(name = "color")
    public void triggerColor(Object o) {
      //  getGenerator().appendNativeStatement("DmxSimple.write(1,int(param));");
    }

}
