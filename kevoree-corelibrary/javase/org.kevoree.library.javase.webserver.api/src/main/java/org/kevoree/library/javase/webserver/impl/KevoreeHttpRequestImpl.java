package org.kevoree.library.javase.webserver.impl;

import org.kevoree.library.javase.webserver.KevoreeHttpRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:41
 */
public class KevoreeHttpRequestImpl implements KevoreeHttpRequest {

    private String uri = "";
	private String url = "";

    private String rawParams = "";

	private String method = "GET";

    private Map<String, String> resolvedParams = new HashMap<String, String>();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

	@Override
	public void setMethod (String method) {
		this.method = method;
	}

	@Override
	public String getMethod () {
		return method;
	}

	private Map<String, String> headers = new HashMap<String, String>();

    private int tokenID = -1;

    public int getTokenID() {
        return tokenID;
    }

    public void setTokenID(int i) {
        tokenID = i;
    }

	public String getUrl() {
        return uri;
    }

    public void setUrl(String url) {
        this.uri = url;
    }

    public String getCompleteUrl() {
        return url;
    }

    public void setCompleteUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getResolvedParams() {
        return resolvedParams;
    }

    public void setResolvedParams(Map<String, String> resolvedParams) {
        this.resolvedParams = resolvedParams;
    }

    private byte[] rawBody = new byte[0];


    public byte[] getRawBody() {
        return rawBody;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }

    public String getRawParams() {
        return rawParams;
    }

    public void setRawParams(String rawParams) {
        this.rawParams = rawParams;
    }
}

