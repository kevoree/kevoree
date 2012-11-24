package org.kevoree.library;

import org.kevoree.ContainerRoot;
import org.kevoree.DictionaryAttribute;
import org.kevoree.api.service.core.script.KevScriptEngineFactory;
import org.kevoree.cloner.ModelCloner;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePlatformHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 09/11/12
 * Time: 07:41
 */
public class NodeNetworkHelper {

    private static Logger logger = LoggerFactory.getLogger(NodeNetworkHelper.class);

    public static void main(String[] args){
        getAddresses();

    }

    public static java.util.HashMap<String,String> getAddresses() {
        java.util.HashMap<String,String> addresses = new java.util.HashMap<String,String>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while(networkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isLoopback()) {
                      for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()){
                          if(interfaceAddress.getAddress() instanceof Inet4Address){
                              addresses.put(interfaceAddress.getAddress().getHostAddress(),networkInterface.getDisplayName());
                          }
                      }
                }
            }
        }
        catch(Exception e) {
            logger.error("",e);
        }
        return addresses;
    }


    public static ContainerRoot addNetworkProperty (ContainerRoot model, String nodeName ,java.util.HashMap<String,String> ips , KevScriptEngineFactory kevScriptEngineFactory) {
        for(String key : ips.keySet()){
            KevoreePlatformHelper.updateNodeLinkProp(model, nodeName, nodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP(), key, ips.get(key), 100);
            logger.info("add {} as IP of {}", key, nodeName);

        }
        return model;
    }

    private static ModelCloner cloner = new ModelCloner();

    public static ContainerRoot updateModelWithNetworkProperty (AbstractGroupType group) {
        Object ipObject = group.getDictionary().get("ip");
        ContainerRoot readWriteModel = cloner.clone(group.getModelService().getLastModel());
        if (ipObject != null && !ipObject.toString().equals("") && !ipObject.toString().equals("0.0.0.0")) {
            java.util.HashMap<String,String> addresses = new java.util.HashMap<String,String>();
            addresses.put(ipObject.toString(),"unknown");
            return addNetworkProperty(readWriteModel, group.getNodeName(),addresses, group.getKevScriptEngineFactory());
        } else {
            java.util.HashMap<String,String> addresses = getAddresses();
            if (!addresses.isEmpty()) {
                return addNetworkProperty(readWriteModel, group.getNodeName(), addresses, group.getKevScriptEngineFactory());
            } else {
                return null;
            }
        }
    }

}
