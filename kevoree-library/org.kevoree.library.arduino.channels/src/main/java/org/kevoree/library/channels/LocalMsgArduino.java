/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.channels;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.Library;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.message.Message;

/**
 *
 * @author ffouquet
 */
@Library(name="KevoreeArduino")
@ChannelTypeFragment
public class LocalMsgArduino extends AbstractChannelFragment {

    @Override
    public Object dispatch(Message msg) {
        StringBuilder context = (StringBuilder) msg.getContent();
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            //forward(p, msg);
        }
        return null;
        
    }

    
    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
