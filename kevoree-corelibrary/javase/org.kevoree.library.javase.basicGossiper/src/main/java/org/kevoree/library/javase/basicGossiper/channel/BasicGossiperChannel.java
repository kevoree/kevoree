package org.kevoree.library.javase.basicGossiper.channel;

import org.kevoree.ContainerRoot;
import org.kevoree.KevoreeFactory;
import org.kevoree.annotation.ChannelTypeFragment;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.ModelListener;
import org.kevoree.framework.*;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.framework.message.Message;
import org.kevoree.library.basicGossiper.protocol.gossip.Gossip;
import org.kevoree.library.basicGossiper.protocol.version.Version;
import org.kevoree.library.javase.basicGossiper.GossiperComponent;
import org.kevoree.library.javase.basicGossiper.GossiperPeriodic;
import org.kevoree.library.javase.basicGossiper.GossiperProcess;
import org.kevoree.library.javase.basicGossiper.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Tuple2;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
* @author Erwan Daubert
*         TODO add a DictionaryAttribute to define the number of uuids sent by response when a VectorClockUUIDsRequest is sent
*/
@Library(name = "JavaSE")
@DictionaryType({
		@DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true)
})
@ChannelTypeFragment
public class BasicGossiperChannel extends AbstractChannelFragment implements ModelListener,GossiperComponent {

	private DataManagerForChannel dataManager;
	private Serializer serializer;
	private ChannelScorePeerSelector selector;
	private GossiperPeriodic actor;
	private GossiperProcess processValue;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Start
	public void startGossiperChannel () {
		Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
		serializer = new ChannelSerializer();
		dataManager = new DataManagerForChannel(this, this.getNodeName()/*, this.getModelService()*/);
		processValue = new GossiperProcess(this, dataManager,serializer, true);
		selector = new ChannelScorePeerSelector(timeoutLong, this.getModelService(), this.getNodeName());
		logger.debug(this.getName() + ": initialize GossiperActor");
		actor = new GossiperPeriodic(this, timeoutLong, selector, processValue);
		processValue.start();
		actor.start();
        this.getModelService().registerModelListener(this);
	}

	@Stop
	public void stopGossiperChannel () {
        this.getModelService().unregisterModelListener(this);
        if (actor != null) {
			actor.stop();
			actor = null;
		}
		if (selector != null) {
			selector = null;
		}
		if (processValue != null) {
			processValue.stop();
			processValue = null;
		}
		if (dataManager != null) {
			dataManager = null;
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
								/*.setTimestamp(timestamp)*/.setVersion(2).build()).setTimestamp(timestamp).build(),
				msg);
		dataManager.setData(uuid, tuple, "");

        notifyPeersInternal(peers.get());
		//SYNCHRONOUS NON IMPLEMENTED
		return null;
	}


    private void notifyPeersInternal(List<String> l) {
        org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message.Builder messageBuilder = org.kevoree.library.basicGossiper.protocol.message.KevoreeMessage.Message.newBuilder().setDestName(getName()).setDestNodeName(getNodeName());
        messageBuilder.setContentClass(Gossip.UpdatedValueNotification.class.getName()).setContent(Gossip.UpdatedValueNotification.newBuilder().build().toByteString());
        for (String peer : l) {
            if (!peer.equals(getNodeName())) {
                String address = getAddress(peer);
                processValue.netSender().sendMessageUnreliable(messageBuilder.build(), new InetSocketAddress(address, parsePortNumber(peer)));
            }
        }
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
    public String getAddress(String remoteNodeName) {
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper
                .getStringNetworkProperties(currentCacheModel.get(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            return ipOption.get();
        } else {
            return "127.0.0.1";
        }
    }

	@Override
	public int parsePortNumber (String nodeName) {
		Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForChannel(this.getModelService().getLastModel(), this.getName(), "port", true, nodeName);
		if (portOption.isDefined()) {
			return portOption.get();
		}
		return 9000;
	}

    @Override
    public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    @Override
    public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        return true;
    }

    protected AtomicReference<ContainerRoot> currentCacheModel = new AtomicReference<ContainerRoot>(KevoreeFactory.createContainerRoot());
    protected AtomicReference<List<String>> peers = new AtomicReference<List<String>>();

    @Override
    public boolean afterLocalUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
        currentCacheModel.set(proposedModel);
        List<String> LoopPeers = new ArrayList<String>();
        for (KevoreeChannelFragment fragment : getOtherFragments()) {
            LoopPeers.add(fragment.getNodeName());
        }
        peers.set(LoopPeers);
        return true;
    }

    @Override
    public void modelUpdated() {
    }

    @Override
    public void preRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }

    @Override
    public void postRollback(ContainerRoot currentModel, ContainerRoot proposedModel) {
    }
}
