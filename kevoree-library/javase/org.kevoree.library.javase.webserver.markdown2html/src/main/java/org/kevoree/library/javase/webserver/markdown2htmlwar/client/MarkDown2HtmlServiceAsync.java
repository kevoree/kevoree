package org.kevoree.library.javase.webserver.markdown2htmlwar.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface MarkDown2HtmlServiceAsync {
	void markdown2html(String input, AsyncCallback<String> callback);
}
