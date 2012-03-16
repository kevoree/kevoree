package org.kevoree.library.sky.provider;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.KevoreeXmiHelper;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 14/03/12
 * Time: 14:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "login", optional = false),
		@DictionaryAttribute(name = "model", optional = false)
})
@Requires({
		@RequiredPort(name = "deploy", type = PortType.MESSAGE, needCheckDependency = true),
		@RequiredPort(name = "release", type = PortType.MESSAGE, needCheckDependency = true)
})
public class PaaSKloudRessourceManagerTester extends AbstractComponentType {

	private boolean starting = true;

	@Start
	@Stop
	public void process () {
		if (starting) {
			starting = false;
			StdKevoreeMessage message = new StdKevoreeMessage();
			message.putValue("login", this.getDictionary().get("login"));
			String modelPath = this.getDictionary().get("model").toString();
			message.putValue("model", KevoreeXmiHelper.saveToString(KevoreeXmiHelper.load(modelPath), false));
			getPortByName("deploy", MessagePort.class).process(message);
		} else {
			StdKevoreeMessage message = new StdKevoreeMessage();
			message.putValue("login", this.getDictionary().get("login"));
			getPortByName("release", MessagePort.class).process(message);
			starting = true;
		}
	}

}
