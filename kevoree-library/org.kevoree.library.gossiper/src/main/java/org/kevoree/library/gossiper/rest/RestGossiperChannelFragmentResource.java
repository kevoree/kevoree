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
package org.kevoree.library.gossiper.rest;

import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.gossiper.version.GossiperMessages;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ffouquet
 */
public class RestGossiperChannelFragmentResource extends ServerResource {

    public static Map<String, RestGossiperChannel> channels = new Hashtable<String, RestGossiperChannel>();
    private Logger logger = LoggerFactory.getLogger(RestGossiperChannelFragmentResource.class);
    /**
     * The underlying Channel object.
     */
    RestGossiperChannel channelFragment;
    /**
     * The sequence of characters that identifies the resource.
     */
    String channelName;
    
    UUID uuid;

    @Override
    protected void doInit() throws ResourceException {
        this.channelName = (String) getRequest().getAttributes().get("channelName");
        this.uuid = UUID.fromString(getRequest().getAttributes().get("uuid").toString());
        this.channelFragment = channels.get(channelName);
        setExisting(this.channelFragment != null);
    }

    @Override
    public Representation doHandle() {
        if (getMethod().equals(Method.POST)) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                KevoreeXmiHelper.saveStream(out, groupFragment.getModelService().getLastModel());
                out.flush();
                //   String flatModel = new String(out.toByteArray());
                //   String flatZippedModel = new String(StringZipper.zipStringToBytes(flatModel),"UTF-8");

                ByteString modelBytes = ByteString.copyFrom(out.toByteArray());
                out.close();

                GossiperMessages.VersionedModel model = GossiperMessages.VersionedModel.newBuilder().setVector(groupFragment.currentClock()).setModel(modelBytes).build();

                return new MessageRepresentation<GossiperMessages.VersionedModel>(model);
            } catch (IOException ex) {
                logger.error("Error processing rest resource", ex);
            }

        }
        if (getMethod().equals(Method.GET)) {
            try {
                return new MessageRepresentation<GossiperMessages.VectorClock>(groupFragment.incrementedVectorClock());
            } catch (Exception ex) {
                logger.error("Error processing rest resource", ex);
            }
        }
        if (getMethod().equals(Method.PUT)) {
            try {
                Representation o = this.getRequestEntity();
                String nodeName = o.getText();

                // Object o = this.getRequestAttributes().get("remotePeerNodeName");
                if (!nodeName.equals("")) {
                    groupFragment.triggerGossipNotification(nodeName);
                } else {
                    logger.warn("Bad request receive for gossip notify");
                }
            } catch (Exception ex) {
                logger.error("Error processing rest resource", ex);
            }

        }
        return new StringRepresentation("Error");
    }
}
