package org.kevoree.api;

/**
 *
 * Created by leiko on 3/3/17.
 */
public interface Channel {

    void internalSend(Port port, String message, Callback callback);
}
