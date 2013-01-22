package org.kevoree.tools.control.framework.utils;

import org.kevoree.KControlModel.KControlModelFactory;
import org.kevoree.KControlModel.RuleMatcher;
import org.kevoree.kompare.JavaSePrimitive;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
public class HelperMatcher {



    public static RuleMatcher createMatcher(String pTypeQuery)
    {
        RuleMatcher m1 = KControlModelFactory.$instance.createRuleMatcher();
        m1.setPTypeQuery(JavaSePrimitive.StartInstance());
        return  m1;

    }
}
