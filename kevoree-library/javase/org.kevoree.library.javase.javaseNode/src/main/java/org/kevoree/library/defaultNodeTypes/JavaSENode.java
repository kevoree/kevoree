/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.adaptation.deploy.osgi.BaseDeployOSGi;
import org.kevoree.adaptation.deploy.osgi.JavaSePrimitive;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author ffouquet
 */
@NodeType
@DictionaryType({
    @DictionaryAttribute(name = "autodiscovery", defaultValue = "true", optional = true,vals={"true","false"})
})
@PrimitiveCommands(values = {"UpdateType","UpdateDeployUnit","AddType","AddDeployUnit","AddThirdParty","RemoveType","RemoveDeployUnit","UpdateInstance","UpdateBinding","UpdateDictionaryInstance","AddInstance","RemoveInstance","AddBinding","RemoveBinding","AddFragmentBinding","RemoveFragmentBinding","StartInstance","StopInstance"},value={})
public class JavaSENode extends AbstractNodeType {
    private static final Logger logger = LoggerFactory.getLogger(JavaSENode.class);

    private JmDnsComponent jmDnsComponent = null;
    private KevoreeKompareBean kompareBean = null;
    private BaseDeployOSGi deployBean = null;


    @Start
    @Override
    public void startNode() {
        kompareBean = new KevoreeKompareBean();
        deployBean = new BaseDeployOSGi((Bundle)this.getDictionary().get("osgi.bundle"));
        Integer port = ((System.getProperty("node.port") == null) ? 8000 : Integer.parseInt(System.getProperty("node.port")));
        if(this.getDictionary().get("autodiscovery").equals("true")){
            jmDnsComponent = new JmDnsComponent(this.getNodeName(), port, this.getModelService(),this.getClass().getSimpleName());
        }
    }

    @Stop
    @Override
    public void stopNode() {
        if(jmDnsComponent != null){
            jmDnsComponent.close();
        }
        kompareBean = null;
        deployBean = null;
    }

    @Override
    public AdaptationModel kompare(ContainerRoot current, ContainerRoot target) {
        return kompareBean.kompare(current,target,this.getNodeName());
    }

    @Override
    public org.kevoree.framework.PrimitiveCommand getPrimitive(AdaptationPrimitive adaptationPrimitive) {
        return deployBean.buildPrimitiveCommand(adaptationPrimitive,this.getNodeName());
    }

    @Override
    public void push(String targetNodeName, ContainerRoot root) {

        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(outStream, root);
            outStream.flush();

            String IP = KevoreePlatformHelper
                    .getProperty(root, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
            if (IP.equals("")) {
                IP = "127.0.0.1";
            }
            String PORT = KevoreePlatformHelper
                    .getProperty(root, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
            if (PORT.equals("")) {
                PORT = "8000";
            }
            URL url = new URL("http://" + IP + ":" + PORT + "/model/current");

//			System.out.println(url);

            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(outStream.toString());
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = rd.readLine();
            while (line != null) {
//				System.out.println(line);
                line = rd.readLine();
            }
            wr.close();
            rd.close();

        } catch (Exception e) {
//			e.printStackTrace();
            logger.error("Unable to push a model on " + targetNodeName, e);

        }

    }

}
