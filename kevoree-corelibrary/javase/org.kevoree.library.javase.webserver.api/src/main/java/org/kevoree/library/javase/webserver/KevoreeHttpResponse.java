package org.kevoree.library.javase.webserver;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:41
 */
public interface KevoreeHttpResponse extends Serializable {

    public int getTokenID();

    public void setTokenID(int tokenID);

    public String getContent();

    public void setContent(String content);

    public byte[] getRawContent();

    public void setRawContent(byte[] rawContent) ;

    public Map<String, String> getHeaders();

    public void setHeaders(Map<String, String> headers);

	public int getStatus () ;

	public void setStatus (int status);
}
