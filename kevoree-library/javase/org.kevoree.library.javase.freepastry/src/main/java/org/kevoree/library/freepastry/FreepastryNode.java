package org.kevoree.library.freepastry;

import java.net.InetSocketAddress;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.Constants;
import org.kevoree.ContainerRoot;
import org.kevoree.ContainerNode;
import org.kevoree.ComponentInstance;
import org.kevoree.api.service.core.handler.ModelListener;
import scala.Option;

/**
 * Created by IntelliJ IDEA.
 * User: sunye
 * Date: 28/03/2012
 * Time: 09:00
 * To change this template use File | Settings | File Templates.
 */
@Library(name = "Freepastry")
@ComponentType
@Provides({
    @ProvidedPort(name = "dht", type = PortType.SERVICE, className = DHTNode.class)
})
public class FreepastryNode extends AbstractComponentType {

    private PastryPeer peer = null;

    @Start
    public void start() throws Exception {


        ModelListener listener = new ModelListener() {

            @Override
            public boolean preUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
                return true;
            }

            @Override
            public boolean initUpdate(ContainerRoot currentModel, ContainerRoot proposedModel) {
                return true;
            }

            @Override
            public void modelUpdated() {
                if (peer != null) {
                    return;//EXIT LISTENER
                }
                InetSocketAddress address;
                String componentName = findComponentNameFromSpecificType("Bootstrapper");
                ContainerRoot model = getModelService().getLastModel();
                Option<Integer> portOption = KevoreePropertyHelper.getIntPropertyForComponent(
                        model,
                        componentName,
                        "port");
                Option<String> ipOption = KevoreePropertyHelper.getStringPropertyForComponent(
                        model,
                        componentName,
                        "address");
                try {
                    address = new InetSocketAddress(ipOption.get(), portOption.get());

                    peer = new PastryPeer(address);
                    peer.join();
                    peer.createPast();
                } catch (Exception e) {
                    try {
                        peer.leave();
                    } catch (Exception ignore) {
                    }
                    peer = null;
                } 

            }
        };

        getModelService().registerModelListener(listener);


    }

    @Stop
    public void stop() {
    }

    @Update
    public void update() {
    }

    @Port(name = "dht", method = "put")
    public void put(String key, String value) throws InterruptedException {
        peer.put(key, value);
    }

    @Port(name = "dht", method = "get")
    public String get(String key) throws InterruptedException {
        return peer.get(key);
    }

    private String findComponentNameFromSpecificType(String typeName) {
        for (ContainerNode node : getModelService().getLastModel().getNodesForJ()) {
            for (ComponentInstance component : node.getComponentsForJ()) {
                if (typeName.equals(component.getTypeDefinition().getName())) {
                    return component.getName();
                }
            }
        }
        return null;
    }
}
