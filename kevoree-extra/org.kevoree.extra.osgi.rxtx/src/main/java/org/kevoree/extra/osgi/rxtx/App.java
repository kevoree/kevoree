package org.kevoree.extra.osgi.rxtx;

import gnu.io.CommPortIdentifier;

/**
 * User: Erwan Daubert
 * Date: 18/04/11
 * Time: 17:44
 */
public class App {

	public static void main(String[] args) {
		// TODO need to add manually the path of the rxtx dynamic library : -Djava.library.path=...

		for (CommPortIdentifier port : RXTXHelper.getAvailablePorts()) {
			System.out.println(port.getName());
		}
	}
}
