package org.kevoree.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 27/11/2013
 * Time: 23:31
 */
public interface ChannelDispatch {

    public void dispatch(Object payload, Callback callback);

}
