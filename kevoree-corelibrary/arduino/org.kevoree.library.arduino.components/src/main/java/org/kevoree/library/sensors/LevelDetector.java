package org.kevoree.library.sensors;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.AbstractArduinoComponent;
import org.kevoree.tools.arduino.framework.ArduinoGenerator;

@Library(name = "Arduino")
@ComponentType
@Requires({
        @RequiredPort(name = "low", type = PortType.MESSAGE, needCheckDependency = false),
        @RequiredPort(name = "high", type = PortType.MESSAGE, needCheckDependency = false)
})
@Provides({
        @ProvidedPort(name = "input", type = PortType.MESSAGE)
})
@DictionaryType({
        @DictionaryAttribute(name = "level", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "avgnb", dataType = java.lang.Integer.class)
})
public class LevelDetector extends AbstractArduinoComponent {

    @Override
    public void generateClassHeader(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("int count;");
        getGenerator().appendNativeStatement("boolean inf;");
        getGenerator().appendNativeStatement("boolean sup;");
    }

    @Override
    public void generateInit(ArduinoGenerator gen) {
        getGenerator().appendNativeStatement("count = 0;");
        getGenerator().appendNativeStatement("inf = false;");
        getGenerator().appendNativeStatement("sup = false;");
    }

    @Port(name = "input")
    public void triggerInput(Object gen) {

        getGenerator().appendNativeStatement("if(atoi(msg->value) > level){");

        getGenerator().appendNativeStatement("inf=false;");
        getGenerator().appendNativeStatement("if(sup){count++;}else{count=0;sup=true;}");
        getGenerator().appendNativeStatement("if(count == avgnb){");
        getGenerator().declareStaticKMessage("msg", "event");
        getGenerator().appendNativeStatement("msg->value = \"sup\";");
        getGenerator().sendKMessage("msg", "high");
        getGenerator().freeStaticKMessage("msg");
        getGenerator().appendNativeStatement("}");

        getGenerator().appendNativeStatement("} else {");

        getGenerator().appendNativeStatement("sup=false;");
        getGenerator().appendNativeStatement("if(inf){count++;}else{count=0;inf=true;}");
        getGenerator().appendNativeStatement("if(count == avgnb){");
        getGenerator().declareStaticKMessage("msg", "event");
        getGenerator().appendNativeStatement("msg->value = \"inf\";");
        getGenerator().sendKMessage("msg", "low");
        getGenerator().freeStaticKMessage("msg");
        getGenerator().appendNativeStatement("}");

        getGenerator().appendNativeStatement("}");

    }
}
