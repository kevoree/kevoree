package org.kevoree.api;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 28/11/2013
 * Time: 08:10
 */
public interface ChannelContext extends Context {

    Set<Port> getLocalInputs();
    Set<Port> getRemoteInputs();

    Set<Port> getLocalOutputs();
    Set<Port> getRemoteOutputs();

    Set<Port> getInputs();
    Set<Port> getOutputs();
}
