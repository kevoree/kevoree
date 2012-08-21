package org.kevoree.library.javase.webserver.servlet;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/12/11
 * Time: 13:20
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeServletFilteredRequest extends KevoreeServletRequest {

	private int filterID;

	public int getFilterID() {
		return filterID;
	}

	public KevoreeServletFilteredRequest (KevoreeServletRequest request) {
		super(request.kevRequest, request.basePath);
	}
}
