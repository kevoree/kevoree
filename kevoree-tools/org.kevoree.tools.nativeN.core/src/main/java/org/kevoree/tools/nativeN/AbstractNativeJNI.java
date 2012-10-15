package org.kevoree.tools.nativeN;

import org.kevoree.tools.nativeN.api.INativeJNI;

import java.util.EventObject;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 15/10/12
 * Time: 11:02
 * To change this template use File | Settings | File Templates.
 */
public class AbstractNativeJNI extends EventObject implements INativeJNI
{
    public AbstractNativeJNI(Object o) {
        super(o);
    }
}
