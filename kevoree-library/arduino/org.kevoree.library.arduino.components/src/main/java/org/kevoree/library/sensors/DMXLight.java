package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@DictionaryType({
        @DictionaryAttribute(name = "dpin", defaultValue = "0", optional = true, vals = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"})
})
@Provides({
        @ProvidedPort(name = "intensity", type = PortType.MESSAGE),
        @ProvidedPort(name = "red", type = PortType.MESSAGE),
        @ProvidedPort(name = "green", type = PortType.MESSAGE),
        @ProvidedPort(name = "blue", type = PortType.MESSAGE)
})
public class DMXLight extends AbstractArduinoComponent {

    @Override
    public void generateHeader(ArduinoGenerator gen) {
        gen.addLibrary("DmxSimple.h", this.getClass().getClassLoader().getResourceAsStream("DmxSimple/DmxSimple.h"));
        gen.addLibrary("DmxSimple.cpp", this.getClass().getClassLoader().getResourceAsStream("DmxSimple/DmxSimple.cpp"));
        gen.appendNativeStatement("#include <DmxSimple.h>");
    }

    @Override
    public void generateUpdatedParams(ArduinoGenerator gen) {
        gen.appendNativeStatement("DmxSimple.usePin(atoi(dpin));");
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

    @Port(name = "red")
    public void triggerRed(Object o) {
        getGenerator().appendNativeStatement("int value = atoi(msg->value);");
        getGenerator().appendNativeStatement("int outputIntensity = (255 * value) / 100 ;");
        getGenerator().appendNativeStatement("DmxSimple.write(2,outputIntensity);");
    }

    @Port(name = "green")
    public void triggerGreen(Object o) {
        getGenerator().appendNativeStatement("int value = atoi(msg->value);");
        getGenerator().appendNativeStatement("int outputIntensity = (255 * value) / 100 ;");
        getGenerator().appendNativeStatement("DmxSimple.write(3,outputIntensity);");
    }

    @Port(name = "blue")
    public void triggerBlue(Object o) {
        getGenerator().appendNativeStatement("int value = atoi(msg->value);");
        getGenerator().appendNativeStatement("int outputIntensity = (255 * value) / 100 ;");
        getGenerator().appendNativeStatement("DmxSimple.write(4,outputIntensity);");
    }

}
