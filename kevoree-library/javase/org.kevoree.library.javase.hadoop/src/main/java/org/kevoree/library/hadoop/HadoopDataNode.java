package org.kevoree.library.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;

import org.eclipse.emf.common.util.EList;
import org.kevoree.ComponentInstance;
import org.kevoree.ContainerNode;
import org.kevoree.DictionaryValue;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * 
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
public class HadoopDataNode extends HadoopComponent {

    private static final Logger LOG = Logger.getLogger(HadoopDataNode.class.getName());
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
    protected void start() throws IOException, InterruptedException {

        EList<DictionaryValue> dictionary = null;
        Map<String, String> values = new HashMap<String, String>();

        for (ContainerNode each : this.getModelService().getLastModel().getNodes()) {
            for (ComponentInstance ci : each.getComponents()) {
                dictionary = ci.getDictionary().getValues(); 
                // find name of attribute then get its value. 
            }
        }

        for (DictionaryValue each : dictionary) {
            values.put(each.getAttribute().getName(), each.getValue());
        }

        
        Configuration configuration = this.getConfiguration();
        configuration.set("hadoop.namenode", null);
        
        dataNode = DataNode.createDataNode(DATANODE_ARGS, configuration);
        LOG.log(Level.INFO, "DataNode connected with NameNode: {0}",
                dataNode.getNamenode());

    }

    protected void stop() throws IOException {

        dataNode.shutdown();

    }
}
