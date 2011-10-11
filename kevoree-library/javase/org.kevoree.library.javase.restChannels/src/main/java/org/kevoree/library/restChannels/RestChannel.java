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

import org.kevoree.ContainerNode;
import org.kevoree.DictionaryValue;
import org.kevoree.annotation.*;
import org.kevoree.extra.marshalling.RichJSONObject;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.ChannelFragmentSender;
import org.kevoree.framework.KevoreeChannelFragment;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.message.Message;
import org.kevoree.remote.rest.Handler;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@ChannelTypeFragment
/*
@ThirdParties({
        @ThirdParty(name = "org.kevoree.extra.marshalling", url = "mvn:org.kevoree.extra/org.kevoree.extra.marshalling")
}) */
public class RestChannel extends AbstractChannelFragment {
	private static final Logger logger = LoggerFactory.getLogger(RestChannel.class);
    private Bundle bundle = null;
    private ServiceReference sr = null;

    @Override
    public Object dispatch(Message msg) {

        logger.debug("Local node bsize" + getBindedPorts().size());
        logger.debug("Remote node bsize" + getOtherFragments().size());


        if (getBindedPorts().isEmpty() && getOtherFragments().isEmpty()) {
            logger.debug("No consumer, msg lost=" + msg.getContent());
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
        /* Get last model handler - previously deploy by kevoree core*/
        RestChannelFragmentResource.channels.put(this.getName(), this);
        Handler.getDefaultHost().attach("/channels/{channelFragmentName}", RestChannelFragmentResource.class);
        Handler.getDefaultHost().attach("/channels", RestChannelsResource.class);

    }

    @Stop
    public void stopHello() {
        Handler.getDefaultHost().detach(RestChannelFragmentResource.class);
        Handler.getDefaultHost().detach(RestChannelsResource.class);
        RestChannelFragmentResource.channels.remove(this.getName());
        //bundle.getBundleContext().ungetService(sr);
    }

    @Update
    public void updateHello() {
        //NOOP
    }

    @Override
    public ChannelFragmentSender createSender(final String remoteNodeName, final String remoteChannelName) {
        return new ChannelFragmentSender() {

            @Override
            public Object sendMessageToRemote(Message message) {
                String lastUrl = null;
                try {
                    message.getPassedNodes().add(getModelService().getNodeName());
                    lastUrl = ModelHelper.buildURL(getModelService(),remoteNodeName,remoteChannelName);
                    logger.debug("remote rest url =>" + lastUrl);
                    ClientResource remoteChannelResource = new ClientResource(lastUrl);
                    if (message.getInOut()) {
                        logger.error("Not implemented yet !");
                    } else {
                        RichJSONObject obj = new RichJSONObject(message);
                        Representation representation = new StringRepresentation(obj.toJSON(), MediaType.TEXT_PLAIN);
                        representation.setCharacterSet(CharacterSet.UTF_8);
                        remoteChannelResource.post(representation);
                    }
                } catch (Exception e) {
                    logger.error("Fail to send to remote channel via =>" + lastUrl + "\nReply not implemented => message lost !!!", e);
                }
                return null;
            }
        };
    }
}
