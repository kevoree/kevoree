package org.kevoree.library.javase.webserver;

import java.net.*;
import org.kevoree.*;
import org.kevoree.ComponentInstance;
import org.kevoree.ComponentType;
import org.kevoree.Port;
import org.kevoree.annotation.*;
import org.kevoree.framework.*;
import scala.*;
import scala.*;
import scala.*;

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 09/12/11
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */
@org.kevoree.annotation.DictionaryType({
        @org.kevoree.annotation.DictionaryAttribute(name = "startPort", defaultValue = "10000"),
        @org.kevoree.annotation.DictionaryAttribute(name = "maxInstanceLimit", optional = true)
})
@org.kevoree.annotation.ComponentType
public class BenchServerStarter extends AbstractComponentType {

    private int nbWebServer = 0;
    private int basePort = 10000;
    private int maxInstances = -5;


    private void startServer() {

        new Thread(new Runnable() {
            public void run() {


                System.out.println("Server Started");
                while ((nbWebServer / 5) != (maxInstances / 5)) {
                    
                    TypeDefinition webServerType = null, helloPageType = null, messageChannelType = null;
                    ContainerRoot model = BenchServerStarter.this.getModelService().getLastModel();

                    for (TypeDefinition td : model.getTypeDefinitionsForJ()) {
                        if (td.getName().equals("WebServer")) {
                            webServerType = td;
                        } else if (td.getName().equals("HelloWorldPage")) {
                            helloPageType = td;
                        } else if (td.getName().equals("defMSG")) {
                            messageChannelType = td;
                        }
                    }
                    //System.out.println("WebServer:" + webServerType + " HelloWorldPage:" + helloPageType + " MessageChannel" + messageChannelType);

                    ContainerNode nodeVar = null;
                    for (ContainerNode node : model.getNodesForJ()) {
                        if (node.getName().equals(getNodeName())) {
                            nodeVar = node;
                        }
                    }
                    for (int i = 0; i < 5; i++) {
                        //System.out.println("Create WebServer");
                        ComponentInstance webServer = KevoreeFactory.createComponentInstance();
                        nodeVar.addComponents(webServer);

                        webServer.setTypeDefinition(webServerType);
                        webServer.setName("WebServer" + nbWebServer++);
                        Port srvReq = KevoreeFactory.createPort();
                        webServer.addRequired(srvReq);
                        srvReq.setPortTypeRef(((ComponentType) webServerType).getRequiredForJ().get(0));

                        Port srvProv = KevoreeFactory.createPort();
                        webServer.addProvided(srvProv);
                        srvProv.setPortTypeRef(((ComponentType) webServerType).getProvidedForJ().get(0));


                        Dictionary webServerDico = KevoreeFactory.createDictionary();
                        DictionaryValue webServerDicoValue = KevoreeFactory.createDictionaryValue();
                        webServerDicoValue.setValue("" + (basePort + nbWebServer));
                        webServerDicoValue.setAttribute(webServerType.getDictionaryType().get().getAttributesForJ().get(0));
                        webServerDico.addValues(webServerDicoValue);
                        webServer.setDictionary(new scala.Some(webServerDico));


                        //System.out.println("Create HelloPage");
                        ComponentInstance helloPage = KevoreeFactory.createComponentInstance();
                        nodeVar.addComponents(helloPage);
                        helloPage.setTypeDefinition(helloPageType);
                        helloPage.setName("HelloPage" + nbWebServer);

                        Port pageReq = KevoreeFactory.createPort();
                        helloPage.addRequired(pageReq);
                        pageReq.setPortTypeRef(((ComponentType) helloPageType).getRequiredForJ().get(0));


                        Port pageProv = KevoreeFactory.createPort();
                        helloPage.addProvided(pageProv);
                        pageProv.setPortTypeRef(((ComponentType) helloPageType).getProvidedForJ().get(0));


                        //System.out.println("Create ServerToPage");
                        Channel serverToPage = KevoreeFactory.createChannel();
                        model.addHubs(serverToPage);
                        serverToPage.setTypeDefinition(messageChannelType);
                        serverToPage.setName("s2p" + nbWebServer);


                        //System.out.println("Create PageToServer");
                        Channel pageToServer = KevoreeFactory.createChannel();
                        model.addHubs(pageToServer);
                        pageToServer.setTypeDefinition(messageChannelType);
                        pageToServer.setName("p2s" + nbWebServer);


                        //System.out.println("Create B1");
                        MBinding b1 = KevoreeFactory.createMBinding();
                        b1.setHub(serverToPage);
                        b1.setPort(srvReq);
                        model.addMBindings(b1);

                        //System.out.println("Create B2");
                        MBinding b2 = KevoreeFactory.createMBinding();
                        model.addMBindings(b2);
                        b2.setHub(serverToPage);
                        b2.setPort(pageProv);


                        //System.out.println("Create B3");
                        MBinding b3 = KevoreeFactory.createMBinding();
                        model.addMBindings(b3);
                        b3.setHub(pageToServer);
                        b3.setPort(srvProv);


                        //System.out.println("Create B4");
                        MBinding b4 = KevoreeFactory.createMBinding();
                        model.addMBindings(b4);
                        b4.setHub(pageToServer);
                        b4.setPort(pageReq);


                    }
                    KevoreeXmiHelper.save(URI.create("file:/tmp/model.kev").toString(), model);
                    getModelService().atomicUpdateModel(model);
                    System.out.println("Server " + (nbWebServer - 5) + " to " + nbWebServer + " started");
                }
            }
        }).start();
    }

    @Start
    public void start() {
        if (getDictionary().get("maxInstanceLimit") != null) {
            maxInstances = Integer.valueOf(getDictionary().get("maxInstanceLimit").toString());
        }
        if (getDictionary().get("startPort") != null) {
            basePort = Integer.valueOf(getDictionary().get("startPort").toString());
        }
        System.out.println("BasePort:" + basePort + " MaxInstances:" + maxInstances);
        startServer();

    }

    @Stop
    public void stop() {
    }

    @Update
    public void update() {
    }

}
