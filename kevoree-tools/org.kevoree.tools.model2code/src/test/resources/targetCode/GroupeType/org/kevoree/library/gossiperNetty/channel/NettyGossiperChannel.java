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
package org.kevoree.library.gossiperNetty.channel;

import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.*;
import org.kevoree.framework.message.Message;
import org.kevoree.library.gossiperNetty.DataManager;
import org.kevoree.library.gossiperNetty.GossiperActor;
import org.kevoree.library.gossiperNetty.NettyGossipAbstractElement;
import org.kevoree.library.gossiperNetty.Serializer;
import org.kevoree.library.gossiperNetty.version.Version;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;

/**
 * @author Erwan Daubert
 *         TODO add a DictionaryAttribute to define the number of uuids sent by response when a VectorClockUUIDsRequest is sent
 */
@Library(name = "Kevoree-Netty")
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true),
		@DictionaryAttribute(name = "port", defaultValue = "9000", optional = true),
		@DictionaryAttribute(name = "FullUDP", defaultValue = "true", optional = true)/*,
		@DictionaryAttribute(name = "sendNotification", defaultValue = "false", optional = true),
		@DictionaryAttribute(name = "alwaysAskModel", defaultValue = "false", optional = true)*/
})
@ChannelTypeFragment
public class NettyGossiperChannel extends AbstractChannelFragment implements NettyGossipAbstractElement {

	private DataManager dataManager = null;//new DataManager();
	private Serializer serializer = null;
	private org.kevoree.library.gossiperNetty.PeerSelector selector = null;
	private GossiperActor actor = null;
	private ServiceReference sr;
	private KevoreeModelHandlerService modelHandlerService = null;
	private Logger logger = LoggerFactory.getLogger(NettyGossiperChannel.class);

	@Start
	public void startGossiperChannel () {
		Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
		sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
		modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);

		long timeout = Long.parseLong((String) this.getDictionary().get("interval"));

		dataManager = new DataManagerForChannel();
		((DataManagerForChannel) dataManager).start();

		serializer = new ChannelSerializer();
		selector = new ChannelPeerSelector(timeout, this);

		actor = new GossiperActor(timeout,
				this,
				dataManager,
				parsePortNumber(getNodeName()),
				parseBooleanProperty("FullUDP"),
				true, serializer,
				selector, parseBooleanProperty("alwaysAskModel"));
		actor.start();
	}

	@Stop
	public void stopGossiperChannel () {
		if (actor != null) {
			actor.stop();
			actor = null;
		}
		if (dataManager != null) {
			dataManager.stop();
		}
		if (modelHandlerService != null) {
			Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
			bundle.getBundleContext().ungetService(sr);
			modelHandlerService = null;
		}
	}

	@Update
	public void updateGossiperChannel () {
		// TODO use the garbage of the dataManager
		Map<UUID, Version.VectorClock> vectorClockUUIDs = dataManager.getUUIDVectorClocks();
		Map<UUID, Tuple2<Version.VectorClock, Object>> messages = new HashMap<UUID, Tuple2<Version.VectorClock, Object>>();
		for (UUID uuid : vectorClockUUIDs.keySet()) {
			messages.put(uuid, dataManager.getData(uuid));
		}

		stopGossiperChannel();
		startGossiperChannel();

		for (UUID uuid : messages.keySet()) {
			dataManager.setData(uuid, messages.get(uuid), "");
		}
	}

	@Override
	public Object dispatch (Message msg) {
		//Local delivery
		localNotification(msg);

		//CREATE NEW MESSAGE
		long timestamp = System.currentTimeMillis();
		UUID uuid = UUID.randomUUID();
		Tuple2<Version.VectorClock, Object> tuple = new Tuple2<Version.VectorClock, Object>(
				Version.VectorClock.newBuilder().
						addEnties(Version.ClockEntry.newBuilder().setNodeID(this.getNodeName())
								.setTimestamp(timestamp).setVersion(2l).build()).setTimestamp(timestamp).build(),
				msg);
		dataManager.setData(uuid, tuple, "");

		actor.notifyPeers();
		//SYNCHRONOUS NON IMPLEMENTED
		return null;
	}

	@Override
	public ChannelFragmentSender createSender (String remoteNodeName, String remoteChannelName) {
		return new NoopChannelFragmentSender();
	}

	@Override
	public void localNotification (Object o) {
		if (o instanceof Message) {
			for (org.kevoree.framework.KevoreePort p : getBindedPorts()) {
				forward(p, (Message) o);
			}
		}
	}

	@Override
	public List<String> getAllPeers () {
		List<String> peers = new ArrayList<String>();
		for (KevoreeChannelFragment fragment : getOtherFragments()) {
			peers.add(fragment.getNodeName());
		}
		return peers;
	}

	@Override
	public String getAddress (String remoteNodeName) {
		String ip = KevoreePlatformHelper.getProperty(modelHandlerService.getLastModel(), remoteNodeName,
				org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
		if (ip == null || ip.equals("")) {
			ip = "127.0.0.1";
		}
		return ip;
	}

	private String name = "[A-Za-z0-9_]*";
	private String portNumber = "(65535|5[0-9]{4}|4[0-9]{4}|3[0-9]{4}|2[0-9]{4}|1[0-9]{4}|[0-9]{0,4})";
	private String separator = ",";
	private String affectation = "=";
	private String portPropertyRegex =
			"((" + name + affectation + portNumber + ")" + separator + ")*(" + name + affectation + portNumber + ")";

	@Override
	public int parsePortNumber (String nodeName) {
		String portProperty = this.getDictionary().get("port").toString();
		if (portProperty.matches(portPropertyRegex)) {
			String[] definitionParts = portProperty.split(separator);
			for (String part : definitionParts) {
				if (part.contains(nodeName + affectation)) {
					//System.out.println(Integer.parseInt(part.substring((nodeName + affectation).length(), part.length())));
					return Integer.parseInt(part.substring((nodeName + affectation).length(), part.length()));
				}
			}
		} else {
			return Integer.parseInt(portProperty);
		}
		return 0;
	}

	@Override
	public Boolean parseBooleanProperty (String name) {
		return this.getDictionary().get(name) != null && this.getDictionary().get(name).toString().equals("true");
	}

	/*@Override
		 public String selectPeer() {
			 int othersSize = this.getOtherFragments().size();
			 if (othersSize > 0) {
				 SecureRandom diceRoller = new SecureRandom();
				 int peerIndex = diceRoller.nextInt(othersSize);
				 return this.getOtherFragments().get(peerIndex).getNodeName();
			 } else {
				 return "";
			 }
		 }*/
}
