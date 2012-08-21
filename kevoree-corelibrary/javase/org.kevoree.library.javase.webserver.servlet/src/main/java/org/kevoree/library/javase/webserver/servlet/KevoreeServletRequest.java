package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/12/11
 * Time: 13:20
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeServletRequest implements HttpServletRequest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    KevoreeHttpRequest kevRequest = null;
    ServletInputStream input = null;

    BufferedReader reader = null;
    
    String basePath = "";

    public KevoreeServletRequest(KevoreeHttpRequest _r,String _basePath) {
        kevRequest = _r;
        basePath = _basePath;
        input = new ServletInputStream() {

            ByteArrayInputStream inputStream = null;
            {
                if (kevRequest.getRawBody() != null)
                    inputStream = new ByteArrayInputStream(kevRequest.getRawBody());
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public int available() throws IOException {
                return inputStream.available();
            }
        };
        reader = new BufferedReader(new InputStreamReader(input));
    }

    @Override
    public Object getAttribute(String s) {
        return kevRequest.getResolvedParams().get(s);
    }


	public int getTokenID () {
		return kevRequest.getTokenID();
	}

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(kevRequest.getResolvedParams().keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
    }

    @Override
    public int getContentLength() {
        if (kevRequest.getRawBody() != null) {
            return kevRequest.getRawBody().length;
        }
        return 0;
    }

    @Override
    public String getContentType() {
        if(kevRequest.getHeaders().containsKey("Content-Type")){
            return kevRequest.getHeaders().get("Content-Type");
		}
		if (kevRequest.getHeaders().containsKey("content-type")) {
			return kevRequest.getHeaders().get("content-type");
		}
        return "text/plain";
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return input;
    }

    @Override
    public String getParameter(String s) {
        return kevRequest.getResolvedParams().get(s);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(kevRequest.getResolvedParams().keySet());
    }

    @Override
    public String[] getParameterValues(String s) {
        return new String[]{kevRequest.getResolvedParams().get(s)};
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        logger.error("Not implemented !!! ");
        return null;
    }

    @Override
    public String getProtocol() {
        return "http";
    }

    @Override
    public String getScheme() {
        logger.error("Not implemented !!! ");
        return null;
    }

    @Override
    public String getServerName() {
        return "kevoreewebserver";
    }

    @Override
    public int getServerPort() {
        return 8080;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return reader;
    }

    @Override
    public String getRemoteAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRemoteHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setAttribute(String s, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeAttribute(String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Locale getLocale() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isSecure() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRealPath(String s) {
        return "/";
    }

    @Override
    public int getRemotePort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getLocalPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletContext getServletContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAsyncStarted() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAsyncSupported() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAuthType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getDateHeader(String name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHeader(String name) {
		/*for (String key : kevRequest.getHeaders().keySet()) {
			System.out.println(key + "\t" + kevRequest.getHeaders().get(key));
		}*/
        return kevRequest.getHeaders().get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
		/*for (String key : kevRequest.getHeaders().keySet()) {
					System.out.println(key + "\t" + kevRequest.getHeaders().get(key));
				}*/
        return Collections.enumeration(Collections.singleton(kevRequest.getHeaders().get(name)));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
		/*for (String key : kevRequest.getHeaders().keySet()) {
					System.out.println(key + "\t" + kevRequest.getHeaders().get(key));
				}*/
        return Collections.enumeration(kevRequest.getHeaders().keySet());
    }

    @Override
    public int getIntHeader(String name) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getMethod() {
        if (kevRequest.getRawBody().length > 0) {
            return "POST";
        } else {
            return "GET";
        }

    }

    @Override
    public String getPathInfo() {
        return kevRequest.getUrl();
    }

    @Override
    public String getPathTranslated() {
        return kevRequest.getUrl();
    }

    @Override
    public String getContextPath() {
        return basePath;
    }

    @Override
    public String getQueryString() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRemoteUser() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Principal getUserPrincipal() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRequestedSessionId() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getRequestURI() {
        return kevRequest.getUrl();
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getServletPath() {
        return kevRequest.getUrl();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HttpSession getSession() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void login(String username, String password) throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logout() throws ServletException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
