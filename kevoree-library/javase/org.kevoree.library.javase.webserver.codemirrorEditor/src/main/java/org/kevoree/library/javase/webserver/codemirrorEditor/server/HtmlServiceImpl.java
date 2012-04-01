package org.kevoree.library.javase.webserver.codemirrorEditor.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kevoree.library.javase.webserver.codemirrorEditor.client.SendContent;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;




public class HtmlServiceImpl extends RemoteServiceServlet implements SendContent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HtmlEditor wrappee;
	public HtmlServiceImpl(HtmlEditor wrappee  ) {
		this.wrappee = wrappee;
	}
	public HtmlServiceImpl() {
	
	}
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		System.err.println("toto");
	}
	@Override
	public void sendHtmlContent(String s) {
		
		wrappee.sendHtmlContent(s) ;
	}

	


}