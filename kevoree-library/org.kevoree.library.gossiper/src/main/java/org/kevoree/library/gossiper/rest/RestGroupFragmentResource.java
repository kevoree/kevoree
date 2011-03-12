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

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.gossiper.version.GossiperMessages;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ffouquet
 */
public class RestGroupFragmentResource extends ServerResource {

    public static Map<String, RestGossipGroup> groups = Collections.synchronizedMap(new HashMap<String, RestGossipGroup>());
    /**
     * The underlying Channel object.
     */
    RestGossipGroup groupFragment;
    /**
     * The sequence of characters that identifies the resource.
     */
    String groupName;

    @Override
    protected void doInit() throws ResourceException {
        this.groupName = (String) getRequest().getAttributes().get("groupName");
        this.groupFragment = groups.get(groupName);
        setExisting(this.groupFragment != null);
    }

    @Override
    public Representation doHandle() {
        if (getMethod().equals(Method.POST)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(out, groupFragment.getModelService().getLastModel());
            String flatModel = new String(out.toByteArray());
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(RestGroupFragmentResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            GossiperMessages.VersionedModel model = GossiperMessages.VersionedModel.newBuilder().setVector(groupFragment.currentClock()).setModel(flatModel).build();
            return new MessageRepresentation<GossiperMessages.VersionedModel>(model);
        }
        if (getMethod().equals(Method.GET)) {
            return new MessageRepresentation<GossiperMessages.VectorClock>(groupFragment.incrementedVectorClock());
        }
        if (getMethod().equals(Method.PUT)) {
            //TRIGGER AND UPDATE FROM REMOTE NODE
            Object o = this.getRequestAttributes().get("remotePeerNodeName");
            if(o != null){
                
            }
        }
        return new StringRepresentation("Error");
    }
}
