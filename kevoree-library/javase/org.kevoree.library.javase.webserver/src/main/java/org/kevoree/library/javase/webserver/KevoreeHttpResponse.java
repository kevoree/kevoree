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
public class KevoreeHttpResponse implements Serializable {

    private UUID tokenID = UUID.randomUUID();

    private String content = "";

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private String contentType = "text/html";

    public UUID getTokenID() {
        return tokenID;
    }

    public void setTokenID(UUID tokenID) {
        this.tokenID = tokenID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private byte[] rawContent = null;

    public byte[] getRawContent() {
        return rawContent;
    }

    public void setRawContent(byte[] rawContent) {
        this.rawContent = rawContent;
    }
    
    private HashMap<String,String> headers = new HashMap<String,String>();

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }
}
