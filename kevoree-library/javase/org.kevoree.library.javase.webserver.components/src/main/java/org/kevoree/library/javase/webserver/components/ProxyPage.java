package org.kevoree.library.javase.webserver.components;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

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
		Forwarder.forward(forwardUrl, request, response, path, this.getDictionary().get("urlpattern").toString());
		return response;
	}
}
