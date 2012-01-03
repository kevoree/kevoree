package org.kevoree.library.javase.webserver.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/12/11
 * Time: 22:54
 * To change this template use File | Settings | File Templates.
 */
public class FakeServletContext implements ServletContext {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public String getContextPath() {
        
        logger.warn("Call getContextPath");
        
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletContext getContext(String uripath) {
        logger.warn("Call getContext");
        return this;
    }

    @Override
    public int getMajorVersion() {
        return 3;
    }

    @Override
    public int getMinorVersion() {
        return 1;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 3;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 1;
    }

    @Override
    public String getMimeType(String file) {
        logger.warn("Call getMimeType");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        logger.warn("Call getResourcePaths");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        logger.warn("Call getResource");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InputStream getResourceAsStream(String path) {
       // return this.getResourceAsStream(path);
        logger.warn("Call getResourceAsStream");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        logger.warn("Call getRequestDispatcher");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        logger.warn("Call getNamedDispatcher");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        logger.warn("Call getServlet");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        logger.warn("Call getServlets");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration<String> getServletNames() {
        logger.warn("Call getServletNames");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void log(String msg) {
        logger.debug(msg);
    }

    @Override
    public void log(Exception exception, String msg) {
        logger.debug(msg,exception);
    }

    @Override
    public void log(String message, Throwable throwable) {
        logger.debug(message,throwable);
    }

    @Override
    public String getRealPath(String path) {
        logger.warn("Call getRealPath");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getServerInfo() {
        logger.warn("Call getServerInfo");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getInitParameter(String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private HashMap<String,String> initParameterNames = new HashMap<String,String>();

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameterNames.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        initParameterNames.put(name,value);
        return true;
    }


    private HashMap<String,Object> attributes = new HashMap<String,Object>();

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        attributes.put(name,object);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addListener(String className) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ClassLoader getClassLoader() {
        //return this.getClass().getClassLoader();
        return null;
    }

    @Override
    public void declareRoles(String... roleNames) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
