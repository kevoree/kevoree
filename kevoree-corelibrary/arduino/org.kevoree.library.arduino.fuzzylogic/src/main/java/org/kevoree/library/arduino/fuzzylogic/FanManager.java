package org.kevoree.library.arduino.fuzzylogic;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.datatypes.IntList4;
import org.kevoree.tools.arduino.framework.fuzzylogic.AbstractFuzzyLogicArduinoComponent;
import org.kevoree.tools.arduino.framework.fuzzylogic.FuzzyRulesContext;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 19/01/12
 * Time: 11:04
 */

@Library(name = "Arduino")
@ComponentType

@Provides({
        @ProvidedPort(name = "temp", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "fan", type = PortType.MESSAGE, needCheckDependency = false, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "temp_cold", defaultValue = "0;0;200;200",dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_warm", defaultValue = "100;100;800;800",dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_hot", defaultValue = "512;512;1024;1024",dataType = IntList4.class),

        @DictionaryAttribute(name = "fan_stop",defaultValue = "0", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "fan_slow" ,defaultValue = "30", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "fan_fast",defaultValue = "100", dataType = java.lang.Integer.class)
})
public class FanManager extends AbstractFuzzyLogicArduinoComponent {

    @Override
    public void declareRules(FuzzyRulesContext rulesContext)
    {
        rulesContext.addRule("IF temp IS cold THEN fan IS stop;");
        rulesContext.addRule("IF temp IS warm THEN fan IS slow;");
        rulesContext.addRule("IF temp IS cold AND temp IS hot THEN fan IS slow;");
        rulesContext.addRule("IF temp IS hot THEN fan IS fast;");
    }

}
