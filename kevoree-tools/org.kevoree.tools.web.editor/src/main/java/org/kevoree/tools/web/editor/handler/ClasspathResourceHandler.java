package org.kevoree.tools.web.editor.handler;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;


/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 25/11/13
 * Time: 17:46
 */
public class ClasspathResourceHandler extends ResourceHandler {

    private String base = "";

    @Override
    public void setResourceBase(String resourceBase) {
        this.base = resourceBase;
    }

    @Override
    protected Resource getResource(HttpServletRequest request) throws MalformedURLException {
        return Resource.newClassPathResource(this.base+request.getRequestURI());
    }
}
