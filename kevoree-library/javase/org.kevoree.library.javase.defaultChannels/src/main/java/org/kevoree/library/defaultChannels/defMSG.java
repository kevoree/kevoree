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
package org.kevoree.library.defaultChannels;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.NoopChannelFragmentSender;
import org.kevoree.framework.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ffouquet
 */
@Library(name = "Kevoree-Android-JavaSE")
@ChannelTypeFragment
public class defMSG extends AbstractChannelFragment {

	private Logger logger = LoggerFactory.getLogger(defMSG.class);


    @Override
    public Object dispatch(Message msg) {

        //System.out.println("Local node bsize" + getBindedPorts().size());

        if (getBindedPorts().isEmpty() && getOtherFragments().isEmpty()) {

           logger.warn("No consumer, msg lost=" + msg.getContent());
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
       logger.debug("Hello Channel");
    }

    @Stop
    public void stopHello() {
        logger.debug("Bye Channel");
    }

    @Update
    public void updateHello() {
        for (String s : this.getDictionary().keySet()) {
            logger.debug("Dic => " + s + " - " + this.getDictionary().get(s));
        }
    }

    @Override
    public ChannelFragmentSender createSender(String remoteNodeName, String remoteChannelName) {
        return new NoopChannelFragmentSender();
    }

}
