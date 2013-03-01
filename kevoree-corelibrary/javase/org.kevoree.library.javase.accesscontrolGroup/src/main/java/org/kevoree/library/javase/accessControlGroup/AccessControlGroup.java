package org.kevoree.library.javase.accessControlGroup;

import controlbasicGossiper.HelperModelSigned;
import jexxus.client.ClientConnection;
import jexxus.client.UniClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;
import org.kevoree.AccessControl.*;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.Instance;

import org.kevoree.adaptation.accesscontrol.api.ControlException;

import org.kevoree.adaptation.accesscontrol.api.SignedModel;
import org.kevoree.adaptation.accesscontrol.api.SignedPDP;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.library.NodeNetworkHelper;

import org.kevoree.tools.accesscontrol.framework.api.ICompareAccessControl;
import org.kevoree.tools.accesscontrol.framework.impl.CompareAccessControlImpl;
import org.kevoree.tools.accesscontrol.framework.impl.SignedModelImpl;
import org.kevoree.tools.accesscontrol.framework.impl.SignedPDPImpl;
import org.kevoree.tools.accesscontrol.framework.utils.AccessControlXmiHelper;
import org.kevoree.tools.accesscontrol.framework.utils.HelperSignature;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import javax.swing.*;
import java.io.*;
import java.security.*;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created with IntelliJ IDEA.  :
 * User: jed
 * Date: 24/01/13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ip", defaultValue = "0.0.0.0", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ssl", defaultValue = "false", vals = {"true", "false"}) ,
        @DictionaryAttribute(name = "pdp", defaultValue = "false", vals = {"true", "false"}),
        @DictionaryAttribute(name = "benchmark", defaultValue = "false", vals = {"true", "false"}),
        @DictionaryAttribute(name = "gui", defaultValue = "false", vals = {"true", "false"})
})
@GroupType
@Library(name = "JavaSE", names = "Android")
public class AccessControlGroup extends AbstractGroupType implements ConnectionListener {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private final byte getModel = 0;
    private final byte pushModel = 1;
    protected Server server = null;
    private boolean starting;
    protected boolean udp = false;
    boolean ssl = false;
    int port = -1;
    private  AccessControlRoot root=null;


    @Start
    public void startRestGroup() throws IOException, NoSuchAlgorithmException, ControlException, InvalidKeyException {
        port = Integer.parseInt(this.getDictionary().get("port").toString());
        ssl = Boolean.parseBoolean(this.getDictionary().get("ssl").toString());

        // root = AccessControlXmiHelper.$instance.loadStream(Tester.class.getClassLoader().getResourceAsStream("model.ac"));

        if(Boolean.parseBoolean(getDictionary().get("gui").toString()))
        {
            JFileChooser dialogue = new JFileChooser(new File("."));
            PrintWriter sortie;
            File fichier=null;
            if (dialogue.showOpenDialog(null)==  JFileChooser.APPROVE_OPTION) {
                fichier = dialogue.getSelectedFile();
                sortie = new PrintWriter
                        (new FileWriter(fichier.getPath(), true));

                sortie.close();
                root =  AccessControlXmiHelper.$instance.loadStream(new FileInputStream(fichier));
            }

        }
        if (udp) {
            server = new Server(this, port, port, ssl);
        } else {
            server = new Server(this, port, ssl);
        }
        logger.info("BasicGroup listen on " + port + "-SSL=" + ssl);
        server.startServer();
        starting = true;
    }

    @Stop
    public void stopRestGroup() {
        server.shutdown();
    }


