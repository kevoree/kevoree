package org.kevoree.library;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jed
 * Date: 09/11/11
 * Time: 14:59
 */


@DictionaryType({
		@DictionaryAttribute(name = "refresh_speed", defaultValue = "2000", optional = true),
		@DictionaryAttribute(name = "range_min", defaultValue = "0", optional = true),
		@DictionaryAttribute(name = "range_max", defaultValue = "50", optional = true)
})
@ComponentType
@Requires({
		@RequiredPort(name = "out", type = PortType.MESSAGE, optional = true)
})
@GroupType
@Library(name = "Android")
public class Random extends AbstractComponentType implements Runnable {

	Boolean alive = false;
	Thread t = null;
	int range_min;
	int range_max;
	int refresh_speed;

	// private Logger logger = LoggerFactory.getLogger(ARandom.class);
	@Start
	public void start () throws IOException {
		// logger.debug("ARandom ","starting");
		updateDico();
		alive = true;
		t = new Thread(this);
		t.start();
	}

	@Stop
	public void stop () {
		//logger.debug("ARandom ","closing");
		alive = false;
	}

	@Update
	public void update () {
		updateDico();
	}

	@Override
	public void run () {
		java.util.Random rand = new java.util.Random();
		while (alive) {
			int rand_valeur = range_min + rand.nextInt(range_max - range_min);
			MessagePort port = (MessagePort) this.getPortByName("out");
			port.process("rand=" + rand_valeur);
			try {
				Thread.sleep(refresh_speed);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateDico () {
		range_max = Integer.parseInt(getDictionary().get("range_max").toString());
		range_min = Integer.parseInt(getDictionary().get("range_min").toString());
		refresh_speed = Integer.parseInt(getDictionary().get("refresh_speed").toString());
	}
}