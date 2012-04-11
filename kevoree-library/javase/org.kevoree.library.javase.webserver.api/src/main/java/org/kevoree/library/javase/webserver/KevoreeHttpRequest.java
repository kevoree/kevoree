package org.kevoree.library.javase.webserver;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:41
 */
public interface KevoreeHttpRequest extends Serializable {

	public HashMap<String, String> getHeaders ();

	public void setHeaders (HashMap<String, String> headers);

	public UUID getTokenID ();

	public String getUrl ();

	public void setUrl (String url);

	public HashMap<String, String> getResolvedParams ();

	public void setResolvedParams (HashMap<String, String> resolvedParams) ;

	public byte[] getRawBody () ;

	public void setRawBody (byte[] rawBody);

	public String getRawParams ();

	public void setRawParams(String rawParams);
}

