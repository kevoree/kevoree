package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.library.javase.webserver.impl.KevoreeHttpResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:52
 */


@Library(name = "JavaSE")
@ComponentFragment
@Provides({
		@ProvidedPort(name = "request", type = PortType.MESSAGE)
})
@Requires({
		@RequiredPort(name = "content", type = PortType.MESSAGE),
		@RequiredPort(name = "forward", type = PortType.MESSAGE, optional = true)
})
@DictionaryType({
		@DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/")
})
public abstract class AbstractPage extends AbstractComponentType {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected static final int NO_RETURN_RESPONSE = 418;
	protected URLHandlerScala handler = new URLHandlerScala();

	public String getLastParam (String url) {

		String urlPattern = this.getDictionary().get("urlpattern").toString();
		Option<String> result = handler.getLastParam(url, urlPattern);
		if (result.isDefined()) {
			return result.get();
		} else {
			return null;
		}
	}

	@Start
	public void startPage () {
		handler.initRegex(this.getDictionary().get("urlpattern").toString());
		logger.debug("Abstract page start");
	}

	@Stop
	public void stopPage () {
		//NOOP
	}

	@Update
	public void updatePage () {
		handler.initRegex(this.getDictionary().get("urlpattern").toString());
	}

	public KevoreeHttpRequest resolveRequest (Object param) {
		logger.debug("KevoreeHttpRequest handler triggered");
		Option<KevoreeHttpRequest> parseResult = handler.check(param);
		if (parseResult.isDefined()) {
			return parseResult.get();
		} else {
			return null;
		}
	}

	public KevoreeHttpResponse buildResponse (KevoreeHttpRequest request) {
		KevoreeHttpResponse responseKevoree = new KevoreeHttpResponseImpl();
		responseKevoree.setTokenID(request.getTokenID());
		return responseKevoree;
	}

	@Port(name = "request")
	public void requestHandler (Object param) {
		KevoreeHttpRequest request = resolveRequest(param);
		if (request != null) {
			KevoreeHttpResponse response = buildResponse(request);
			response = process(request, response);
			if (response.getStatus() != 418) {
				this.getPortByName("content", MessagePort.class).process(response);//SEND MESSAGE
			} else {
				logger.debug("Status code correspond to tea pot: No response returns!");
			}
		}
	}


	public abstract KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response); /*{
        //TO OVERRIDE
        return response;
    }*/

	public KevoreeHttpResponse forward (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		// remove already used pattern => / + getLastParam(...)
		String previousUrl = request.getUrl();
		String url = getLastParam(previousUrl);
		if (!url.startsWith("/")) {
			url = "/" + url;
		}
		request.setUrl(url);/*
		logger.debug(request.getCompleteUrl());
		logger.debug(previousUrl);
		logger.debug(url);
		url = request.getCompleteUrl().replace(previousUrl, url);
		request.setCompleteUrl(url);*/
		if (isPortBinded("forward")) {
			logger.debug("forward request for url = {}", url);
			getPortByName("forward", MessagePort.class).process(request);
			response.setStatus(NO_RETURN_RESPONSE);
		} else {
			response.setContent("Bad request from " + getName() + "@" + getNodeName());
		}
		return response;
	}

}
