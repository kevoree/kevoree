package org.kevoree.library.javase.webserver.components;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

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
		@DictionaryAttribute(name = "forward", defaultValue = "http://kloud.kevoree.org", optional = false)
})
public class ProxyPage extends AbstractPage {


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
		Forwarder.forward(this.getDictionary().get("forward").toString(), request, response, path);
		return response;
	}
}
