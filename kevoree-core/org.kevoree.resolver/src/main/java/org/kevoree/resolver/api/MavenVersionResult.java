package org.kevoree.resolver.api;


/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/05/13
 * Time: 11:43
 */
public class MavenVersionResult {

    private String value;
    private String lastUpdate;

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUrl_origin() {
        return url_origin;
    }

    public void setUrl_origin(String url_origin) {
        this.url_origin = url_origin;
    }

    private String url_origin;


    public boolean isPrior(MavenVersionResult remote) {
        try {

            return Long.parseLong(lastUpdate) < Long.parseLong(remote.lastUpdate);
        } catch (Exception e) {
            org.kevoree.log.Log.error("Bad artefact timestamp",e);
            return false;
        }
    }

    public String toString(){
        return value+"@"+url_origin;
    }

    private boolean notDeployed = false;

    public boolean isNotDeployed() {
        return notDeployed;
    }

    public void setNotDeployed(boolean notDeployed) {
        this.notDeployed = notDeployed;
    }
}
