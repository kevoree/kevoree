package org.kevoree.library.nanohttp;

import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.library.javase.nanohttp.NodeNetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 16/02/12
 * Time: 09:37
 */

@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true)
})
@GroupType
@Library(name = "JavaSE", names = "Android")
public class NanoRestGroup extends AbstractGroupType {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected NanoHTTPD server = null;
    //	private ModelSerializer modelSaver = new ModelSerializer();
    private KevoreeModelHandlerService handler = null;
    private boolean starting;

    private int port;

    ExecutorService poolUpdate = Executors.newSingleThreadExecutor();
    final NanoRestGroup self = this;

    @Start
    public void startRestGroup() throws IOException {
        poolUpdate = Executors.newSingleThreadExecutor();
        handler = this.getModelService();
        port = Integer.parseInt(this.getDictionary().get("port").toString());
        Object addressObject = this.getDictionary().get("ip");
        String address = "0.0.0.0";
        if (addressObject != null) {
            address = addressObject.toString();
        }
//		final NanoRestGroup self = this;
        server = new NanoHTTPD(new InetSocketAddress(InetAddress.getByName(address), port)) {
            //        server = new NanoHTTPD(port) {
            @Override
            public Response serve(String uri, String method, Properties header, Properties parms, Properties files, InputStream body) {
                if ("POST".equals(method)) {
                    if (uri.endsWith("/model/current") || uri.endsWith("/model/current/zip")) {
                        try {
                            logger.debug("Model receive, process to load");
                            ContainerRoot model = null;
                            if (uri.endsWith("zip")) {
                                model = KevoreeXmiHelper.$instance.loadCompressedStream(body);
                                logger.debug("Load  model From ZIP Stream");
                            } else {
                                model = KevoreeXmiHelper.$instance.loadStream(body);
                                logger.debug("Load  model From XMI Stream");
                            }
                            body.close();
                            logger.debug("Model loaded,send to core");
                            String srcNodeName = "";
                            Boolean externalSender = true;
                            Enumeration e = parms.propertyNames();
                            while (e.hasMoreElements()) {
                                String value = (String) e.nextElement();
                                if (value.endsWith("nodesrc")) {
                                    srcNodeName = parms.getProperty(value);
                                }
                            }
                            for (ContainerNode subNode : getModelElement().getSubNodes()) {
                                if (subNode.getName().trim().equals(srcNodeName.trim())) {
                                    logger.debug("model received from another node: forward is not needed");
                                    externalSender = false;
                                }
                            }

							/*//DO NOT NOTIFY ALL WHEN REC FROM THIS GROUP
                            final Boolean finalexternalSender = externalSender;
							final ContainerRoot finalModel = model;
							Runnable t = new Runnable() {
								@Override
								public void run () {
									if (!finalexternalSender) {
										getModelService().unregisterModelListener(self);
									}
									handler.atomicUpdateModel(finalModel);
									if (!finalexternalSender) {
										getModelService().registerModelListener(self);
									}
								}
							};
							poolUpdate.submit(t);*/
                            processOnModelReceived(externalSender, model);

                            return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, "<ack nodeName=\"" + getNodeName() + "\" />");
                        } catch (Exception e) {
                            logger.error("Error while loading model ", e);
                            return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_HTML, "Error while uploading model");
                        }
                    }
                } else if ("GET".equals(method)) {                    /*if (uri.endsWith("/model/current")) {
                        String msg = KevoreeXmiHelper.saveToString(handler.getLastModel(), false);
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, msg);
					}
					if (uri.endsWith("/model/current/zip")) {
						ByteArrayOutputStream st = new ByteArrayOutputStream();
						KevoreeXmiHelper.saveCompressedStream(st, handler.getLastModel());
						ByteArrayInputStream resultStream = new ByteArrayInputStream(st.toByteArray());
						return new NanoHTTPD.Response(HTTP_OK, MIME_HTML, resultStream);
					}*/
                    return processOnModelRequested(uri);
                }
                return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_XML, "ONLY GET OR POST METHOD SUPPORTED");
            }
        };

        //logger.info("Rest service start on port ->" + port);
        starting = true;

    }

    protected void processOnModelReceived(boolean externalSender, ContainerRoot model) {
        //DO NOT NOTIFY ALL WHEN REC FROM THIS GROUP
        final Boolean finalexternalSender = externalSender;
        final ContainerRoot finalModel = model;
        Runnable t = new Runnable() {
            @Override
            public void run() {
                if (!finalexternalSender) {
                    getModelService().unregisterModelListener(self);
                }
                handler.atomicUpdateModel(finalModel);
                if (!finalexternalSender) {
                    getModelService().registerModelListener(self);
                }
            }
        };
        poolUpdate.submit(t);
    }

    protected NanoHTTPD.Response processOnModelRequested(String uri) {
        if (uri.endsWith("/model/current")) {
            String msg = KevoreeXmiHelper.$instance.saveToString(handler.getLastModel(), false);
            return server.new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, msg);
        } else if (uri.endsWith("/model/current/zip")) {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            KevoreeXmiHelper.$instance.saveCompressedStream(st, handler.getLastModel());
            ByteArrayInputStream resultStream = new ByteArrayInputStream(st.toByteArray());
            return server.new Response(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, resultStream);
        } else {
            return server.new Response(NanoHTTPD.HTTP_BADREQUEST, null, "");
        }
    }

    @Stop
    public void stopRestGroup() {
        poolUpdate.shutdownNow();
        server.stop();
    }

    @Update
    public void update() throws IOException {
        if (!this.getDictionary().get("port").toString().equals("" + port)) {
            stopRestGroup();
            startRestGroup();
        }
    }

    @Override
    public void triggerModelUpdate() {
        if (starting) {
            final Option<ContainerRoot> modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
            if (modelOption.isDefined()) {
                final NanoRestGroup self = this;
                new Thread() {
                    public void run() {
                        getModelService().unregisterModelListener(self);
                        getModelService().atomicUpdateModel(modelOption.get());
                        getModelService().registerModelListener(self);
                    }
                }.start();
            }
            starting = false;
        } else {
            Group group = getModelElement();
            for (ContainerNode subNode : group.getSubNodes()) {
                if (!subNode.getName().equals(this.getNodeName())) {
                    try {
                        internalPush(getModelService().getLastModel(), subNode.getName(), this.getNodeName());
                    } catch (Exception e) {
                        logger.warn("Unable to notify other members of {} group", group.getName());
                    }
                }
            }
        }
    }

    @Override
    public void push(ContainerRoot model, String targetNodeName) throws Exception {
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(this.getModelService().getLastModel(), targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }


        Group groupOption = model.findByQuery("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
//		List<String> ips = KevoreePropertyHelper.getStringNetworkProperties(model, targetNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            int PORT = 8000;
            if (portOption.isDefined()) {
                try {
                    PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e) {
                    logger.warn("Attribute \"port\" of {} is not an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
//		boolean sent = false;
//		for (String ip : ips) {
            logger.debug("try to send model on url=>" + "http://" + ip + ":" + PORT + "/model/current");
            try {
                sendModel(model, "http://" + ip + ":" + PORT + "/model/current");
            } catch (Exception e) {
                logger.debug("try to send model on url=>" + "http://127.0.0.1:" + PORT + "/model/current");
                sendModel(model, "http://127.0.0.1:" + PORT + "/model/current");
            }
        } else {

        }
    }

    protected void internalPush(ContainerRoot model, String targetNodeName, String sender) throws Exception {
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(this.getModelService().getLastModel(), targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        Group groupOption = model.findByQuery("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            int PORT = 8000;
            if (portOption.isDefined()) {
                try {
                    PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e) {
                    logger.warn("Attribute \"port\" of {} is not an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
            logger.debug("try to send model on url=>" + "http://" + ip + ":" + PORT + "/model/current?nodesrc=" + sender);
            sendModel(model, "http://" + ip + ":" + PORT + "/model/current?nodesrc=" + sender);
        }
    }

    private void sendModel(ContainerRoot model, String urlPath) throws Exception {

        String urlPath2 = urlPath;
        if (urlPath2.contains("?")) {
            urlPath2 = urlPath2.replace("?", "/zip?");
        } else {
            urlPath2 = urlPath2 + "/zip";
        }
        try {
            sendModel(model, urlPath2, true);
        } catch (Exception e) {
            logger.debug("Unable to push a model on {}", urlPath);
            sendModel(model, urlPath, false);
        }

    }

    private void sendModel(ContainerRoot model, String urlPath, Boolean zip) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        if (zip) {
            KevoreeXmiHelper.$instance.saveCompressedStream(outStream, model);
        } else {
            KevoreeXmiHelper.$instance.saveStream(outStream, model);
        }
        outStream.flush();
        logger.debug("Try URL PATH " + urlPath);
        URL url = new URL(urlPath);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setDoOutput(true);
        conn.getOutputStream().write(outStream.toByteArray());
        conn.getOutputStream().close();
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = rd.readLine();
        while (line != null) {
            line = rd.readLine();
        }
        rd.close();
    }

    @Override
    public ContainerRoot pull(String targetNodeName) throws Exception {
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(this.getModelService().getLastModel(), targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }

        Option<String> portOption = KevoreePropertyHelper.getProperty(getModelElement(), "port", true, targetNodeName);
        int PORT = 8000;
        if (portOption.isDefined()) {
            try {
                PORT = Integer.parseInt(portOption.get());
            } catch (NumberFormatException e) {
                logger.warn("Attribute \"port\" of {} is not an Integer. Default value ({}) is used", getName(), PORT);
            }
        }
//		for (String ip : ips) {
        logger.debug("try to pull model on url=>" + "http://" + ip + ":" + PORT + "/model/current");
        ContainerRoot model = pullModel("http://" + ip + ":" + PORT + "/model/current");
        if (model == null) {
            model = pullModel("http://127.0.0.1:" + PORT + "/model/current");
        }
//		}

//		if (model == null) {
//			logger.debug("Unable to pull a model on " + targetNodeName);
//			return null;
//		} else {
//			return model;
//		}
        return model;
    }

    private ContainerRoot pullModel(String urlPath) throws Exception {
        ContainerRoot model = null;
        model = pullModel(urlPath + "/zip", true);
        if (model == null) {
            model = pullModel(urlPath, false);
        }
        if (model == null) {
            throw new Exception("Unable to pull model");
        }
        return model;
    }

    private ContainerRoot pullModel(String urlPath, Boolean zip) throws Exception {
        URL url = new URL(urlPath);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(2000);
        InputStream inputStream = conn.getInputStream();
        if (zip) {
            return KevoreeXmiHelper.$instance.loadCompressedStream(inputStream);
        } else {
            return KevoreeXmiHelper.$instance.loadStream(inputStream);
        }
    }

    @Override
    public void preRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
    }

    @Override
    public void postRollback(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
    }
}
