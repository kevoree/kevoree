package org.kevoree.library.javase.nodejs;

import de.flapdoodle.embed.nodejs.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/08/12
 * Time: 22:50
 */
public class Tester {

    public static void main(String[] args) throws IOException, InterruptedException {
        NodejsProcess node = null;
        NodejsRuntimeConfig runtimeConfig = new NodejsRuntimeConfig();
        ExtNodejsDownloadConfig dwlConfig = new ExtNodejsDownloadConfig();
        dwlConfig.setPackageResolver(new ExtNodejsPaths());
        runtimeConfig.setDownloadConfig(dwlConfig);


        List<String> params = new ArrayList<String>();
        params.add("port=8066");

        //NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.V0_8_6, "/Users/duke/Documents/dev/sandbox/etherpad-lite/node_modules/ep_etherpad-lite/node/server.js", params, "/Users/duke/Documents/dev/sandbox/etherpad-lite");
        NodejsConfig nodejsConfig = new NodejsConfig(NodejsVersion.V0_8_6, "/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-corelibrary/javase/org.kevoree.library.javase.nodeJS/src/main/resources/tester.js", params, "/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-corelibrary/javase/org.kevoree.library.javase.nodeJS/src/main/resources");
        NodejsStarter runtime = new NodejsStarter(runtimeConfig);
        NodejsExecutable nodeExecutable = runtime.prepare(nodejsConfig);
        node = nodeExecutable.start();
       // Thread.sleep(5000);

       // node.stop();

        node.waitFor();

    }

}
