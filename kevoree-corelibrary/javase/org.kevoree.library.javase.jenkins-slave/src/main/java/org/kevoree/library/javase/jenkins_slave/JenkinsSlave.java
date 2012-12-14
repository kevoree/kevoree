package org.kevoree.library.javase.jenkins_slave;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Start;
import org.kevoree.annotation.Stop;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.library.jabase.jenkins_slave.ProcessStreamFileLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 10/10/12
 * Time: 15:14
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
public class JenkinsSlave extends AbstractComponentType {
	private static final Logger logger = LoggerFactory.getLogger(JenkinsSlave.class);

	private Process process;

	@Start
	public void start () throws Exception {
		process = Runtime.getRuntime().exec(new String[]{getJava(), "-cp", buildClassPath(), "hudson.plugins.swarm.Client"});
		String logFile = System.getProperty("java.io.tmpdir") + File.separator + getName() + ".log";
		File outFile = new File(logFile + ".out");
		logger.debug("writing logs about {} on {}", getName(), outFile.getAbsolutePath());
		new Thread(new ProcessStreamFileLogger(process.getInputStream(), outFile)).start();
		File errFile = new File(logFile + ".err");
		logger.debug("writing logs about {} on {}", getName(), errFile.getAbsolutePath());
		new Thread(new ProcessStreamFileLogger(process.getErrorStream(), errFile)).start();
	}

	@Stop
	public void stop () {
		process.destroy();
	}

	private String getJava () {
		String java_home = System.getProperty("java.home");
		return java_home + File.separator + "bin" + File.separator + "java";
	}

	private String buildClassPath () throws Exception {
		List<String> repos = new ArrayList<String>(1);
	    File file1 = getBootStrapperService().resolveArtifact("swarm-client", "org.jenkins-ci.plugins", "1.7", repos);
		File file2 = getBootStrapperService().resolveArtifact("args4j", "args4j", "2.0.19", repos);
		File file3 = getBootStrapperService().resolveArtifact("remoting", "org.jenkins-ci.main", "2.12", repos);
		File file4 = getBootStrapperService().resolveArtifact("commons-httpclient", "commons-httpclient", "3.1", repos);

	    if (file1 != null && file2 != null && file3 != null && file4 != null) {
	      return file1.getAbsolutePath() + ";" + file3.getAbsolutePath() +";" + file3.getAbsolutePath() + ";" + file3.getAbsolutePath();
	    } else {
	      throw new Exception("Unable to find dependencies to start jenkins slave process");
	    }
	  }

}
