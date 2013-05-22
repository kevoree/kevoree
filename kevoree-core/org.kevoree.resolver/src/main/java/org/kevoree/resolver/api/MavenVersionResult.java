package org.kevoree.resolver.api;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 21/05/13
 * Time: 11:43
 */
public class MavenVersionResult {

    private String timestamp;
    private String buildNumber;

    public String getUrl_origin() {
        return url_origin;
    }

    public void setUrl_origin(String url_origin) {
        this.url_origin = url_origin;
    }

    private String url_origin;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public boolean isPrior(MavenVersionResult remote) {
        try {
            Long thisT = Long.parseLong(timestamp.replace(".",""));
            Long remoteT = Long.parseLong(remote.getTimestamp().replace(".",""));
            if (thisT < remoteT) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.err.println("Bad artefact timestamp ");
            return false;
        }
    }

    public String toString(){
        return timestamp+"-"+buildNumber+"@"+url_origin;
    }

    private boolean notDeployed = false;

    public boolean isNotDeployed() {
        return notDeployed;
    }

    public void setNotDeployed(boolean notDeployed) {
        this.notDeployed = notDeployed;
    }
}
