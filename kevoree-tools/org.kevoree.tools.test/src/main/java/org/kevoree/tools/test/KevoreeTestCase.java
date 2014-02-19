package org.kevoree.tools.test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.junit.After;
import org.kevoree.ContainerRoot;
import org.kevoree.loader.JSONModelLoader;
import org.kevoree.log.Log;
import org.kevoree.serializer.JSONModelSerializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by duke on 16/02/2014.
 */
public class KevoreeTestCase {

    private HashMap<String, KevoreePlatformCtrl> runners = new HashMap<String, KevoreePlatformCtrl>();

    private AtomicInteger integer = new AtomicInteger(2000);

    public void shutdown(String nodeName) throws Exception {
        if (runners.containsKey(nodeName)) {
            KevoreePlatformCtrl p = runners.get(nodeName);
            p.stop();
        } else {
            throw new Exception("Not started : " + nodeName);
        }
    }

    public void bootstrap(String nodeName) throws Exception {
        bootstrap(nodeName, null);
    }

    public void bootstrap(String nodeName, String bootfile) throws Exception {
        if (runners.containsKey(nodeName)) {
            throw new Exception("Already started : " + nodeName);
        }
        KevoreePlatformCtrl p = new KevoreePlatformCtrl(nodeName);
        p.setModelDebugPort(integer.getAndIncrement());
        runners.put(nodeName, p);
        p.start(bootfile);
        Log.info("Kevoree Platform started {}", nodeName);
    }

    @After
    public void tearDown() throws Exception {
        Log.info("Cleanup and stop every platforms");
        //shutdown all platforms
        for (String nodeName : runners.keySet()) {
            shutdown(nodeName);
        }
    }

    public ContainerRoot getCurrentModel(String nodeName) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            KevoreePlatformCtrl runner = runners.get(nodeName);
            URL url = new URL("http://localhost:" + runner.getModelDebugPort() + "/model");
            URLConnection conn = url.openConnection();
            JSONModelLoader loader = new JSONModelLoader();
            ContainerRoot model = (ContainerRoot) loader.loadModelFromStream(conn.getInputStream()).get(0);
            conn.getInputStream().close();
            return model;
        }
    }

    public boolean deploy(String nodeName, ContainerRoot model) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            KevoreePlatformCtrl runner = runners.get(nodeName);
            URL url = new URL("http://localhost:" + runner.getModelDebugPort() + "/model");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            JSONModelSerializer saver = new JSONModelSerializer();
            out.write(saver.serialize(model));
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                System.out.println(decodedString);
            }
            in.close();
        }
        return true;
    }

    public boolean exec(String nodeName, String script) throws Exception {
        if (!runners.containsKey(nodeName)) {
            throw new Exception("Node not started : " + nodeName);
        } else {
            KevoreePlatformCtrl runner = runners.get(nodeName);
            HttpResponse<String> response = Unirest.post("http://localhost:" + runner.getModelDebugPort() + "/script")
                    .field("script", script)
                    .asString();
            return Boolean.parseBoolean(response.getBody());
        }
    }

}
