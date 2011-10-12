package org.kevoree.library.javase.webserver;

import com.sun.net.httpserver.HttpServer;
import com.twitter.finagle.Service;
import com.twitter.finagle.builder.ServerBuilder;
import com.twitter.finagle.http.Http;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.*;

import java.net.InetSocketAddress;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/10/11
 * Time: 17:58
 * To change this template use File | Settings | File Templates.
 */
@Library(name="JavaSE")
@ComponentType
@DictionaryType({
    @DictionaryAttribute(name = "port")
})
public class WebServer {

    @Stop
   	public void start () {
        /*
        Service<HttpRequest, HttpResponse> myService = new HttpServer.Respond(this.getModelService());
      		server = ServerBuilder.safeBuild(myService, ServerBuilder.get().codec(Http.get())
      				.bindTo(new InetSocketAddress(portint))
      				.name(this.getNodeName()));   */

   	}

    @Start
   	public void stop () {

   	}

   	@Update
   	public void update () {
   		stop();
   		start();
   	}




}