    protected void locaUpdateModel(final ContainerRoot modelOption) {
        new Thread() {
            public void run() {
                try {
                    long duree,start;
                    getModelService().unregisterModelListener(AccessControlGroup.this);
                    start = System.currentTimeMillis();
                    getModelService().atomicUpdateModel(modelOption);
                    duree =  (System.currentTimeMillis() - start) ;
                    getModelService().registerModelListener(AccessControlGroup.this);
                    /*
                    try
                    {
                        String filename= System.getProperty("java.io.tmpdir")+ File.separator+ "accesscontrol2.benchmark";
                        FileWriter fw = new FileWriter(filename,true); //the true will append the new data
                        fw.write(""+duree+"\n");
                        fw.close();
                        logger.info("total="+filename);
                    }
                    catch(IOException ioe)
                    {
                        System.err.println("IOException: " + ioe.getMessage());
                    }
                    */
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }.start();
    }

    @Override
    public void triggerModelUpdate() {
        if (starting) {

            final ContainerRoot modelOption = NodeNetworkHelper.updateModelWithNetworkProperty(this);
            if (modelOption != null) {
                new Thread() {
                    public void run() {
                        try {
                            getModelService().unregisterModelListener(AccessControlGroup.this);
                            getModelService().atomicUpdateModel(modelOption);
                            getModelService().registerModelListener(AccessControlGroup.this);
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }.start();
            }
            starting = false;
        } else {
            Group group = getModelElement();
            ContainerRoot currentModel = (ContainerRoot) group.eContainer();
            for (ContainerNode subNode : group.getSubNodes()) {
                if (!subNode.getName().equals(this.getNodeName())) {
                    try {
                        push(currentModel, subNode.getName());
                    } catch (Exception e) {
                        logger.warn("Unable to notify other members of {} group", group.getName());
                    }
                }
            }
        }
    }

    public void pushPDP(AccessControlRoot root,PrivateKey key,ContainerRoot model,String targetNodeName) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        SignedPDP pdp = new SignedPDPImpl(root,key);
        write(model,targetNodeName,pdp);
    }


    public void pushSignedModel(PrivateKey key,ContainerRoot model, String targetNodeName)  throws Exception
    {
        SignedModel signedmodel = new SignedModelImpl(model,key);
        write(model,targetNodeName,signedmodel);
    }



    @Override
    public void push(ContainerRoot model, String targetNodeName) throws Exception
    {
        String private_exponent="";
        String modulus="";
        if(Boolean.parseBoolean(getDictionary().get("gui").toString()))
        {
            JFileChooser dialogue = new JFileChooser(new File("."));
            PrintWriter sortie;
            File fichier=null;
            if (dialogue.showOpenDialog(null)==
                    JFileChooser.APPROVE_OPTION) {
                fichier = dialogue.getSelectedFile();
                sortie = new PrintWriter
                        (new FileWriter(fichier.getPath(), true));

                sortie.close();
            }
            FileReader fr = new FileReader (fichier);
            BufferedReader br = new BufferedReader (fr);
            StringBuilder stringkey = new StringBuilder();
            try
            {
                String line = br.readLine();

                while (line != null)
                {
                    stringkey.append(line);
                    line = br.readLine();
                }

                br.close();
                fr.close();
                private_exponent = stringkey.toString().split(":")[0];
                modulus = stringkey.toString().split(":")[1];
            }      catch (EOFException e ){

            }
        }

        SignedModel signedmodel = new SignedModelImpl(model,HelperSignature.getPrivateKey(modulus,private_exponent));
        write(model,targetNodeName,signedmodel);

    }

    @Override
    public ContainerRoot pull(final String targetNodeName) throws Exception {
        ContainerRoot model = getModelService().getLastModel();
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        int PORT = 8000;
        Group groupOption = model.findByPath("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            if (portOption.isDefined()) {
                try {
                    PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e){
                    logger.warn("Attribute \"port\" of {} must be an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
        }
        return requestModel(ip, PORT, targetNodeName);
    }

    protected ContainerRoot requestModel(String ip, int port, final String targetNodeName) throws IOException, TimeoutException, InterruptedException {
        final Exchanger<ContainerRoot> exchanger = new Exchanger<ContainerRoot>();
        final ClientConnection[] conns = new ClientConnection[1];
        conns[0] = new ClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
                conns[0].close();
                try {
                    exchanger.exchange(null);
                } catch (InterruptedException e) {
                    logger.error("", e);
                }
            }

            @Override
            public void receive(byte[] data, Connection from) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                final ContainerRoot root = KevoreeXmiHelper.$instance.loadCompressedStream(inputStream);
                try {
                    exchanger.exchange(root);
                } catch (InterruptedException e) {
                    logger.error("error while waiting model from " + targetNodeName, e);
                } finally {
                    conns[0].close();
                }
            }

            @Override
            public void clientConnected(ServerConnection conn) {
            }

        }, ip, port, ssl);
        conns[0].connect(5000);
        byte[] data = new byte[1];
        data[0] = getModel;
        conns[0].send(data, Delivery.RELIABLE);
        return exchanger.exchange(null, 5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void connectionBroken(Connection broken, boolean forced) {
    }

    @Override
    public void receive(byte[] data, Connection from) {
        try {
            if (data == null) {
                logger.error("Null rec");
            } else {
                switch (data[0]) {
                    case getModel: {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        KevoreeXmiHelper.$instance.saveCompressedStream(output, getModelService().getLastModel());
                        from.send(output.toByteArray(), Delivery.RELIABLE);
                    }
                    break;
                    case pushModel: {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
                        inputStream.read();

                        byte [] bytessignedModel = HelperModelSigned.loadSignedModelStream(inputStream);
                        ByteArrayInputStream bis = new ByteArrayInputStream(bytessignedModel);
                        ObjectInputStream ois= new ObjectInputStream(bis);

                        try
                        {

                            Object signed =    ois.readObject();

                            if(signed instanceof SignedModelImpl) {

                                SignedModel signedModel = (SignedModelImpl)signed;
                                if(root != null)
                                {
                                    CompareAccessControlImpl accessControl =    new CompareAccessControlImpl(root);
                                    if(Boolean.parseBoolean(getDictionary().get("benchmark").toString()))
                                    {
                                        accessControl.setBenchmark(true);
                                    } else {
                                        accessControl.setBenchmark(false);
                                    }
                                    List<AdaptationPrimitive> result =     accessControl.approval(getNodeName(), getModelService().getLastModel(), signedModel);

                                    if(result != null && result.size() == 0)
                                    {
                                        logger.info("model accepted according to access control");
                                        ContainerRoot target_model = KevoreeXmiHelper.$instance.loadString(new String(signedModel.getSerialiedModel()));
                                        locaUpdateModel(target_model);
                                    }else
                                    {
                                        if(result != null){
                                            for(AdaptationPrimitive p : result)
                                            {

                                                String ref="";

                                                if(p.getRef() instanceof Instance){
                                                    ref = ((Instance)p.getRef()).getTypeDefinition().getName();
                                                }   else
                                                {
                                                    ref =  p.getRef().toString();
                                                }
                                                logger.error("Refused Adapation Primitive " + p.getPrimitiveType().getName() + " " + ref);


                                            }
                                        }  else {
                                            logger.error(" no result ");
                                        }

                                    }
                                }else
                                {
                                    logger.error("There is no access control defined");
                                }

                            } else if( signed instanceof SignedPDPImpl)
                            {
                                SignedPDPImpl pdp = (SignedPDPImpl)signed;

                                if(root == null)
                                {
                                    root = AccessControlXmiHelper.$instance.loadString(new String(pdp.getSerialiedModel()));
                                }  else
                                {

                                    ICompareAccessControl accessControl =    new CompareAccessControlImpl(root);
                                    if(accessControl.accessPDP(pdp))
                                    {
                                        root = AccessControlXmiHelper.$instance.loadString(new String(pdp.getSerialiedModel()));
                                    }else {
                                        logger.error("There is no acess to PDP");

                                    }

                                }

                            }
                        } finally {
                            // on ferme les flux
                            try {
                                ois.close();
                            } finally {
                                bis.close();
                            }
                        }
                    }
                    break;
                    default:
                        externalProcess(data, from);
                }
            }
        } catch (Exception e) {
            logger.error("Something bad ...", e);
        }

    }

    protected void externalProcess(byte[] data, Connection from) {
        from.close();
    }


    @Override
    public void clientConnected(ServerConnection conn) {

    }



    public void write(ContainerRoot model,String targetNodeName,Object data) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(pushModel);
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        } else {
            logger.warn("No addr, found default local");
        }

        int PORT = 8000;
        Group groupOption = model.findByPath("groups[" + getName() + "]", Group.class);
        if (groupOption!=null) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption, "port", true, targetNodeName);
            if (portOption.isDefined()) {
                try {
                    PORT = Integer.parseInt(portOption.get());
                } catch (NumberFormatException e){
                    logger.warn("Attribute \"port\" of {} must be an Integer. Default value ({}) is used", getName(), PORT);
                }
            }
        }

        final UniClientConnection[] conns = new UniClientConnection[1];
        conns[0] = new UniClientConnection(new ConnectionListener() {
            @Override
            public void connectionBroken(Connection broken, boolean forced) {
            }

            @Override
            public void receive(byte[] data, Connection from) {
            }

            @Override
            public void clientConnected(ServerConnection conn) {
            }
        }, ip, PORT, ssl);



        ObjectOutputStream oos= new ObjectOutputStream(output);

        try
        {
            oos.writeObject(data);
            oos.flush();
            conns[0].connect(5000);
            conns[0].send(output.toByteArray(), Delivery.RELIABLE);
        } finally
        {

            try {
                oos.close();
            } finally {
                output.close();
            }
        }
    }

}
