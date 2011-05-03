package org.kevoree.tools.agent;


import org.ops4j.pax.url.mvn.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KevoreeNodeRunner {

	private Logger logger = LoggerFactory.getLogger(KevoreeNodeRunner.class);

	private Process nodePlatformProcess;

	public KevoreeNodeRunner(String nodeName, Integer basePort) {

	}

	public void startNode() {
		
	
	}

	public void stopKillNode() {
		try {
			nodePlatformProcess.getOutputStream().write("stop 0".getBytes());
			nodePlatformProcess.getOutputStream().flush();
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error("The node cannot be killed. Try to force kill", e.getCause());
			nodePlatformProcess.destroy();
		}
	}

}
