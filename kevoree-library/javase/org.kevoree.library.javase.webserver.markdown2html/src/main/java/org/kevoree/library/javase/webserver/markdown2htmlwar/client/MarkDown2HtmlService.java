package org.kevoree.library.javase.webserver.markdown2htmlwar.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("markdown2html")
public interface MarkDown2HtmlService extends RemoteService {
	String markdown2html(String name);
}