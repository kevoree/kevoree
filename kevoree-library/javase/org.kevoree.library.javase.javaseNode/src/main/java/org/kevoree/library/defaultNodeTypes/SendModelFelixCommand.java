package org.kevoree.library.defaultNodeTypes;

import org.apache.felix.shell.Command;
import org.kevoree.ContainerRoot;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/09/11
 * Time: 13:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class SendModelFelixCommand implements Command {
	private static final Logger logger = LoggerFactory.getLogger(SendModelFelixCommand.class);

	private KevoreeModelHandlerService handler;

	public SendModelFelixCommand (KevoreeModelHandlerService handler) {
		this.handler = handler;
	}

	public void sendModel (String model) {
		try {
			ContainerRoot root = KevoreeXmiHelper.load(model);
			handler.updateModel(root);
		} catch (Exception e) {
			logger.error("Unable to update model", e);
		}

	}

	@Override
	public String getName () {
		return "sendModel";
	}

	@Override
	public String getUsage () {
		return "sendModel <model file path>";
	}

	@Override
	public String getShortDescription () {
		return "sendModel to the platform and update the configuration";
	}

	@Override
	public void execute (String line, PrintStream out, PrintStream err) {
		StringTokenizer st = new StringTokenizer(line, " ");
		if (st.countTokens() == 2) {
			// Ignore the command name.
			st.nextToken();
			sendModel(st.nextToken());
		} else {
			out.println("Unable to execute command (Invalid number of parameters\n" + "Usage: " + getUsage());
		}
	}
}
