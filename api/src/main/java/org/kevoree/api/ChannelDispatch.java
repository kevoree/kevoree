package org.kevoree.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 27/11/2013
 * Time: 23:31
 */
public interface ChannelDispatch {

    /**
     * This method will be called each time a message is send through an output port bound to this channel
     * @param payload the message to be dispatched by the channel
     * @param callback
     */
    void dispatch(String payload, Callback callback);
}
