/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.defaultNodeTypes;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.api.service.core.logging.KevoreeLogLevel;
import org.kevoree.framework.AbstractNodeType;
import org.kevoree.kompare.KevoreeKompareBean;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.CommandMapper;
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager;
import org.kevoreeAdaptation.AdaptationModel;
import org.kevoreeAdaptation.AdaptationPrimitive;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @author ffouquet
 */
@Library(name = "JavaSE")
@NodeType
@DictionaryType({
		@DictionaryAttribute(name = "logLevel", defaultValue = "INFO", optional = true, vals = {"INFO", "WARN", "DEBUG", "ERROR", "FINE"}),
		@DictionaryAttribute(name = "coreLogLevel", defaultValue = "WARN", optional = true, vals = {"INFO", "WARN", "DEBUG", "ERROR", "FINE"})
})
@PrimitiveCommands(
		values = {"UpdateType", "UpdateDeployUnit", "AddType", "AddDeployUnit", "AddThirdParty", "RemoveType", "RemoveDeployUnit", "UpdateInstance", "UpdateBinding", "UpdateDictionaryInstance", "AddInstance", "RemoveInstance", "AddBinding", "RemoveBinding", "AddFragmentBinding", "RemoveFragmentBinding", "UpdateFragmentBinding", "StartInstance", "StopInstance", "StartThirdParty", "RemoveThirdParty"},
		value = {})
public class JavaSENode extends AbstractNodeType {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JavaSENode.class);

	private KevoreeKompareBean kompareBean = null;
	private CommandMapper mapper = null;

	private boolean isRunning;
	private Thread shutdownThread;

	protected boolean isDaemon () {
		return false;
	}

	@Start
	@Override
	public void startNode () {
		isRunning = true;
		kompareBean = new KevoreeKompareBean();
		mapper = new CommandMapper();
		mapper.setNodeType(this);

		updateNode();

		if (!isDaemon()) {
			shutdownThread = new Thread() {
				@Override
				public void run () {
					catchShutdown();
				}
			};
			shutdownThread.start();
		}
	}


	@Stop
	@Override
	public void stopNode () {
		kompareBean = null;
		mapper = null;
		isRunning = false;
		if (shutdownThread != null) {
			shutdownThread.stop();
		}
		//Cleanup the local runtime
		KevoreeDeployManager.clearAll(this);
	}

	@Update
	@Override
	public void updateNode () {
		if (getBootStrapperService().getKevoreeLogService() != null) {
			KevoreeLogLevel logLevel = KevoreeLogLevel.WARN;
			KevoreeLogLevel corelogLevel = KevoreeLogLevel.WARN;
			if ("DEBUG".equals(getDictionary().get("logLevel"))) {
				logLevel = KevoreeLogLevel.DEBUG;
			}
			if ("WARN".equals(getDictionary().get("logLevel"))) {
				logLevel = KevoreeLogLevel.WARN;
			}
			if ("INFO".equals(getDictionary().get("logLevel"))) {
				logLevel = KevoreeLogLevel.INFO;
			}
			if ("ERROR".equals(getDictionary().get("logLevel"))) {
				logLevel = KevoreeLogLevel.ERROR;
			}
			if ("FINE".equals(getDictionary().get("logLevel"))) {
				logLevel = KevoreeLogLevel.FINE;
			}

			if ("DEBUG".equals(getDictionary().get("coreLogLevel"))) {
				corelogLevel = KevoreeLogLevel.DEBUG;
			}
			if ("WARN".equals(getDictionary().get("coreLogLevel"))) {
				corelogLevel = KevoreeLogLevel.WARN;
			}
			if ("INFO".equals(getDictionary().get("coreLogLevel"))) {
				corelogLevel = KevoreeLogLevel.INFO;
			}
			if ("ERROR".equals(getDictionary().get("coreLogLevel"))) {
				corelogLevel = KevoreeLogLevel.ERROR;
			}
			if ("FINE".equals(getDictionary().get("coreLogLevel"))) {
				corelogLevel = KevoreeLogLevel.FINE;
			}
			getBootStrapperService().getKevoreeLogService().setUserLogLevel(logLevel);
			getBootStrapperService().getKevoreeLogService().setCoreLogLevel(corelogLevel);
		}
	}

	@Override
	public AdaptationModel kompare (ContainerRoot current, ContainerRoot target) {
		return kompareBean.kompare(current, target, this.getNodeName());
	}

	@Override
	public org.kevoree.api.PrimitiveCommand getPrimitive (AdaptationPrimitive adaptationPrimitive) {
		return mapper.buildPrimitiveCommand(adaptationPrimitive, this.getNodeName());
	}

	private void catchShutdown () {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			String line = reader.readLine();
			boolean shutdown = false;
			while (isRunning && !shutdown) {
				if ("shutdown".equalsIgnoreCase(line)) {
					shutdown = true;
				} else if ("kcl".equalsIgnoreCase(line)) {
					System.out.println(this.getBootStrapperService().getKevoreeClassLoaderHandler().getKCLDump());
				} else if ("help".equalsIgnoreCase(line)) {
					System.out.println("commands:\n\tshutdown: allow to shutdown the node\n\tkcl: allow to list all the KCLClassLoader and their relationships");
				} else if (line == null) {
					isRunning = false;
				}
				line = reader.readLine();
			}
			if (shutdown) {
				// start the shutdown of the platform
				System.exit(0);
			}
		} catch (IOException ignored) {
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
	}

}
