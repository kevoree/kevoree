package org.kevoree.library.javase.webserver.jenkins;

import org.eclipse.jetty.osgi.boot.JettyBootstrapActivator;
import org.eclipse.jetty.osgi.boot.internal.webapp.OSGiWebappClassLoader;
import org.eclipse.jetty.osgi.boot.utils.BundleClassLoaderHelper;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.Bundle;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 02/01/12
 * Time: 20:49
 * To change this template use File | Settings | File Templates.
 */
public class Tester {

    public static void main(String[] args) throws Exception {
        System.out.println("Duke :-)");

        Thread serverThread = new Thread(){
            @Override
            public void run() {

            }
        };
        
        Server server = new Server(8080);
        WebAppContext context = new WebAppContext();
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setBaseResource(Resource.newClassPathResource("/"));
        context.setBaseResource(Resource.newClassPathResource("/"));

       // context.setDescriptor("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/javase/org.kevoree.library.javase.webserver.jenkins/target/warcontent/web.xml");
        context.setContextPath("/");
        context.setParentLoaderPriority(false);
        server.setHandler(context);
        HashLoginService dummyLoginService = new HashLoginService("KEVOREE-SECURITY-REALM");
        context.getSecurityHandler().setLoginService(dummyLoginService);
        server.start();
        //server.join();
        //OSGiWebappClassLoader


        org.eclipse.jetty.osgi.boot.internal.webapp.OSGiWebappClassLoader webappClassLoader = new org.eclipse.jetty.osgi.boot.internal.webapp.OSGiWebappClassLoader(org.eclipse.jetty.osgi.boot.JettyBootstrapActivator.class.getClassLoader(), context, null, new BundleClassLoaderHelper() {
            @Override
            public ClassLoader getBundleClassLoader(Bundle bundle) {
                return getClass().getClassLoader();
            }
        });
        context.setClassLoader(webappClassLoader);
        webappClassLoader.setWebappContext(context);
        
        server.stop();server.destroy();serverThread.stop();
    }

}
