package org.kevoree.library.rest;

import nanohttpd.NanoHTTPD;
import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.*;
import org.kevoree.framework.Constants;
import org.kevoree.serializer.ModelSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 11/10/11
 * Time: 18:27
 */


@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true)
})
@GroupType
@Library(name = "Android")
public class RestGroup extends AbstractGroupType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private NanoHTTPD server = null;
    private ModelSerializer modelSaver = new ModelSerializer();
    private KevoreeModelHandlerService handler = null;

    @Start
    public void startRestGroup() throws IOException {
        handler = this.getModelService();
        int port = Integer.parseInt(this.getDictionary().get("port").toString());
        server = new NanoHTTPD(port) {
            public Response serve(String uri, String method, Properties header, Properties parms, Properties files, String body) {
                if (method.equals("POST")) {
                    try {
                        ContainerRoot model = KevoreeXmiHelper.loadString(body.trim());
                        handler.updateModel(model);
                        return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
                    } catch (Exception e) {
                        logger.error("Error while loading model");
                        return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_HTML, "Error while uploading model");
                    }


                }
                if (method.equals("GET")) {
                    String msg = KevoreeXmiHelper.saveToString(handler.getLastModel(), false);
                    return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
                }
                return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_XML, "ONLY GET OR POST METHOD SUPPORTED");
            }
        };

        logger.warn("Rest service start on port ->" + port);
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

            System.out.println("port=>" + PORT);

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
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(), remoteNodeName,
                org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
        if (ip == null || ip.equals("")) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    public int parsePortNumber(String nodeName) throws IOException {
        try {
            //logger.debug("look for port on " + nodeName);
            return KevoreeFragmentPropertyHelper
                    .getIntPropertyFromFragmentChannel(this.getModelService().getLastModel(), this.getName(), "port",
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
}
