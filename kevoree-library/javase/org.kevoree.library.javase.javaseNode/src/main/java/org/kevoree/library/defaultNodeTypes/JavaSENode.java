/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.NodeType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.framework.Constants;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoreeAdaptation.AdaptationModel;
import org.osgi.framework.BundleContext;
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
public class JavaSENode extends AbstractNodeType {
    private static final Logger logger = LoggerFactory.getLogger(JavaSENode.class);

    private JmDnsComponent jmDnsComponent = null;

    @Start
    @Override
    public void startNode() {
        Integer port = ((System.getProperty("node.port") == null) ? 8000 : Integer.parseInt(System.getProperty("node.port")));
        jmDnsComponent = new JmDnsComponent(this.getNodeName(), port, this.getModelService());
    }

    @Stop
    @Override
    public void stopNode() {
        jmDnsComponent.close();
    }

    @Override
    public void push(String targetNodeName, ContainerRoot root, BundleContext context) {

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

    @Override
    public boolean deploy(AdaptationModel model, String nodeName) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return true;
    }

}
