package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.library.javase.webserver.AbstractPage;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

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
            legacyServlet.init();
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
            legacyServlet.service(wrapper_request, wrapper_response);
        } catch (Exception e) {
            logger.warn("Error while processing request");
        }
        wrapper_response.populateKevoreeResponse(response);
        return super.process(request, response);
    }


}
