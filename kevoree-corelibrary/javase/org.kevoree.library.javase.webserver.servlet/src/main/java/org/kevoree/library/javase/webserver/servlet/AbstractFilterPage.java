package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.annotation.*;
import org.kevoree.framework.MessagePort;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import javax.servlet.*;
import java.util.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/07/12
 * Time: 22:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentFragment
@Provides({
		@ProvidedPort(name = "filteredResponse", type = PortType.MESSAGE)
})
@Requires({
		@RequiredPort(name = "filteredRequest", type = PortType.MESSAGE)
})
public abstract class AbstractFilterPage extends AbstractPage {

	protected Filter legacyFilter;
	protected FilterConfig config;

	private KevoreeFilterChain chain = new KevoreeFilterChain(this);

	public abstract ServletContext getSharedServletContext ();

	public abstract void initFilter ();

	@Override
	public void startPage () {
		super.startPage();
		try {
			chain.staticInit();
			chain.start();
			initFilter();
			if (config == null) {
				config = new FilterConfig() {
					@Override
					public String getFilterName () {
						return getName();
					}

					@Override
					public ServletContext getServletContext () {
						return getSharedServletContext();
					}

					@Override
					public String getInitParameter (String name) {
						return null;
					}

					@Override
					public Enumeration<String> getInitParameterNames () {
						return Collections.enumeration(new ArrayList<String>(0));
					}
				};
			}

			legacyFilter.init(config);
		} catch (ServletException e) {
			logger.error("Error while starting servlet");
		}
	}

	@Override
	public void stopPage () {
		legacyFilter.destroy();
		super.stopPage();
	}

	@Override
	public KevoreeHttpResponse process (final KevoreeHttpRequest request, final KevoreeHttpResponse response) {

		KevoreeServletRequest wrapper_request = new KevoreeServletRequest(request, getLastParam(request.getUrl()));
		KevoreeServletResponse wrapper_response = new KevoreeServletResponse();
		try {
			logger.debug("Process filter request");
			//logger.debug("Sending " + request.getResolvedParams().keySet().size());
			legacyFilter.doFilter(wrapper_request, wrapper_response, chain);
//					service(wrapper_request, wrapper_response);
		} catch (Exception e) {
			logger.error("Error while processing request", e);
		}
		wrapper_response.populateKevoreeResponse(response);
		return response;
	}

	public void sendFilteredRequest (KevoreeHttpRequest request) {
		logger.debug("sendFilteredRequest");
		getPortByName("filteredRequest", MessagePort.class).process(request);
	}

	@Port(name = "filteredResponse")
	public void receiveFilteredResponse (Object msg) {
		logger.debug("receiveFilteredResponse");
		if (msg instanceof KevoreeHttpResponse) {
			chain.receiveFilterResponse((KevoreeHttpResponse) msg);
		} else {
			logger.debug("Unable to process this kind of result: {}", msg);
		}
	}

}
