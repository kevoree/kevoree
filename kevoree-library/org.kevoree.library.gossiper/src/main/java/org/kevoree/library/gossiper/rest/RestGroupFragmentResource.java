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
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.library.gossiper.version.GossiperMessages;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * @author ffouquet
 */
public class RestGroupFragmentResource extends ServerResource {

    public static HashMap<String, RestGossipGroup> groups = new HashMap<String, RestGossipGroup>();

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

    public Representation doHandle() {
        if (getMethod().equals(Method.POST)) {


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(out,groupFragment.getModelService().getLastModel());
            try {
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String flatModel = new String(out.toByteArray());
            GossiperMessages.VersionedModel model = GossiperMessages.VersionedModel.newBuilder().setVector(groupFragment.clockRef.get()).setModel(flatModel).build();
            Representation representation = new StringRepresentation(model.toByteString().toStringUtf8());
            representation.setMediaType(MediaType.TEXT_HTML);
            return representation;
        }
        if (getMethod().equals(Method.GET)) {
            Representation representation = new StringRepresentation(groupFragment.clockRef.get().toByteString().toStringUtf8());
            representation.setMediaType(MediaType.TEXT_HTML);
            return representation;
        }
        return new StringRepresentation("Error");
    }

}
