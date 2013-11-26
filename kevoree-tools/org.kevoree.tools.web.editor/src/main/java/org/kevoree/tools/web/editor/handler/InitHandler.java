package org.kevoree.tools.web.editor.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.serializer.JSONModelSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 26/11/13
 * Time: 11:11
 */
public class InitHandler extends AbstractHandler {

    private KevoreeModelHandlerService modelService;
    private JSONModelSerializer serializer;

    public InitHandler(KevoreeModelHandlerService modelService) {
        this.modelService = modelService;
        this.serializer = new JSONModelSerializer();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            baseRequest.setHandled(true);
            String strModel = this.serializer.serialize(this.modelService.getLastModel());
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(strModel);
        } catch (Exception e) {
            response.sendError(500, "Unable to serialize model");
        }
    }
}
