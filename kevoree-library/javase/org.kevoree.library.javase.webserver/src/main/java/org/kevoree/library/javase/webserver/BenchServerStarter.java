package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.annotation.Update;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.kevoree.framework.AbstractComponentType;

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

                    for (int i = 0; i < 5; i++) {
                        KevScriptEngine scriptEngine = getKevScriptEngineFactory().createKevScriptEngine();
                        nbWebServer++;
                        scriptEngine.addVariable("websrvname", "WebServer" + nbWebServer);
                        scriptEngine.addVariable("helloPageName", "HelloPage" + nbWebServer);
                        scriptEngine.addVariable("s2pname", "s2p" + nbWebServer);
                        scriptEngine.addVariable("p2sname", "p2s" + nbWebServer);
                        scriptEngine.append("addComponent {websrvname}@{nodename} : WebServer { 'port'='" + (basePort + nbWebServer) + "' } ");
                        scriptEngine.append("addComponent {helloPageName}@{nodename} : HelloWorldPage  ");
                        scriptEngine.append("addChannel {s2pname} : defMSG");
                        scriptEngine.append("addChannel {p2sname} : defMSG");
                        //BIND
                        scriptEngine.append("bind {websrvname}.handler@{nodename} => {s2pname}");
                        scriptEngine.append("bind {websrvname}.response@{nodename} => {p2sname}");
                        scriptEngine.append("bind {helloPageName}.request@{nodename} => {s2pname}");
                        scriptEngine.append("bind {helloPageName}.content@{nodename} => {p2sname}");
                        getModelService().atomicUpdateModel(scriptEngine.interpret());
                    }

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
