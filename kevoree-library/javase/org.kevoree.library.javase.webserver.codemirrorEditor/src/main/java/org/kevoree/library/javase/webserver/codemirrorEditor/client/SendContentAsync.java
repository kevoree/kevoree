package org.kevoree.library.javase.webserver.codemirrorEditor.client;

import com.google.gwt.user.client.rpc.AsyncCallback;



public interface SendContentAsync  {

	public void sendHtmlContent(String s, AsyncCallback<Void> res);
	
}
