package org.kevoree.library.javase.jmdns;

import org.kevoree.ContainerRoot;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/02/13
 * Time: 10:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface JmDNSListener {
    /**
     * this function is used to notify when a new node has been discovered
     * @param remoteNodeName the name of the new node
     */
    void notifyNewSubNode(String remoteNodeName);

    /**
     * This function is used to update the current configuration according to the given model
     * We let the type which implements this interface to decide how to update the current configuration according to the model.
     * @param model the model which can be used to update the current configuration
     * @return true, if the update is done, false else
     */
    boolean updateModel(ContainerRoot model);
}
