package org.kevoree.tools.web.editor;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.tools.web.editor.handler.ClasspathResourceHandler;
import org.kevoree.tools.web.editor.handler.IndexHandler;
import org.kevoree.tools.web.editor.handler.InitHandler;

/**
 * Created with IntelliJ IDEA.
 * User: leiko
 * Date: 25/11/13
 * Time: 12:37
 */
@DictionaryType({
        @DictionaryAttribute(name = "port", defaultValue = "3042", optional = false)})
@ComponentType
public class KevWebEditor extends AbstractComponentType {

    private Server server;

    @Start
    public void start() throws Exception {
        int port = Integer.parseInt(getDictionary().get("port").toString());
        server = new Server(port);

        ResourceHandler resourceHandler = new ClasspathResourceHandler();
        resourceHandler.setResourceBase("webapp/public");

        ContextHandler initContext = new ContextHandler();
        initContext.setContextPath("/init");
        initContext.setHandler(new InitHandler(this.getModelService()));

        ContextHandler indexContext = new ContextHandler();
        indexContext.setContextPath("/");
        indexContext.setHandler(new IndexHandler());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, initContext, indexContext });
        server.setHandler(handlers);

        server.start();
    }

    @Stop
    public void stop() throws Exception {
        if (server != null) server.stop();
    }
}
