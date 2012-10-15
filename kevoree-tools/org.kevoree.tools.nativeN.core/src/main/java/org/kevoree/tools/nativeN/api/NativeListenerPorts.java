package org.kevoree.tools.nativeN.api;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/08/12
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */
public interface NativeListenerPorts extends java.util.EventListener
{
      public void disptach(NativeEventPort event,String port_name,String msg);
}
