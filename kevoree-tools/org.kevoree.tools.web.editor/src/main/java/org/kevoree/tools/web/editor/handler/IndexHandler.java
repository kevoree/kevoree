package org.kevoree.tools.web.editor.handler;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.ClasspathTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 25/11/13
 * Time: 12:58
 */
public class IndexHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);

            JadeConfiguration config = new JadeConfiguration();
            config.setTemplateLoader(new ClasspathTemplateLoader());
            config.setCaching(true);
            JadeTemplate template = config.getTemplate("webapp/views/editor.jade");
            String html = config.renderTemplate(template, new HashMap<String, Object>());

            response.getWriter().println(html);
            baseRequest.setHandled(true);
        } catch (Exception e) {
            baseRequest.setHandled(false);
        }
    }
}
