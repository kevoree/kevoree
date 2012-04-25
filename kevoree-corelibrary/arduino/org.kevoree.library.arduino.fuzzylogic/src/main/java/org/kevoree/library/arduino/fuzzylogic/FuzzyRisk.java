package org.kevoree.library.arduino.fuzzylogic;

import org.kevoree.annotation.*;
import org.kevoree.tools.arduino.framework.datatypes.IntList4;
import org.kevoree.tools.arduino.framework.fuzzylogic.AbstractFuzzyLogicArduinoComponent;
import org.kevoree.tools.arduino.framework.fuzzylogic.FuzzyRulesContext;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 20/02/12
 * Time: 17:11
 */


@Library(name = "Arduino")
@ComponentType
@Provides({
        @ProvidedPort(name = "triggerAlarme", type = PortType.MESSAGE),
        @ProvidedPort(name = "triggerPrealarme", type = PortType.MESSAGE)

})

@Requires({
        @RequiredPort(name = "led", type = PortType.MESSAGE, optional = true) ,
        @RequiredPort(name = "buzzer", type = PortType.MESSAGE, optional = true)

})


@DictionaryType({

        @DictionaryAttribute(name = "triggerAlarme_low", defaultValue = "0;0;20;20",dataType = IntList4.class),
        @DictionaryAttribute(name = "triggerAlarme_intermediate", defaultValue = "19;19;50;50",dataType = IntList4.class),
        @DictionaryAttribute(name = "risk_medium", defaultValue = "40;40;70;70",dataType = IntList4.class),
        @DictionaryAttribute(name = "risk_high", defaultValue = "60;60;100;100",dataType = IntList4.class),


        @DictionaryAttribute(name = "led_none",defaultValue = "0" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "led_low",defaultValue = "25" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "led_medium",defaultValue = "50" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "led_high",defaultValue = "100" , dataType = java.lang.Integer.class),

        @DictionaryAttribute(name = "buzzer_none",defaultValue = "0" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "buzzer_medium",defaultValue = "50" , dataType = java.lang.Integer.class),
        @DictionaryAttribute(name = "buzzer_high",defaultValue = "100" , dataType = java.lang.Integer.class)
})


public class FuzzyRisk  extends AbstractFuzzyLogicArduinoComponent  {


    @Override
    public void declareRules(FuzzyRulesContext rules)
    {
        rules.addRule("IF risk IS low THEN led IS none AND buzzer IS none;");
        rules.addRule("IF risk IS medium THEN led IS low AND buzzer IS none;");
        rules.addRule("IF risk IS intermediate THEN led IS medium AND buzzer IS none;");
        rules.addRule("IF risk IS medium THEN led IS medium AND buzzer IS medium;");
        rules.addRule("IF risk IS high THEN led IS high AND buzzer IS high;");
    }
}
