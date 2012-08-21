package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.annotation.ComponentType;

import javax.servlet.*;
import java.io.IOException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 19/08/12
 * Time: 09:39
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class HelloFilter  extends AbstractFilterPage {
	@Override
	public ServletContext getSharedServletContext () {
		return null;
	}

	@Override
	public void initFilter () {
		legacyFilter = new SimpleHelloFilter();
	}

	class SimpleHelloFilter implements Filter {

		@Override
		public void init (FilterConfig filterConfig) throws ServletException {
		}

		@Override
		public void doFilter (ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			logger.debug("Filter request");
			// do nothing on request
			chain.doFilter(request, response);
			logger.debug("Filter request after chain.doFilter");
			// add hello world on the begin of the response
			response.getOutputStream().write(("Hello World from Filter: " + getName()).getBytes("UTF-8"));
		}

		@Override
		public void destroy () {
		}
	}
}
