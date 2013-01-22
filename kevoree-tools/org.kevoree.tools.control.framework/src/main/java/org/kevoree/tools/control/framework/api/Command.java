package org.kevoree.tools.control.framework.api;

import org.kevoree.adaptation.control.api.ControlException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 22/01/13
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public interface Command {

    public void execute() throws ControlException;
}
