package org.kevoree.library.arduino.fuzzylogic;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.datatypes.IntList4;
import org.kevoree.tools.arduino.framework.fuzzylogic.AbstractFuzzyLogicArduinoComponent;
import org.kevoree.tools.arduino.framework.fuzzylogic.FuzzyRulesContext;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 02/03/12
 * Time: 10:19
 */
@Library(name = "Arduino")
@ComponentType
@Provides({
        @ProvidedPort(name = "temp", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "i", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "r", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "g", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "b", type = PortType.MESSAGE, needCheckDependency = false, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "temp_cold", defaultValue = "-10;-10;10;10", dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_medium", defaultValue = "10;10;25;25", dataType = IntList4.class),
        @DictionaryAttribute(name = "temp_hot", defaultValue = "25;25;100;100", dataType = IntList4.class),

        @DictionaryAttribute(name = "r_l",defaultValue = "20" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "r_h",defaultValue = "100" , dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "g_l",defaultValue = "20", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "g_h",defaultValue = "100", dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "b_l",defaultValue = "20", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "b_h",defaultValue = "100", dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "i_l",defaultValue = "20", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "i_h",defaultValue = "100", dataType = java.lang.Integer.class)
})

public class ThermometerManager extends AbstractFuzzyLogicArduinoComponent {

    @Override
    public void declareRules(FuzzyRulesContext rulesContext)
    {
        rulesContext.addRule("IF temp IS cold THEN r IS h AND g IS l AND b IS l AND i IS h;");

        rulesContext.addRule("IF temp IS medium THEN r IS l AND g IS h AND b IS l AND i IS h;");

        rulesContext.addRule("IF temp IS hot THEN r IS l AND g IS l AND b IS h AND i IS h;");


    }
}
