package org.kevoree.library.javase.jmdns;

import org.kevoree.*;
import org.kevoree.Dictionary;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.impl.DefaultKevoreeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/02/13
 * Time: 13:10
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class JmDNSComponent {
    private static Logger logger = LoggerFactory.getLogger(JmDNSComponent.class);

    ServiceListener serviceListener = null;
    List<JmDNS> jmdns = new ArrayList<JmDNS>();
    final String REMOTE_TYPE = "_kevoree-remote._tcp.local.";
    List<String> nodeAlreadyDiscovered = new ArrayList<String>();

    KevoreeFactory factory = new DefaultKevoreeFactory();
    ModelCloner modelCloner = new ModelCloner();

    private String inet;
    private AbstractGroupType group;
    private JmDNSListener listener;
    private int localPort;
    private boolean ipV4Only;

    public JmDNSComponent(AbstractGroupType group, JmDNSListener listener, String inet, int localPort) {
        this(group, listener, inet, localPort, false);
    }

    public JmDNSComponent(AbstractGroupType group, JmDNSListener listener, String inet, int localPort, boolean ipV4Only) {
        this.inet = inet;
        this.group = group;
        this.listener = listener;
        this.localPort = localPort;
        this.ipV4Only = ipV4Only;
    }

    public void start() throws IOException {
        logger.debug("Starting JmDNS component for {}", group.getName());
        initializeJmDNS();

        serviceListener = new ServiceListener() {

            public void serviceAdded(ServiceEvent p1) {
        /*if (p1.getInfo.getSubtype == group.getName) {
          jmdns.requestServiceInfo(p1.getType, p1.getName, 1)
          logger.info("Node added: {} port: {}", Array[String](p1.getInfo.getName, new java.lang.Integer(p1.getInfo.getPort).toString))
          addNodeDiscovered(p1.getInfo)
        }*/
            }

            public void serviceResolved(ServiceEvent p1) {
                if (p1.getInfo().getSubtype().equals(group.getName())) {
                    logger.debug("Node discovered: {} port: {}", new String[]{p1.getInfo().getName(), Integer.toString(p1.getInfo().getPort())});
                    addNodeDiscovered(p1.getInfo());
                }
            }

            public void serviceRemoved(ServiceEvent p1) {
                if (p1.getInfo().getSubtype().equals(group.getName())) {
                    logger.debug("Node disappeared ", p1.getInfo().getName());
                    // REMOVE NODE FROM JMDNS GROUP INSTANCES SUBNODES
                    if (group.getName().equals(p1.getInfo().getSubtype()) && nodeAlreadyDiscovered.contains(p1.getInfo().getName())) {
                        nodeAlreadyDiscovered.remove(p1.getInfo().getName());
                    }
                }
            }
        };

        for (JmDNS jmdnsElement : jmdns) {
            jmdnsElement.addServiceListener(REMOTE_TYPE, serviceListener);
        }

        new Thread() {
            public void run() {
                for (JmDNS jmdnsElement : jmdns) {
                    try {
                        // register the local group fragment on jmdns instances
                        ServiceInfo localServiceInfo = ServiceInfo.create(REMOTE_TYPE, group.getNodeName(), group.getName(), localPort, "");

                        Map<String, String> props = new HashMap<String, String>(3);
                        props.put("groupType", group.getModelElement().getTypeDefinition().getName());
                        props.put("nodeType", group.getModelService().getLastModel().findNodesByID(group.getNodeName()).getTypeDefinition().getName());
                        localServiceInfo.setText(props);
                        jmdnsElement.registerService(localServiceInfo);
                    } catch (IOException e) {
                        logger.debug("Unable to register local service on jmDNS", e);
                    }
                }
            }
        }.start();
    }

    public void stop() {
        new Thread() {
            public void run() {
                if (serviceListener != null) {
                    for (JmDNS jmdnsElement : jmdns) {
                        jmdnsElement.removeServiceListener(REMOTE_TYPE, serviceListener);
                    }
                }
                for (JmDNS jmdnsElement : jmdns) {
                    try {
                        jmdnsElement.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }.start();
    }

    private void initializeJmDNS() throws IOException {
        if ("0.0.0.0".equals(inet)) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isUp()) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress inetAddress = addresses.nextElement();
                        if (!ipV4Only || inetAddress instanceof Inet4Address) {
                            logger.debug("adding jmdns on {}", inetAddress.toString());
                            jmdns.add(JmDNS.create(inetAddress, group.getNodeName() + "." + inetAddress.getHostAddress()));
                            logger.debug("JmDNS listen on {}", inetAddress.getHostAddress());
                        }
                    }
                }
            }
        } else {
            jmdns.add(JmDNS.create(InetAddress.getByName(inet), group.getNodeName() + "." + inet));
            logger.debug("JmDNS listen on {}", inet);
        }
    }

    private boolean updateModel(ContainerRoot model) {
        boolean created = false;
        int i = 1;
        while (!created) {
            try {
                group.getModelService().unregisterModelListener(group);
                group.getModelService().atomicUpdateModel(model);
                group.getModelService().registerModelListener(group);
                created = true;
            } catch (Exception e) {
                logger.warn("Error while trying to update model due to {}, try number {}", new String[]{e.getMessage(), Integer.toString(i)});
            }
            if (i == 20) {
                logger.warn("Unable to update model after {} tries. Update aborted !", i);
            }
            i = i + 1;
        }
        return created;
    }

    private void addNodeDiscovered(ServiceInfo p1) {
        if (p1.getInetAddresses().length > 0 && p1.getPort() != 0) {
            if (!nodeAlreadyDiscovered.contains(p1.getName())) {
                String nodeType = p1.getPropertyString("nodeType");
                String groupType = p1.getPropertyString("groupType");
                String groupName = p1.getSubtype();
                ContainerRoot model = modelCloner.clone(group.getModelService().getLastModel());
                addNode(model, p1.getName(), nodeType);
                updateNetworkProperties(model, p1.getName(), p1.getInetAddresses());
                if (groupName.equals(group.getName()) && groupType.equals(group.getModelElement().getTypeDefinition().getName())) {
                    updateGroup(model, p1.getName(), p1.getPort());
                } else {
                    logger.debug("{} discovers a node using a group which is not the same as the local one:{}.", new String[]{group.getName(), p1.toString()});
                }
                if (updateModel(model)) {
                    logger.debug("model is updated");
                    nodeAlreadyDiscovered.add(p1.getName());
                    if (!group.getNodeName().equals(p1.getName())) {
                        listener.notifyNewSubNode(p1.getName());
                    }
                } else {
                    logger.warn("unable to update the current configuration");
                }
                StringBuilder builder = new StringBuilder();
                for (String nodeName : nodeAlreadyDiscovered) {
                    builder.append(nodeName).append(", ");
                }
                logger.debug("List of discovered nodes <{}>", builder.substring(0, builder.length() - 1));
            } else {
                logger.debug("node already known");
            }
        } else {
            StringBuilder builder = new StringBuilder();
            for (InetAddress address : p1.getInetAddresses()) {
                builder.append(address.toString()).append(", ");
            }
            logger.warn("Unable to get address or port from {} and {}", builder.substring(0, builder.length() - 1), Integer.toString(p1.getPort()));
        }
    }

    private void updateGroup(ContainerRoot model, String remoteNodeName, int port) {
        Group currentGroup = model.findGroupsByID(group.getName());
        ContainerNode remoteNode = model.findNodesByID(remoteNodeName);
        if (remoteNode != null) {
            DictionaryType dicTypeDef = currentGroup.getTypeDefinition().getDictionaryType();
            if (dicTypeDef != null) {
                DictionaryAttribute attPort = dicTypeDef.findAttributesByID("port");
                if (attPort != null) {
                    Dictionary dic = currentGroup.getDictionary();
                    if (dic == null) {
                        dic = factory.createDictionary();
                        currentGroup.setDictionary(dic);
                    }
                    DictionaryValue dicValue = null;
                    for (DictionaryValue val : dic.getValues()) {
                        if (val.getAttribute() == attPort && val.getTargetNode() != null && val.getTargetNode().getName().equals(remoteNodeName)) {
                            dicValue = val;
                            break;
                        }
                    }
                    if (dicValue == null) {
                        dicValue = factory.createDictionaryValue();
                        dicValue.setAttribute(attPort);
                        dicValue.setTargetNode(remoteNode);
                        dic.addValues(dicValue);

                    }
                    dicValue.setValue(Integer.toString(port));
                }
            }
            if (currentGroup.findSubNodesByID(remoteNodeName) == null) {
                currentGroup.addSubNodes(remoteNode);
            }
        }
    }

    private void addNode(ContainerRoot model, String nodeName, String nodeType) {
        ContainerNode remoteNode = model.findNodesByID(nodeName);
        if (remoteNode == null) {
            TypeDefinition nodeTypeDef = model.findTypeDefinitionsByID(nodeType);
            if (nodeTypeDef == null) {
                nodeTypeDef = model.findNodesByID(group.getNodeName()).getTypeDefinition();
            }
            remoteNode = factory.createContainerNode();
            remoteNode.setName(nodeName);
            remoteNode.setTypeDefinition(nodeTypeDef);
            model.addNodes(remoteNode);
        }
    }

    private void updateNetworkProperties(ContainerRoot model, String remoteNodeName, InetAddress[] addresses) {
        for (InetAddress address : addresses) {
            if (!ipV4Only || address instanceof Inet4Address) {
                KevoreePlatformHelper.updateNodeLinkProp(model, group.getNodeName(), remoteNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(), address.getHostAddress(), "LAN-" + address.getHostAddress(), 100);
            }
        }
    }
}
