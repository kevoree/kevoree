package org.kevoree.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 02/12/2013
 * Time: 14:55
 */
public class InstanceContext implements Context {

    private String path = null;
    private String nodeName = null;
    private String instanceName = null;

    public InstanceContext(String path, String nodeName, String instanceName) {
        this.path = path;
        this.nodeName = nodeName;
        this.instanceName = instanceName;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public String getInstanceName() {
        return instanceName;
    }
}
