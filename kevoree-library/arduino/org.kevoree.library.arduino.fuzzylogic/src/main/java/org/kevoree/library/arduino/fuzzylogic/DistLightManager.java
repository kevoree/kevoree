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
        @DictionaryAttribute(name = "distance_near", defaultValue = "0;0;20;20\n"),
        @DictionaryAttribute(name = "distance_med", defaultValue = "10;10;100;100\n"),
        @DictionaryAttribute(name = "distance_far", defaultValue = "80;80;150;150\n"),

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
        rulesContext.addRule("IF distance IS near THEN red IS high AND green IS low AND blue IS low AND intensity IS high;");

        rulesContext.addRule("IF distance IS med THEN red IS low AND green IS high AND blue IS low AND intensity IS high;");

        rulesContext.addRule("IF distance IS far THEN red IS low AND green IS low AND blue IS high AND intensity IS high;");



    }

}
