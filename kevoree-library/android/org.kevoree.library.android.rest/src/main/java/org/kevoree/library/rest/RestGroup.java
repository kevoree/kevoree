package org.kevoree.library.rest;

import org.kevoree.ContainerRoot;
import org.kevoree.DictionaryValue;
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
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true , fragmentDependant = true)
})
@GroupType
@Library(name="Android")
public class RestGroup extends AbstractGroupType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ServerBootstrap server = new ServerBootstrap(this);

    @Start
    public void startRestGroup() {
        logger.warn("Rest service start on port "+this.getDictionary().get("port").toString());
        server.startServer(Integer.parseInt(this.getDictionary().get("port").toString()));
    }

    @Stop
    public void stopRestGroup() {
        server.stop();
    }

    @Override
    public void triggerModelUpdate() {
        //NOOP
        //FORWARD TO ALL CONNECTED NODE
        //TODO
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
            
            System.out.println("port=>"+PORT);
            
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

    @Override
    public ContainerRoot pull(String targetNodeName) {
        //TODO SEARCH IN MODEL FOR
        String localhost = "127.0.0.1";
        String port = "8000";
        try {
            URL url = new URL("http://" + localhost + ":" + port + "/model/current");
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(2000);
            InputStream inputStream = conn.getInputStream();
            return KevoreeXmiHelper.loadStream(inputStream);
        } catch (IOException e) {
            logger.error("error while pulling model for name "+targetNodeName,e);
        }
        return null;
    }
}
