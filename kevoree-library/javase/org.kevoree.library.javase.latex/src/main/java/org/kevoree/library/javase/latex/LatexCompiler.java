package org.kevoree.library.javase.latex;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
				elems = {@MsgElem(name = "file", className = String.class), @MsgElem(name = "folder",
						className = String.class)}),
		@MessageType(name = "CLEAN", elems = {@MsgElem(name = "folder", className = String.class)})
})
@Provides({
		@ProvidedPort(name = "CLEAN", type = PortType.MESSAGE, messageType = "CLEAN"),
		@ProvidedPort(name = "COMPILE", type = PortType.MESSAGE, messageType = "COMPILE")
})
@Requires({
		@RequiredPort(name = "COMPILE_CALLBACK", type = PortType.MESSAGE)
})
@Library(name = "JavaSE")
@ComponentType
public class LatexCompiler extends AbstractComponentType implements CompilerComponent {

	private Logger logger = LoggerFactory.getLogger(LatexCompiler.class);
	private LatexCompilerManager compiler;

	@Start
	public void start () throws Exception {
		compiler = new LatexCompilerManager();
		if (!compiler.isAvailable()) {
			throw new Exception(
					"Unable to find required software (pdflatex and/or bibtex). Please check the configuration of your system");
		}
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
		if (message instanceof StdKevoreeMessage && ((StdKevoreeMessage) message).getKeys().contains("file")
				&& ((StdKevoreeMessage) message).getKeys().contains("folder")) {
			String file = (String) ((StdKevoreeMessage) message).getValue("file").get();
			String folder = (String) ((StdKevoreeMessage) message).getValue("folder").get();
			String result = compiler.compile(file, folder);
			// TODO send result COMPILE_CALLBACK port
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
