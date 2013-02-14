package org.kevoree.library.javase.jmdns;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/02/13
 * Time: 10:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface JmDNSListener {
    void notifyNewSubNode(String remoteNodeName);
}
