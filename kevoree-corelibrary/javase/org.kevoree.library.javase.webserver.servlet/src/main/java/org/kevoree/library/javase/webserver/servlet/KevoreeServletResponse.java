package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/12/11
 * Time: 13:28
 */
public class KevoreeServletResponse implements HttpServletResponse {

	private Map<String, String> headers = new HashMap<String, String>();

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	final private ByteArrayOutputStream stream = new ByteArrayOutputStream();
	final private PrintWriter writer = new PrintWriter(stream);
	final ServletOutputStream servletStream = new ServletOutputStream() {
		@Override
		public void write (int i) throws IOException {
			stream.write(i);
		}
	};

	private String contentType = "text/html";

	@Override
	public String getCharacterEncoding () {
		return "UTF-8";
	}

	@Override
	public String getContentType () {
		return contentType;
	}

	@Override
	public ServletOutputStream getOutputStream () throws IOException {
		return servletStream;
	}

	@Override
	public PrintWriter getWriter () throws IOException {
		return writer;
	}

	@Override
	public void setCharacterEncoding (String s) {
	}

	@Override
	public void setContentLength (int i) {
	}

	@Override
	public void setContentType (String s) {
		contentType = s;
	}

	@Override
	public void setBufferSize (int i) {
	}

	@Override
	public int getBufferSize () {
		return 0;
	}

	@Override
	public void flushBuffer () throws IOException {
		stream.flush();
	}

	@Override
	public void resetBuffer () {
		stream.reset();
	}

	@Override
	public boolean isCommitted () {
		return true;
	}

	@Override
	public void reset () {
		stream.reset();
	}

	@Override
	public void setLocale (Locale locale) {
	}

	@Override
	public Locale getLocale () {
		return null;
	}

	public void populateKevoreeResponse (KevoreeHttpResponse response) {
		try {
			stream.flush();
			//logger.debug("Set ContentRaw {}",new String(stream.toByteArray()));
			response.setRawContent(stream.toByteArray());
			response.setHeaders(headers);
			response.getHeaders().put("Content-Type", contentType);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void populateFromKevoreeResponse (KevoreeHttpResponse response) {
		byte[] content = response.getRawContent();
		String contentString = response.getContent();
		if (contentString != null) {
			try {
				content = contentString.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				content = contentString.getBytes();
			}
		}
		if (content != null) {
			headers = response.getHeaders();
			setContentType(response.getHeaders().get("Content-Type"));
			stream.write(content, 0, content.length);
		} else {
			content = "Unable to get content coming from KevoreeHttpResponse".getBytes();
			setStatus(404);
			stream.write(content, 0, content.length);
		}
	}


	@Override
	public void addCookie (Cookie cookie) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public boolean containsHeader (String name) {
		return headers.containsKey(name);
	}

	@Override
	public String encodeURL (String url) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String encodeRedirectURL (String url) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String encodeUrl (String url) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public String encodeRedirectUrl (String url) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void sendError (int sc, String msg) throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void sendError (int sc) throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void sendRedirect (String location) throws IOException {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void setDateHeader (String name, long date) {
		headers.put(name, date + "");
	}

	@Override
	public void addDateHeader (String name, long date) {
		headers.put(name, date + "");
	}

	@Override
	public void setHeader (String name, String value) {
		headers.put(name, value);
	}

	@Override
	public void addHeader (String name, String value) {
		headers.put(name, value);
	}

	@Override
	public void setIntHeader (String name, int value) {
		headers.put(name, value + "");
	}

	@Override
	public void addIntHeader (String name, int value) {
		headers.put(name, value + "");
	}

	@Override
	public void setStatus (int sc) {
		status = sc;
	}

	@Override
	public void setStatus (int sc, String sm) {
		status = sc;
	}

	int status = 200;

	@Override
	public int getStatus () {
		return status;
	}

	@Override
	public String getHeader (String name) {
		return headers.get(name);
	}

	@Override
	public Collection<String> getHeaders (String name) {
		return headers.keySet();
	}

	@Override
	public Collection<String> getHeaderNames () {
		return headers.keySet();
	}
}
