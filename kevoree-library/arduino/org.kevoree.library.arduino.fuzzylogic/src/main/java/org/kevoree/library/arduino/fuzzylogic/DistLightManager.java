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
        @ProvidedPort(name = "d", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "i", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "r", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "g", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "b", type = PortType.MESSAGE, needCheckDependency = false, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "d_n", defaultValue = "0;0;20;20", dataType = IntList4.class),
        @DictionaryAttribute(name = "d_m", defaultValue = "10;10;100;100", dataType = IntList4.class),
        @DictionaryAttribute(name = "d_f", defaultValue = "80;80;150;150", dataType = IntList4.class),

        @DictionaryAttribute(name = "r_l",defaultValue = "20" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "r_h",defaultValue = "100" , dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "g_l",defaultValue = "20", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "g_h",defaultValue = "100", dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "b_l",defaultValue = "20", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "b_h",defaultValue = "100", dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "i_l",defaultValue = "20", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "i_h",defaultValue = "100", dataType = java.lang.Integer.class)
})
public class DistLightManager extends AbstractFuzzyLogicArduinoComponent {

    @Override
    public void declareRules(FuzzyRulesContext rulesContext)
    {
        rulesContext.addRule("IF d IS n THEN r IS h AND g IS l AND b IS l AND i IS h;");

        rulesContext.addRule("IF d IS m THEN r IS l AND g IS h AND b IS l AND i IS h;");

        rulesContext.addRule("IF d IS f THEN r IS l AND g IS l AND b IS h AND i IS h;");


    }

}
