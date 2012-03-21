package org.kevoree.library.javase.webserver.markdown2htmlwar.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.kevoree.library.javase.webserver.markdown2htmlwar.client.MarkDown2HtmlService;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;




public class MarkDown2HtmlServiceImpl extends RemoteServiceServlet implements MarkDown2HtmlService,org.kevoree.library.javase.markdown2html.MarkDown2HtmlService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MarkDown2Html wrappee;
	public MarkDown2HtmlServiceImpl(MarkDown2Html wrappee  ) {
		this.wrappee = wrappee;
	}
	public MarkDown2HtmlServiceImpl() {
	}
	
	@Override
	public String markdown2html(String name) {
		return wrappee.markdown2html(name);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		System.err.println("toto");
	}



}