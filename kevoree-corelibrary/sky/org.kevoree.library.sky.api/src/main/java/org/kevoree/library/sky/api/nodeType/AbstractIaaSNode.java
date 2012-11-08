package org.kevoree.library.sky.api.nodeType;

import com.sun.management.OperatingSystemMXBean;
import org.kevoree.annotation.NodeFragment;
import org.kevoree.api.service.core.script.KevScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/10/12
 * Time: 17:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@NodeFragment
public abstract class AbstractIaaSNode extends AbstractHostNode implements IaaSNode {
	private static final Logger logger = LoggerFactory.getLogger(AbstractIaaSNode.class);
	private boolean initialization;

	@Override
	public void startNode () {
		super.startNode();
		initialization = true;
	}

	protected void updateModel (KevScriptEngine kengine) {
		Boolean created = false;
		for (int i = 0; i < 20; i++) {
			try {
				kengine.atomicInterpretDeploy();
				created = true;
				break;
			} catch (Exception e) {
				logger.warn("Error while try to update the configuration of node {}, try number {}", new String[]{getName(), i + ""}, e);
			}
		}
		if (!created) {
			logger.error("After 20 attempt, it was not able to update the configuration of {}", getName());
		}
	}

	@Override
	public void modelUpdated () {
		super.modelUpdated();
		if (initialization) {
			// check RAM, CPU_CORE, CPU_FREQUENCY, ARCH, OS, DISK_SIZE
			KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
			kengine.addVariable("nodeName", getName());
			kengine.addVariable("os", System.getProperty("os.name") + "-" + System.getProperty("os.version"));
			kengine.addVariable("arch", System.getProperty("os.arch"));
			if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
				kengine.addVariable("ram", ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() + "");
			} else {
				logger.warn("Unable to get RAM");
				kengine.addVariable("ram", "N/A");
			}

			kengine.addVariable("cpuCore", ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors() + "");
			try {
				Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo  | grep 'model name' | cut -d'@' -f2");
				// TODO read the output and parse to get the frequency
				kengine.addVariable("cpuFrequency", "N/A");
			} catch (IOException e) {
				logger.warn("Unable to get CPU frequency");
				kengine.addVariable("cpuFrequency", "N/A");
			}
			kengine.addVariable("diskSize", new File(System.getProperty("user.home")).getTotalSpace() + "");

			kengine.append("updateDictionary {nodeName} { OS = '{os}', ARCH = '{arch}', RAM = '{ram}', CPU_CORE = '{cpuCore}', CPU_FREQUENCY = '{cpuFrequency}', DISK_SIZE = '{diskSize}' }");

			updateModel(kengine);
			initialization = false;
		}
	}
}
