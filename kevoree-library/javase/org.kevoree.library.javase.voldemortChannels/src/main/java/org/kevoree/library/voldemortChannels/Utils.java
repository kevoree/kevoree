package org.kevoree.library.voldemortChannels;

import org.apache.commons.io.FileUtils;
import org.kevoree.extra.voldemort.KUtils;
import voldemort.server.VoldemortConfig;
import voldemort.utils.Props;

import java.io.File;
import java.io.IOException;

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 18/04/12
 * Time: 10:08
 */
public class Utils {

    public static VoldemortConfig createServerConfig(int nodeId) throws IOException
        {
            Props props = new Props();
            props.put("node.id", nodeId);
            props.put("voldemort.home", KUtils.createTempDir() + "/node-" + nodeId);

            props.put("bdb.cache.size", 1 * 1024 * 1024);
            props.put("jmx.enable", "false");
            VoldemortConfig config = new VoldemortConfig(props);


            // clean and reinit metadata dir.
            File tempDir = new File(config.getMetadataDirectory());
            tempDir.mkdirs();

            File tempDir2 = new File(config.getDataDirectory());
            tempDir2.mkdirs();

            //    FileUtils.copyFileToDirectory(new File(this.getClass().getClassLoader().getResource("config/one-node-cluster.xml").getPath()), tempDir);
            FileUtils.copyFileToDirectory(new File(Utils.class.getClassLoader().getResource("config/stores.xml").getPath()), tempDir);
            return config;
        }
    


}
