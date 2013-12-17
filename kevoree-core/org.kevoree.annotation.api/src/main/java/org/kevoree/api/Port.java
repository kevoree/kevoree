package org.kevoree.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/11/2013
 * Time: 00:17
 */
public interface Port {

    public void call(Object payload, Callback callback);

    public void send(Object payload);

    public String getPath();

}
