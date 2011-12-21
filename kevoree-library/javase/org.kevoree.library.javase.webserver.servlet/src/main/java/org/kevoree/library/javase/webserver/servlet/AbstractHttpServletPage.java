package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.osgi.framework.BundleException;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/12/11
 * Time: 12:50
 * To change this template use File | Settings | File Templates.
 */
@ComponentFragment
public abstract class AbstractHttpServletPage extends AbstractPage {

    public HttpServlet legacyServlet = null;
    public abstract void initServlet();

    @Override
    public void startPage() {
        super.startPage();
        try {
            initServlet();
            ServletConfig config = new ServletConfig() {
                @Override
                public String getServletName() {
                    return getName();
                }

                @Override
                public ServletContext getServletContext() {
                    return ServletContextHandler.getContext();
                }
                
                private HashMap<String,String> initParameterNames = new HashMap<String,String>();                

                @Override
                public String getInitParameter(String name) {
                    return initParameterNames.get(name);
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return Collections.enumeration(initParameterNames.keySet());
                }
            };

            legacyServlet.init(config);
        } catch (ServletException e) {
            logger.error("Error while starting servlet");
        }
    }

    @Override
    public void stopPage() {
        legacyServlet.destroy();
        super.stopPage();
    }

    @Override
    public KevoreeHttpResponse process(KevoreeHttpRequest request, KevoreeHttpResponse response) {
        KevoreeServletRequest wrapper_request = new KevoreeServletRequest(request);
        KevoreeServletResponse wrapper_response = new KevoreeServletResponse();
        try {
            logger.debug("Sending "+new String(request.getRawBody()));
            logger.debug("Sending " + request.getResolvedParams().keySet().size());
            legacyServlet.service(wrapper_request, wrapper_response);
        } catch (Exception e) {
            logger.error("Error while processing request",e);
        }
        wrapper_response.populateKevoreeResponse(response);
        return super.process(request, response);
    }


}
