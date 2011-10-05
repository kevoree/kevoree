package org.kevoree.library.hadoop;

import java.io.IOException;
import java.util.logging.Level;
import org.slf4j.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;

import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.annotation.*;
import org.kevoree.framework.KevoreePlatformHelper;
import org.kevoree.framework.Constants;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "nameNodeName", optional = false)
})
public class HadoopDataNode extends HadoopComponent {

    private static final Logger LOG = LoggerFactory.getLogger(HadoopDataNode.class.getName());
    private String nameNodeName = "";
    private DataNode dataNode;
    private final static String[] DATANODE_ARGS = {"-rollback"};

    /**
     * @TODO: Retrieve and set Name Node address;
     * 
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    @Start
    public void start() throws IOException, InterruptedException {

        nameNodeName = (String) this.getDictionary().get("nameNodeName");
        String nameNodeNodeName = "";


        /*
         * @FIXME : use while instead of foreach
         */
        for (ContainerNode each : this.getModelService().getLastModel().getNodes()) {
            for (ComponentInstance ci : each.getComponents()) {
                if (nameNodeName.equals(ci.getName())) {
                    nameNodeNodeName = each.getName();
                    break;
                }
            }
        }

        // retrieve NameNode IP address
        String ip = KevoreePlatformHelper.getProperty(this.getModelService().getLastModel(),
                nameNodeNodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP());


        Configuration configuration = this.getConfiguration();
        configuration.set("hadoop.namenode", ip);


        new Thread() {

            @Override
            public void run() {
                try {
                    dataNode = DataNode.createDataNode(DATANODE_ARGS, getConfiguration());
                    LOG.info("DataNode connected with NameNode: {0}",
                    dataNode.getNamenode());
                }
                catch (IOException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
               
            }
        }.start();
        


        

    }

    @Stop
    public void stop() throws IOException {

        dataNode.shutdown();

    }
}
