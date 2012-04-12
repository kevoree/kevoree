package org.kevoree.library.javase.webserver.impl;

import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:41
 */
public class KevoreeHttpResponseImpl implements KevoreeHttpResponse {

    private UUID tokenID = UUID.randomUUID();

    private String content = "";

	private int status = 200;

    /*public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    private String contentType = "text/html";*/

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

	public int getStatus () {
		return status;
	}

	public void setStatus (int status) {
		this.status = status;
	}
}
