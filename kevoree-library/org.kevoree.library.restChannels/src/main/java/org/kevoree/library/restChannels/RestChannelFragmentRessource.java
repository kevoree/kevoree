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

import java.util.HashMap;
import org.kevoree.framework.AbstractChannelFragment;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *
 * @author ffouquet
 */
public class RestChannelFragmentRessource extends ServerResource {

    public static HashMap<String, AbstractChannelFragment> channels = new HashMap<String, AbstractChannelFragment>();

    /** The underlying Channel object. */
    AbstractChannelFragment channelFragment;
    /** The sequence of characters that identifies the resource. */
    String channelFragmentName;

    @Override
    protected void doInit() throws ResourceException {

        for(String key : channels.keySet()){
            System.out.println("key="+key);
        }


        // Get the "itemName" attribute value taken from the URI template
        // /channels/{channelFragmentName}.
        this.channelFragmentName = (String) getRequest().getAttributes().get("channelFragmentName");

        System.out.println("asked="+channelFragmentName);

        // Get the item directly from the "persistence layer".
        this.channelFragment = channels.get(channelFragmentName);
        setExisting(this.channelFragment != null);
    }

    @Get()
    public String getName() {
        return channelFragmentName;
    }
}
