package org.kevoree.platform.standalone.test;

import org.kevoree.ContainerRoot;
import org.kevoree.api.handler.UpdateCallback;
import org.kevoree.bootstrap.Bootstrap;
import org.kevoree.core.impl.KevoreeCoreBean;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Exchanger;

/**
 * Created by duke on 18/02/2014.
 */
public class App {

    public static void main(String[] args) throws Exception {
        org.kevoree.platform.standalone.App.main(args);
        Bootstrap bootstrap = org.kevoree.platform.standalone.App.bootstrap;
        final KevoreeCoreBean core = bootstrap.getCore();
        final JSONModelSerializer saver = new JSONModelSerializer();
        final JSONModelLoader loader = new JSONModelLoader();

        int port = 2000;
        if (System.getProperty("model.debug.port") != null) {
            port = Integer.parseInt(System.getProperty("model.debug.port"));
        }
        Log.info("Start management port on {}", port);

        NanoHTTPD http = new NanoHTTPD(port) {
            @Override
            public Response serve(IHTTPSession session) {
                if (session.getUri().startsWith("/model") && session.getMethod().equals(Method.GET)) {
                    return new NanoHTTPD.Response(saver.serialize(core.getCurrentModel().getModel()));
                }
                if (session.getUri().startsWith("/model") && session.getMethod().equals(Method.POST)) {
                    ContainerRoot root = (ContainerRoot) loader.loadModelFromStream(session.getInputStream()).get(0);
                    core.update(root, (UpdateCallback) null);
                    return new NanoHTTPD.Response("");
                }
                if (session.getUri().startsWith("/script") && session.getMethod().equals(Method.POST)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    try {
                        InputStream is = session.getInputStream();
                        while (is.available() > 0) {
                            stringBuffer.append((char) is.read());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String[] result = stringBuffer.toString().split("=");
                    String script = null;
                    if(result.length>1){
                        String payload = result[1];
                        script = URLDecoder.decode(payload);
                    }
                    final Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
                    core.submitScript(script, new UpdateCallback() {
                        @Override
                        public void run(Boolean aBoolean) {
                            try {
                                exchanger.exchange(aBoolean);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    try {
                        return new Response(exchanger.exchange(true)+"");
                    } catch (InterruptedException e) {
                        return new Response(false+"");
                    }
                }
                return new NanoHTTPD.Response("false");
            }
        };
        http.start();

    }

}
