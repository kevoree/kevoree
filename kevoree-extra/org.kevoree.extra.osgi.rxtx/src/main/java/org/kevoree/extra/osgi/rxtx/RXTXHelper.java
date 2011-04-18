package org.kevoree.extra.osgi.rxtx;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Erwan Daubert
 * Date: 18/04/11
 * Time: 17:36
 */
public class RXTXHelper {

	private final static Logger logger = LoggerFactory.getLogger(RXTXHelper.class);

	public static Set<CommPortIdentifier> getAvailablePorts() {
		HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
			switch (com.getPortType()) {
				case CommPortIdentifier.PORT_SERIAL:
					try {
						CommPort thePort = com.open("CommUtil", 50);
						thePort.close();
						h.add(com);
					} catch (PortInUseException e) {
						logger.error("Port, " + com.getName() + ", is in use.");
					} catch (Exception e) {
						logger.error("Failed to open port " + com.getName(), e.getCause());
					}
			}
		}
		return h;
	}
}
