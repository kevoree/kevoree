package org.kevoree.library.javase.webserver.codemirrorEditor.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


@RemoteServiceRelativePath("htmlService")
public interface SendContent  extends RemoteService {

	public void sendHtmlContent(String s);
	
}
