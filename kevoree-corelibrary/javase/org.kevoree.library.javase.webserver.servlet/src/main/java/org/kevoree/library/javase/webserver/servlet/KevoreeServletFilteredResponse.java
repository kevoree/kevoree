package org.kevoree.library.javase.webserver.servlet;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/12/11
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class KevoreeServletFilteredResponse extends KevoreeServletResponse {

	private int filterID;

	public int getFilterID () {
		return filterID;
	}

	public KevoreeServletFilteredResponse (KevoreeServletResponse response, int filterID) {
		this.filterID = filterID;

	}
}
