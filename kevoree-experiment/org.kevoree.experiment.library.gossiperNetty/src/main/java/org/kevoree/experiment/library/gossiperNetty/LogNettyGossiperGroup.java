package org.kevoree.experiment.library.gossiperNetty;

import org.greg.client.ForkedConfiguration;
import org.greg.client.ForkedGregClient;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.library.gossiperNetty.GossiperActor;
import org.kevoree.library.gossiperNetty.Serializer;
import org.kevoree.library.gossiperNetty.group.DataManagerForGroup;
import org.kevoree.library.gossiperNetty.group.GroupPeerSelector;
import org.kevoree.library.gossiperNetty.group.GroupSerializer;
import org.kevoree.library.gossiperNetty.group.NettyGossiperGroup;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GroupType
@Library(name = "KevoreeExperiment")
@DictionaryType({
    @DictionaryAttribute(name = "loggerServerIP", defaultValue = "127.0.0.1")
})
public class LogNettyGossiperGroup extends NettyGossiperGroup {

    private ForkedGregClient client = null;
    private Logger logger = LoggerFactory.getLogger(LogNettyGossiperGroup.class);

    @Override
    public void startGossiperGroup() {
        logger.debug("starting group instance");

        FailureSimulation.startServer(parsePortNumber(getNodeName()) + 1000);

        ForkedConfiguration clientConfig = new ForkedConfiguration();
        clientConfig.clientId = this.getNodeName();
        clientConfig.server = this.getDictionary().get("loggerServerIP").toString();
        clientConfig.calibrationPort = 5677;
        clientConfig.port = 5676;
        client = new ForkedGregClient(clientConfig);


        Bundle bundle = (Bundle) this.getDictionary().get("osgi.bundle");
        sr = bundle.getBundleContext().getServiceReference(KevoreeModelHandlerService.class.getName());
        modelHandlerService = (KevoreeModelHandlerService) bundle.getBundleContext().getService(sr);

        dataManager = new LogDataManagerForGroup(client, this.getName(), this.getNodeName(), modelHandlerService);
		((LogDataManagerForGroup)dataManager).start();

        sendNotification = parseBooleanProperty("sendNotification");

        Long timeoutLong = Long.parseLong((String) this.getDictionary().get("interval"));
        Serializer serializer = new GroupSerializer(modelHandlerService);
        selector = new StrictGroupPeerSelector(timeoutLong, modelHandlerService, this.getName());
        actor = new LogGossiperActor(timeoutLong, this, dataManager, parsePortNumber(getNodeName()),
                parseBooleanProperty("FullUDP"), false, serializer, selector, parseBooleanProperty("alwaysAskModel"));
		actor.start();
        logger.debug("group instance started");
    }

    @Override
    public void stopGossiperGroup() {
        FailureSimulation.stop();
        super.stopGossiperGroup();
        client.stop();
    }

    @Override
    public List<String> getAllPeers() {
        return PeersHelper.getPeers(modelHandlerService.getLastModel(), this.getName(), this.getNodeName());
    }
}
