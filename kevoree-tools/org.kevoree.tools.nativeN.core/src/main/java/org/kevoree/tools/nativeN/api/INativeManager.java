package org.kevoree.tools.nativeN.api;


import org.kevoree.tools.nativeN.NativeHandlerException;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 15/10/12
 * Time: 10:55
 * To change this template use File | Settings | File Templates.
 */
public interface INativeManager {

    public boolean start() throws NativeHandlerException;
    public boolean stop() throws NativeHandlerException;
    public boolean update();
    public boolean push(String port_name, String data);

    public void addEventListener (NativeListenerPorts listener);
    public void removeEventListener (NativeListenerPorts listener);
}
