package org.kevoree.library.hadoop;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.datanode.DataNode;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

/**
 * 
 * @author sunye
 */
@Library(name = "Hadoop")
@ComponentType
@Requires({
    @RequiredPort(name = "records", type = PortType.MESSAGE),
    @RequiredPort(name = "calibrations", type = PortType.MESSAGE)
})
@Provides({
    @ProvidedPort(name = "log", type = PortType.MESSAGE)
})
public class HadoopDataNode extends AbstractComponentType {

    private static final Logger LOG = Logger.getLogger(HadoopDataNode.class.getName());
    private DataNode dataNode;
    private final Configuration cfg;
    private final String dirName;
    private final String dirData;

    public HadoopDataNode(Configuration hdfsConf,
            String dirName, String dirData) {

        this.cfg = hdfsConf;
        this.dirName = dirName;
        this.dirData = dirData;
    }

    protected void start() throws IOException, InterruptedException {
        cfg.set("dfs.name.dir", dirName);
        cfg.set("dfs.data.dir", dirData);
        LOG.info("Starting DataNode!");

        String[] args = {"-rollback"};

        dataNode = DataNode.createDataNode(args, cfg);

        String serveraddr = dataNode.getNamenode();
        LOG.log(Level.INFO, "DataNode connected with NameNode: {0}",
                serveraddr);

    }

    protected void stop() throws IOException {

        dataNode.shutdown();

    }
}
