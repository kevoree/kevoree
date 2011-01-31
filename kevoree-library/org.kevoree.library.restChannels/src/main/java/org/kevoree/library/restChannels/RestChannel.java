/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.restChannels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.message.*;
import org.kevoree.remote.rest.Handler;

/**
 *
 * @author ffouquet
 */
@Library(name = "art2baselib")
@ChannelTypeFragment
/*@ThirdParties({
@ThirdParty(name="reslet.api",url="mvn:org.restlet.jse/org.restlet/2.1-M2")
})*/
public class RestChannel extends AbstractChannelFragment {

    @Override
    public Object dispatch(Message msg) {

        System.out.println("Local node bsize" + getBindedPorts().size());

        if (getBindedPorts().isEmpty() && getOtherFragments().isEmpty()) {
            System.out.println("No consumer, msg lost=" + msg.getContent());
        }
        for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
            forward(p, msg);
        }
        for (KevoreeChannelFragment cf : getOtherFragments()) {
            if (!msg.getPassedNodes().contains(cf.getNodeName())) {
                forward(cf, msg);
            }
        }
        return null;
    }

    @Start
    public void startHello() {
        RestChannelFragmentRessource.channels.put(this.getName(), this);
        Handler.defaultHost().attach("/channels/{channelFragmentName}", RestChannelFragmentRessource.class);
    }

    @Stop
    public void stopHello() {
        Handler.defaultHost().detach(RestChannelFragmentRessource.class);
        RestChannelFragmentRessource.channels.remove(this.getName());
    }

    @Update
    public void updateHello() {
        //NOOP
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                return null;
            }
        };
    }
}
