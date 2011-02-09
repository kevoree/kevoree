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

import org.kevoree.extra.marshalling.RichJSONObject;
import org.kevoree.extra.marshalling.RichString;
import org.kevoree.framework.AbstractChannelFragment;
import org.kevoree.framework.message.Message;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import scala.actors.Actor;

import java.util.HashMap;

/**
 * @author ffouquet
 */
public class RestChannelFragmentRessource extends ServerResource {

    public static HashMap<String, AbstractChannelFragment> channels = new HashMap<String, AbstractChannelFragment>();

    /**
     * The underlying Channel object.
     */
    AbstractChannelFragment channelFragment;
    /**
     * The sequence of characters that identifies the resource.
     */
    String channelFragmentName;

    @Override
    protected void doInit() throws ResourceException {
        this.channelFragmentName = (String) getRequest().getAttributes().get("channelFragmentName");
        this.channelFragment = channels.get(channelFragmentName);
        setExisting(this.channelFragment != null);
    }

    @Get()
    public String getInput() {
        Message msg = buildMsg();
        Object o = channelFragment.dispatch(msg);
        RichJSONObject oo = new RichJSONObject(o);
        return oo.toJSON();
    }

    @Post()
    public String postInput() {
        Message msg = buildMsg();
        Object o = channelFragment.remoteDispatch(msg);
        return "<ack />";
    }

    private Message buildMsg(){
        Message msg =  buildMessageFromJSON();
        if(msg == null) { msg = buildMessageFromAttribute(); }
        return msg;
    }

    private Message buildMessageFromJSON() {
        try {
            RichString c = new RichString(getRequest().getEntityAsText());
            Message obj = (Message) c.fromJSON(Message.class);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Message buildMessageFromAttribute() {
        Message msg = new Message();
        for (String key : getRequest().getAttributes().keySet()) {
            System.out.println("Debug=" + key + "--" + getRequest().getAttributes().get(key));
        }
        return msg;
    }

}
