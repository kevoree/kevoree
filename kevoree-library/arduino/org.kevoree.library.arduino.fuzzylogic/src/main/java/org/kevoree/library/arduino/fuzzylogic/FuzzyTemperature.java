package org.kevoree.library.arduino.fuzzylogic;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.datatypes.IntList4;
import org.kevoree.tools.arduino.framework.fuzzylogic.AbstractFuzzyLogicArduinoComponent;
import org.kevoree.tools.arduino.framework.fuzzylogic.FuzzyRulesContext;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 20/02/12
 * Time: 16:11
 */

@Library(name = "Arduino")
@ComponentType

@Provides({
        @ProvidedPort(name = "temp", type = PortType.MESSAGE)
})

@Requires({
        @RequiredPort(name = "peril", type = PortType.MESSAGE, needCheckDependency = false, optional = true)
})
@DictionaryType({

        @DictionaryAttribute(name = "temp_frozen", defaultValue = "-30;-30;0;0",dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_cold", defaultValue = "-1;-1;15;15",dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_warm", defaultValue = "10;10;25;25",dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_hot", defaultValue = "20;20;100;100",dataType = IntList4.class),

        @DictionaryAttribute(name = "peril_low",defaultValue = "0", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "peril_medium" ,defaultValue = "50", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "peril_high",defaultValue = "100", dataType = java.lang.Integer.class)

})
public class FuzzyTemperature extends AbstractFuzzyLogicArduinoComponent {


    @Override
    public void declareRules(FuzzyRulesContext rules) {

        rules.addRule("IF temp IS frozen THEN peril IS high;");
        rules.addRule("IF temp IS hot THEN peril IS high;");
        rules.addRule("IF temp IS cold AND temp IS warn THEN peril IS medium;");
        rules.addRule("IF temp IS cold AND temp IS frozen warn THEN peril IS high;");
        rules.addRule("IF temp IS warn AND temp IS hot warn THEN peril IS high;");
        rules.addRule("IF temp IS cold THEN peril IS medium;");
        rules.addRule("IF temp IS warn THEN peril IS low;");
    }
}
