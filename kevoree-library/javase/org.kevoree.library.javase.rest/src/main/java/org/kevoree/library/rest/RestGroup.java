package org.kevoree.library.rest;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 18:27
 */


@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
		@DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true)
})
@GroupType
@Library(name = "JavaSE")
public class RestGroup extends AbstractGroupType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerBootstrap server = new ServerBootstrap(this);

    @Start
    public void startRestGroup() {
        logger.warn("Rest service start on port " + this.getDictionary().get("port").toString());
		Object ipOption = this.getDictionary().get("ip");
		String ip = "0.0.0.0";
		if (ipOption != null) {
			ip = ipOption.toString();
		}
        server.startServer(Integer.parseInt(this.getDictionary().get("port").toString()), ip);

       //logger.info("!!! try to block => "+getModelService().getLastModel()+"->"+getModelService().getLastModification());

    }

    @Stop
    public void stopRestGroup() {
        server.stop();
    }

    @Override
    public void triggerModelUpdate() {
        ContainerRoot model = this.getModelService().getLastModel();
        for (Group group : model.getGroupsForJ()) {
            if (group.getName().equals(this.getName())) {
                for (ContainerNode subNode : group.getSubNodesForJ()) {
                    if (!subNode.getName().equals(this.getNodeName())) {
                        //push(model, subNode.getName());
                    }
                }
                return;
            }
        }

    }

    @Override
    public void push(ContainerRoot model, String targetNodeName) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            KevoreeXmiHelper.saveStream(outStream, model);
            outStream.flush();
            String IP = KevoreePlatformHelper.getProperty(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
            if (IP.equals("")) {
                IP = "127.0.0.1";
            }

            int PORT = KevoreeFragmentPropertyHelper.getIntPropertyFromFragmentGroup(model, this.getName(), "port", targetNodeName);

            logger.debug("port=>" + PORT);

            URL url = new URL("http://" + IP + ":" + PORT + "/model/current");
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(3000);
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
            //			e.printStackTrace();
            logger.error("Unable to push a model on " + targetNodeName, e);

        }
    }


    public String getAddress(String remoteNodeName) {

        logger.debug("ModelService "+getModelService());

        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public int parsePortNumber(String nodeName) throws IOException {
        try {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper
                    .getIntPropertyFromFragmentGroup(this.getModelService().getLastModel(), this.getName(), "port",
                            nodeName);
        } catch (NumberFormatException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public ContainerRoot pull(String targetNodeName) {
        String localhost = "localhost";
        int port=8000;

        try
        {
            localhost = getAddress(targetNodeName);
            port = parsePortNumber(targetNodeName);
        } catch (IOException e) {
           logger.error("Unable to getAddress or Port of " + targetNodeName, e);
        }

        logger.debug("Pulling model "+targetNodeName+" "+"http://" + localhost + ":" + port + "/model/current");
        try {
            URL url = new URL("http://" + localhost + ":" + port + "/model/current");
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(2000);
            InputStream inputStream = conn.getInputStream();
            return KevoreeXmiHelper.loadStream(inputStream);
        } catch (IOException e) {
            logger.error("error while pulling model for name " + targetNodeName, e);
        }
        return null;
    }

	public void updateModel(ContainerRoot model) {
		this.getModelService().updateModel(model);
	}

	public String getModel() {
		return KevoreeXmiHelper.saveToString(this.getModelService().getLastModel(), false);
	}
}
