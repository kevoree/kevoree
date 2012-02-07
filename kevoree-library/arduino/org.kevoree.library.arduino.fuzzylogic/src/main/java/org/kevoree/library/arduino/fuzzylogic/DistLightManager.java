package org.kevoree.library.arduino.fuzzylogic;

import org.kevoree.annotation.*;
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
        @ProvidedPort(name = "distance", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "intensity", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "red", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "green", type = PortType.MESSAGE, needCheckDependency = false, optional = true),
        @RequiredPort(name = "blue", type = PortType.MESSAGE, needCheckDependency = false, optional = true)
})
@DictionaryType({
        @DictionaryAttribute(name = "distance_near", defaultValue = "0;0;15;15"),
        @DictionaryAttribute(name = "distance_med", defaultValue = "20;20;60;60"),
        @DictionaryAttribute(name = "distance_far", defaultValue = "80;80;150;150"),

        @DictionaryAttribute(name = "red_low",defaultValue = "20"),
        @DictionaryAttribute(name = "red_high",defaultValue = "100"),
        @DictionaryAttribute(name = "green_low",defaultValue = "20"),
        @DictionaryAttribute(name = "green_high",defaultValue = "100"),
        @DictionaryAttribute(name = "blue_low",defaultValue = "20"),
        @DictionaryAttribute(name = "blue_high",defaultValue = "100"),
        @DictionaryAttribute(name = "intensity_low",defaultValue = "20"),
        @DictionaryAttribute(name = "intensity_high",defaultValue = "100")
})
public class DistLightManager extends AbstractFuzzyLogicArduinoComponent {

    @Override
    public void declareRules(FuzzyRulesContext rulesContext)
    {
        rulesContext.addRule("IF distance IS near THEN red IS high;");
            /*
        rulesContext.addRule("IF distance IS near THEN green IS low;");
        rulesContext.addRule("IF distance IS near THEN blue IS low;");
        rulesContext.addRule("IF distance IS near THEN intensity IS high;");

        rulesContext.addRule("IF distance IS med THEN red IS low;");
        rulesContext.addRule("IF distance IS med THEN green IS high;");
        rulesContext.addRule("IF distance IS med THEN blue IS low;");
        rulesContext.addRule("IF distance IS med THEN intensity IS high;");

        rulesContext.addRule("IF distance IS far THEN red IS low;");
        rulesContext.addRule("IF distance IS far THEN green IS low;");
        rulesContext.addRule("IF distance IS far THEN blue IS high;");
        rulesContext.addRule("IF distance IS far THEN intensity IS low;");
        */
    }

}
