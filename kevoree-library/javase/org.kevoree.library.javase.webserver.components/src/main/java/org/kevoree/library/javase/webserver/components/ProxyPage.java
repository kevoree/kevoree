package org.kevoree.library.javase.webserver.components;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 05/01/12
 * Time: 10:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "forward", defaultValue = "kloud.kevoree.org", optional = false)
})
public class ProxyPage extends ParentAbstractPage {

	protected Logger logger = LoggerFactory.getLogger(ProxyPage.class);


	@Override
	@Start
	public void startPage () {
		super.startPage();
		Forwarder.initialize();
	}

	@Stop
	public void stopPage() {
		Forwarder.kill();
	}

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		String path = this.getLastParam(request.getUrl());
		if (path == null) {
			path = "";
		}
		String forwardUrl = this.getDictionary().get("forward").toString();
		if (forwardUrl.startsWith("http://")) {
			forwardUrl = forwardUrl.substring("http://".length());
		}
		String[] splittedForwardUrl = forwardUrl.split("/");
		String[] splittedBaseUrl = splittedForwardUrl[0].split(":");
		String baseUrl = splittedBaseUrl[0];
		int port = 80;
		if (splittedBaseUrl.length == 2) {
			try {
			port = Integer.parseInt(splittedBaseUrl[1]);
			} catch (NumberFormatException e){
				logger.debug("Unable to parse forward parameter", e);
			}
		}
		if (splittedForwardUrl.length >= 1) {
			boolean first = true;
			for (String split : splittedBaseUrl) {
				if (!first) {
					path = split + "/" + path;
					first = false;
				}
			}
		}

		Forwarder.forward(baseUrl, port, request, response, path, this.getDictionary().get("urlpattern").toString());
		return response;
	}
}
