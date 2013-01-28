package org.kevoree.library.javase.controlbasicGossiper;

import controlbasicGossiper.HelperModelSigned;
import jexxus.client.ClientConnection;
import jexxus.client.UniClientConnection;
import jexxus.common.Connection;
import jexxus.common.ConnectionListener;
import jexxus.common.Delivery;
import jexxus.server.Server;
import jexxus.server.ServerConnection;
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.Instance;
import org.kevoree.KControlModel.KControlRule;
import org.kevoree.adaptation.control.api.ControlException;
import org.kevoree.adaptation.control.api.SignedModel;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractGroupType;
import org.kevoree.framework.KevoreePropertyHelper;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.NetworkHelper;
import org.kevoree.kompare.JavaSePrimitive;
import org.kevoree.library.NodeNetworkHelper;
import org.kevoree.tools.control.framework.ControlFactory;
import org.kevoree.tools.control.framework.api.IAccessControlChecker;
import org.kevoree.tools.control.framework.command.CreateRulesCommand;
import org.kevoree.tools.control.framework.command.CreateSignatureCommand;
import org.kevoree.tools.control.framework.impl.SignedModelImpl;
import org.kevoree.tools.control.framework.utils.HelperMatcher;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import sun.security.rsa.RSAPublicKeyImpl;
import java.io.*;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
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
        @DictionaryAttribute(name = "modulus", defaultValue = "113516234051956049432409187188825393502984619772956207385345685738666746838534603335798592032015399622572296950837820797405103032010371993257571113176479404016660247843515233397619352905415148266464061237370342391412374444151093637080303229352033181632645308355392280045548592503992366806190035989980710579393", optional = false),
        @DictionaryAttribute(name = "public_exponent", defaultValue = "65537", optional = true),
        @DictionaryAttribute(name = "private_exponent", defaultValue = "21315146806283033161652615431675012473072138348200239377512916500603216299113582078067923059431794371963542522193726028546732348321095529991114486759384092703814541510187115924048468004015237870413922406207019743559010600414146667882946750781755445400385375683434026824013704183952393040334248195815289551457", optional = true),
        @DictionaryAttribute(name = "port", defaultValue = "8000", optional = true, fragmentDependant = true),
        @DictionaryAttribute(name = "ssl", defaultValue = "false", vals = {"true", "false"})
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

    private  IAccessControlChecker accessControl;


    @Start
    public void startRestGroup() throws IOException, NoSuchAlgorithmException, ControlException, InvalidKeyException {
        port = Integer.parseInt(this.getDictionary().get("port").toString());
        ssl = Boolean.parseBoolean(this.getDictionary().get("ssl").toString());
        if (udp) {
            server = new Server(this, port, port, ssl);
        } else {
            server = new Server(this, port, ssl);
        }
        logger.info("BasicGroup listen on " + port + "-SSL=" + ssl);
        server.startServer();
        starting = true;

           // todo
        accessControl = ControlFactory.createAccessControlChecker();

        BigInteger exponent = new BigInteger(getDictionary().get("public_exponent").toString());
        BigInteger modulus = new BigInteger(getDictionary().get("modulus").toString());
        RSAPublicKey default_key = new RSAPublicKeyImpl(modulus,exponent);

        CreateRulesCommand rules = new CreateRulesCommand(default_key);
        rules.setAccessControl(accessControl);


        KControlRule r1 = rules.addAuthorizedMatcher("typeDefinitions[FakeConsole]");
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r1.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r2 = rules.addAuthorizedMatcher("typeDefinitions[BasicGroup]");
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r2.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));


        KControlRule r3 = rules.addAuthorizedMatcher("typeDefinitions[Grapher]");
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r3.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        KControlRule r4 = rules.addAuthorizedMatcher("typeDefinitions[NioChannel]");
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StopInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.StartInstance()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.AddFragmentBinding()));
        r4.addMatcher(HelperMatcher.createMatcher(JavaSePrimitive.UpdateDictionaryInstance()));

        rules.execute();

    }

    @Stop
    public void stopRestGroup() {
        server.shutdown();
    }


    protected void locaUpdateModel(final ContainerRoot modelOption) {
        new Thread() {
            public void run() {
                getModelService().unregisterModelListener(AccessControlGroup.this);
                getModelService().atomicUpdateModel(modelOption);
                getModelService().registerModelListener(AccessControlGroup.this);
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
            for (ContainerNode subNode : group.getSubNodesForJ()) {
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

    @Override
    public void push(ContainerRoot model, String targetNodeName) throws Exception
    {
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
        Option<Group> groupOption = model.findByQuery("groups[" + getName() + "]", Group.class);
        if (groupOption.isDefined()) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption.get(), "port", true, targetNodeName);
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

        SignedModel signedmodel = new SignedModelImpl(getModelService().getLastModel());
        // create a signature
        CreateSignatureCommand c = new CreateSignatureCommand();
        c.setSignedModel(signedmodel);

        BigInteger exponent = new BigInteger(getDictionary().get("public_exponent").toString());
        BigInteger modulus = new BigInteger(getDictionary().get("modulus").toString());
        RSAPublicKey publicKey_default = new RSAPublicKeyImpl(modulus,exponent);


        KeySpec spec =	  new RSAPrivateKeySpec(modulus, new BigInteger(getDictionary().get("private_exponent").toString()));
        RSAPrivateKey privateKey_default = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);


        KeyPair keypair =  new KeyPair(publicKey_default,privateKey_default);
        c.setKey(keypair);
        c.execute();


        ObjectOutputStream oos= new ObjectOutputStream(output);

        try
        {
            oos.writeObject(signedmodel);
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

    @Override
    public ContainerRoot pull(final String targetNodeName) throws Exception {
        ContainerRoot model = getModelService().getLastModel();
        String ip = "127.0.0.1";
        Option<String> ipOption = NetworkHelper.getAccessibleIP(KevoreePropertyHelper.getNetworkProperties(model, targetNodeName, org.kevoree.framework.Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP()));
        if (ipOption.isDefined()) {
            ip = ipOption.get();
        }
        int PORT = 8000;
        Option<Group> groupOption = model.findByQuery("groups[" + getName() + "]", Group.class);
        if (groupOption.isDefined()) {
            Option<String> portOption = KevoreePropertyHelper.getProperty(groupOption.get(), "port", true, targetNodeName);
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
                final ContainerRoot root = KevoreeXmiHelper.loadCompressedStream(inputStream);
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
                        KevoreeXmiHelper.saveCompressedStream(output, getModelService().getLastModel());
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
                            SignedModelImpl       signedModel = (SignedModelImpl) ois.readObject();
                            ContainerRoot target_model = KevoreeXmiHelper.loadString(new String(signedModel.getSerialiedModel()));

                            List<AdaptationPrimitive> result =     accessControl.approval(getNodeName(), getModelService().getLastModel(), signedModel);

                            if(result.size() == 0)
                            {
                                logger.info("model accepted according to access control");
                                locaUpdateModel(target_model);
                            }else
                            {
                                for(AdaptationPrimitive p : result)
                                {
                                    logger.error("Refused Adapation Primitive " + p.getPrimitiveType().getName() + " " + ((Instance) p.getRef()).getName());
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
}
