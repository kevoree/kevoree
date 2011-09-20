/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.javase.virtualCloud;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
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
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 15/09/11
 * Time: 16:26
 */
@NodeType
@DictionaryType({
	@DictionaryAttribute(name = "port", defaultValue = "7000", optional = false)
})
public class VirtualCloudPhysicalNodeType extends AbstractNodeType {
    private static final Logger logger = LoggerFactory.getLogger(VirtualCloudPhysicalNodeType.class);

	// TODO replace jmdns by something else to do discovery ? => maybe for EC2 but not for this implementation

    @Start
    @Override
    public void startNode() {}

    @Stop
    @Override
    public void stopNode() {}

    @Override
    public void push(String physicalNodeName, ContainerRoot root, BundleContext context) {

        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(outStream, root);
            outStream.flush();

            String IP = KevoreePlatformHelper
                    .getProperty(root, physicalNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
            if (IP.equals("")) {
                IP = "127.0.0.1";
            }
            String PORT = KevoreePlatformHelper
                    .getProperty(root, physicalNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_MODELSYNCH_PORT());
            if (PORT.equals("")) {
                PORT = "7000";
            }
            URL url = new URL("http://" + IP + ":" + PORT + "/model/current");

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
                line = rd.readLine();
            }
            wr.close();
            rd.close();

        } catch (Exception e) {
            logger.error("Unable to push a model on " + physicalNodeName, e);

        }

    }

    @Override
    public boolean deploy(AdaptationModel model, String physicalNodeName) {
		// TODO how to do the deploy
        return true;
    }

}
