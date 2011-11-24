package org.kevoree.library.javase.latex;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/11/11
 * Time: 09:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@MessageTypes({
		@MessageType(name = "COMPILE",
				elems = {@MsgElem(name = "id", className = UUID.class), @MsgElem(name = "file",
						className = String.class), @MsgElem(name = "folder",
						className = String.class)}),
		@MessageType(name = "CLEAN", elems = {@MsgElem(name = "folder", className = String.class)}),
		@MessageType(name = "COMPILE_CALLBACK",
				elems = {@MsgElem(name = "id", className = UUID.class), @MsgElem(name = "path",
						className = String.class), @MsgElem(name = "log", className = String.class), @MsgElem(
						name = "success", className = boolean.class)})
})
@Provides({
		@ProvidedPort(name = "CLEAN", type = PortType.MESSAGE, messageType = "CLEAN"),
		@ProvidedPort(name = "COMPILE", type = PortType.MESSAGE, messageType = "COMPILE")
})
@Requires({
		@RequiredPort(name = "COMPILE_CALLBACK", type = PortType.MESSAGE, optional = false)
})
@Library(name = "JavaSE")
@ComponentType
public class LatexCompiler extends AbstractComponentType implements CompilerComponent {

	private Logger logger = LoggerFactory.getLogger(LatexCompiler.class);
	private LatexCompilerManager compiler;

	@Start
	public void start () throws Exception {
		logger.debug("Starting " + this.getName());
		compiler = new LatexCompilerManager();
		if (!compiler.isAvailable()) {
			throw new Exception(
					"Unable to find required software (pdflatex and/or bibtex). Please check the configuration of your system");
		}
		logger.debug(this.getName() + " is started");
	}

	@Stop
	public void stop () {
		if (compiler != null) {
			compiler.stop();
		}
	}

	@Update
	public void update () throws Exception {
		stop();
		start();
	}

	@Port(name = "COMPILE")
	public void compile (Object message) {
		if (message instanceof StdKevoreeMessage && ((StdKevoreeMessage) message).getKeys().contains("id")
				&& ((StdKevoreeMessage) message).getKeys().contains("file")
				&& ((StdKevoreeMessage) message).getKeys().contains("folder")) {
			UUID id = (UUID) ((StdKevoreeMessage) message).getValue("id").get();
			String file = (String) ((StdKevoreeMessage) message).getValue("file").get();
			String folder = (String) ((StdKevoreeMessage) message).getValue("folder").get();
			String result = compiler.compile(file, folder);
			// TODO send result COMPILE_CALLBACK port
			if (isPortBinded("COMPILE_CALLBACK")) {
				StdKevoreeMessage msg = new StdKevoreeMessage();
				msg.putValue("id", id);
				msg.putValue("log", result);

				if (result.endsWith("Build success!")) {
					msg.putValue("path",
							folder + File.separator + file.substring(0, file.length() - ".tex".length()) + ".pdf");
					msg.putValue("success", true);
				} else {

					msg.putValue("success", false);
				}

				getPortByName("COMPILE_CALLBACK", MessagePort.class).process(msg);
			}
		} else {
			logger.warn("Missing data to compile something");
		}
	}

	@Port(name = "CLEAN")
	public void clean (Object message) {
		if (message instanceof StdKevoreeMessage && ((StdKevoreeMessage) message).getKeys().contains("folder")) {
			String folder = (String) ((StdKevoreeMessage) message).getValue("folder").get();
			compiler.clean(folder);
		} else {
			logger.warn("Missing data to compile something");
		}
	}
}
