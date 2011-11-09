package org.kevoree.library.javase.webserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:41
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeHttpRequest implements Serializable {
    
    private String url = "";
    
    private HashMap<String,String> resolvedParams = new HashMap<String,String>();

    private UUID tokenID = UUID.randomUUID();

    public UUID getTokenID() {
        return tokenID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, String> getResolvedParams() {
        return resolvedParams;
    }

    public void setResolvedParams(HashMap<String, String> resolvedParams) {
        this.resolvedParams = resolvedParams;
    }
}

