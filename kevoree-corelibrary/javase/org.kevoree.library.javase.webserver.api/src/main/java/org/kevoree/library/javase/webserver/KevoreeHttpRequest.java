package org.kevoree.library.javase.webserver;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:41
 */
public interface KevoreeHttpRequest extends Serializable {

	public Map<String, String> getHeaders ();

	public void setHeaders (Map<String, String> headers);

	public int getTokenID();

    public void setTokenID(int t);

	public String getUrl ();

	public void setUrl (String url);

	public Map<String, String> getResolvedParams ();

	public void setResolvedParams (Map<String, String> resolvedParams) ;

	public byte[] getRawBody () ;

	public void setRawBody (byte[] rawBody);

	public String getRawParams ();

	public void setRawParams(String rawParams);
}

