package org.kevoree.tools.control.framework;

import org.kevoree.tools.control.framework.api.IAccessControl;
import org.kevoree.tools.control.framework.impl.AccessControlImpl;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
public class ControlFactory {
    public static IAccessControl createAccessControl(){
        return  new AccessControlImpl();
    }
}
