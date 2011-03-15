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
import org.kevoree.extra.marshalling.RichJSONObject;
import org.kevoree.framework.message.Message;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClock;
import org.kevoree.library.gossiper.version.GossiperMessages.VectorClockUUIDS.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

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
        setExisting(this.channelFragment != null && ( channelFragment.getUUIDS().contains(uuid) || uuid.equals("all")  ));
    }

    @Override
    public Representation doHandle() {
        if (getMethod().equals(Method.POST)) {
            try {
                Tuple2<VectorClock,Object> tuple = channelFragment.getObject(uuid);
                RichJSONObject localObjJSON = new RichJSONObject((Message)tuple._2);
                String res = localObjJSON.toJSON(); 
                ByteString modelBytes = ByteString.copyFromUtf8(res);
                GossiperMessages.VersionedModel model = GossiperMessages.VersionedModel.newBuilder().setVector(tuple._1).setModel(modelBytes).build();
                return new MessageRepresentation<GossiperMessages.VersionedModel>(model);
            } catch (Exception ex) {
                logger.error("Error processing rest resource", ex);
            }
        }
        if (getMethod().equals(Method.GET)) {
            try {
                if(uuid.toString().equals("all")){
                    Builder b = GossiperMessages.VectorClockUUIDS.newBuilder();
                    for(UUID s : channelFragment.getUUIDS()){
                        b.addUuids(s.toString());
                    }
                    return new MessageRepresentation<GossiperMessages.VectorClockUUIDS>(b.build());
                } else {
                    Tuple2<VectorClock,Object> tuple = channelFragment.getObject(uuid);
                    return new MessageRepresentation<GossiperMessages.VectorClock>(tuple._1);
                }
            } catch (Exception ex) {
                logger.error("Error processing rest resource", ex);
            }
        }
        if (getMethod().equals(Method.PUT)) {
            try {
                Representation o = this.getRequestEntity();
                String nodeName = o.getText();
                if (!nodeName.equals("")) {
                    channelFragment.triggerGossipNotification(nodeName);
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
