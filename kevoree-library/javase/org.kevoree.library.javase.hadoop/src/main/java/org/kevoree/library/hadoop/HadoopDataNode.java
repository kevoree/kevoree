package org.kevoree.library.hadoop;

import java.io.IOException;
import org.slf4j.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;

import org.kevoree.annotation.*;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;

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
    private String name;
    
    
    public HadoopDataNode() {
        this.name = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    }
    
    
    
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
        /*
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
         * */
        
        /* Proeprties to set
        -Dhadoop.tmp.dir=$DN_DIR_PREFIX$DN\
        -Ddfs.datanode.address=0.0.0.0:5001$DN \
        -Ddfs.datanode.http.address=0.0.0.0:5008$DN \
        -Ddfs.datanode.ipc.address=0.0.0.0:5002$DN"
        */
        
        Configuration configuration = this.getConfiguration();
        
        String fileSystem = String.format("hdfs://%s:%s", this.hostName(),
                configuration.get("hadoop.namenode.port"));
        
        String baseDir = String.format("/Users/sunye/Work/hadoop/%s-dfs/", name);
              
        
        String nameDir = baseDir + "name/";
        String dataDir = baseDir + "data/";

        

        
        if (name.length() > 2) {
            name = name.substring(3);
        }
        
        configuration.set("hadoop.tmp.dir",baseDir);
        configuration.set("dfs.name.dir", nameDir);
        configuration.set("dfs.data.dir", dataDir);
        configuration.set("dfs.datanode.address",hostName()+":50"+name);
        configuration.set("dfs.datanode.http.address",hostName()+":51"+name);
        configuration.set("dfs.datanode.ipc.address",hostName()+":52"+name);
        configuration.set("fs.default.name", fileSystem);
        

        HadoopConfiguration.removeDir(new File(baseDir)); 
        new File(nameDir).mkdirs();
        new File(dataDir).mkdirs();
        
        new Thread() {

            @Override
            public void run() {
                try {
                    dataNode = DataNode.createDataNode(DATANODE_ARGS, getConfiguration());
                    System.out.println("DataNode connected with NameNode: "+
                    dataNode.getNamenode());
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
               
            }
        }.start();
        


        

    }

    @Stop
    public void stop() throws IOException {

        dataNode.shutdown();

    }
    
    public static void main(String[] args) throws Exception {

        HadoopDataNode node = new HadoopDataNode();
        node.start();
  
    }
}
