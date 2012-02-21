package org.kevoree.library.arduino.fuzzylogic;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.datatypes.IntList4;
import org.kevoree.tools.arduino.framework.fuzzylogic.AbstractFuzzyLogicArduinoComponent;
import org.kevoree.tools.arduino.framework.fuzzylogic.FuzzyRulesContext;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 20/02/12
 * Time: 16:54
 */

@Library(name = "Arduino")
@ComponentType
@Provides({
        @ProvidedPort(name = "roll", type = PortType.MESSAGE),
        @ProvidedPort(name = "pitch", type = PortType.MESSAGE),
        @ProvidedPort(name = "yaw", type = PortType.MESSAGE)
})

@Requires({
        @RequiredPort(name = "motion", type = PortType.MESSAGE, needCheckDependency = false, optional = true)
})

@DictionaryType({

        @DictionaryAttribute(name = "roll_light", defaultValue = "-20;-20;20;20",dataType = IntList4.class),
        @DictionaryAttribute(name = "roll_strong", defaultValue = "10;10;60;60",dataType = IntList4.class),

        @DictionaryAttribute(name = "pitch_light", defaultValue = "-10;-10;10;10",dataType = IntList4.class),
        @DictionaryAttribute(name = "pitch_strong", defaultValue = "10;10;60;60",dataType = IntList4.class),

        @DictionaryAttribute(name = "motion_light",defaultValue = "0", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "motion_medium",defaultValue = "25", dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "motion_strong",defaultValue = "100", dataType = java.lang.Integer.class)

})

public class FuzzyIMU extends AbstractFuzzyLogicArduinoComponent {

    @Override
    public void declareRules(FuzzyRulesContext rules)
    {
        rules.addRule("IF roll IS light AND pitch IS light THEN motion IS light;");
        rules.addRule("IF roll IS strong AND pitch IS light THEN motion IS medium;");
        rules.addRule("IF pitch IS strong AND roll IS light THEN motion IS medium;");
        rules.addRule("IF pitch IS strong AND roll IS light THEN motion IS medium;");
        rules.addRule("IF pitch IS strong AND roll IS strong THEN motion IS strong;");

    }
}
