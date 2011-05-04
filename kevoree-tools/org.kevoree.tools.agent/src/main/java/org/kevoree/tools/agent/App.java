package org.kevoree.tools.agent;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/05/11
 * Time: 20:45
 */
public class App {

	public static void main(String[] args) {
		new KevoreeNodeRunner("duke", 8000).startNode();
	}
}
