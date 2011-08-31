/**
 * Project: EnTiMid
 * Copyright: INRIA/IRISA 2011
 * Contributor(s) :
 * Author: barais
 */
package org.kevoree.library.esper;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This Kevoree component encapsulates JS.
 *
 * @author Olivier Barais
 * @copyright INRIA
 */
@Provides({@ProvidedPort(name = "inport", type = PortType.MESSAGE)})
@Requires({@RequiredPort(name = "outport", type = PortType.MESSAGE, needCheckDependency = false),
		@RequiredPort(name = "booleanoutport", type = PortType.MESSAGE, needCheckDependency = false)
})
@DictionaryType({@DictionaryAttribute(name = "code")})
@Library(name = "Kevoree::JScript")
@ComponentType
public class JavaScriptComponent extends AbstractComponentType {
	private static final Logger logger = LoggerFactory.getLogger(JavaScriptComponent.class);


	private String code;
	private ScriptEngine jsEngine;

	@Start
	public void start () {

		code = (String) this.getDictionary().get("code").toString();

		ScriptEngineManager mgr = new ScriptEngineManager();
		jsEngine = mgr.getEngineByName("JavaScript");

	}

	@Stop
	public void stop () {

	}

	@Update
	public void update () {

	}

	@Port(name = "inport")
	public void port1 (Object msg) {
		try {
			jsEngine.put("msg", msg);
			Object o = jsEngine.eval(code);
			out(o);
			booleanout(o);
		} catch (ScriptException ex) {
//			ex.printStackTrace();
			logger.warn("Unable to evaluate the received message", ex);
		}

	}

	public void out (Object o) {
		if (this.isPortBinded("outport")) {
			this.getPortByName("outport", MessagePort.class).process(o);
		}

	}

	public void booleanout (Object o) {
		if (Boolean.TRUE.equals(o)) {
			if (this.isPortBinded("booleanoutport")) {
				this.getPortByName("booleanoutport", MessagePort.class)
						.process(true);
			}
		}
	}

}
