package org.kevoree.api;

import org.kevoree.Channel;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 19/11/2013
 * Time: 00:17
 */
public interface Port {

    void send(String payload);

    /**
     * Deprecation note: callback are about to be dropped completely because they
     * mislead developers when using a component architecture
     *
     * @param payload message to send
     * @param callback DEPRECATED won't be used
     */
    @Deprecated
    void send(String payload, Callback callback);
    
    String getPath();

    Set<Channel> getChannels();
}
