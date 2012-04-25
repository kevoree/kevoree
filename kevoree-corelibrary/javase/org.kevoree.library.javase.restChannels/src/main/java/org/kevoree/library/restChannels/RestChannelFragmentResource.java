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
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author ffouquet
 */
public class RestChannelFragmentResource extends ServerResource {
	private static final Logger logger = LoggerFactory.getLogger(RestChannelFragmentResource.class);

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

    public Representation doHandle() {
        if (getMethod().equals(Method.POST)) {
            try {
                return new StringRepresentation(postInput(getRequestEntity().getText()));
            } catch (IOException e) {
                return new StringRepresentation(e.getLocalizedMessage());
            }
        }
        if (getMethod().equals(Method.GET)) {
           try {
                return new StringRepresentation(getInput(getRequestEntity().getText()));
            } catch (IOException e) {
                return new StringRepresentation(e.getLocalizedMessage());
            }
        }
        return new StringRepresentation("Error");
    }


    //@Get()
    public String getInput(String entity) {
        Message msg = buildMsg(entity);
        Object o = channelFragment.remoteDispatch(msg);
        RichJSONObject oo = new RichJSONObject(o);
        return oo.toJSON();
    }

    //@Post()
    public String postInput(String entity) {
        Message msg = buildMsg(entity);
        logger.debug(entity);

        Object o = channelFragment.remoteDispatch(msg);
        return "<ack />";
    }

    private Message buildMsg(String entity) {
        Message msg = buildMessageFromJSON(entity);
        if (msg == null) {
            msg = buildMessageFromAttribute(entity);
        }
        return msg;
    }

    private Message buildMessageFromJSON(String entity) {
        try {
            RichString c = new RichString(entity);
            Message obj = (Message) c.fromJSON(Message.class);
            return obj;
        } catch (Exception e) {
//            e.printStackTrace();
			logger.error("Unable to build message from JSON (" + entity + ")", e);
        }
        return null;
    }

    private Message buildMessageFromAttribute(String entity) {
        Message msg = new Message();
        for (String key : getRequest().getAttributes().keySet()) {
            logger.debug(key + "--" + getRequest().getAttributes().get(key));
        }
        return msg;
    }

}
