package org.kevoree.library.javase.kinect;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.framework.message.StdKevoreeMessage;
import org.kevoree.library.javase.kinect.tester.KinectReconfigurationTester;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/01/12
 * Time: 14:45
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ComponentType
@Provides({
		@ProvidedPort(name = "motion", type = PortType.MESSAGE)
})
@Requires({
		@RequiredPort(name = "start_scan", type = PortType.MESSAGE),
		@RequiredPort(name = "stop_scan", type = PortType.MESSAGE),
		@RequiredPort(name = "motor", type = PortType.MESSAGE, optional = true)
})
public class KinectTester extends AbstractComponentType {

	@Start
	@Stop
	public void emptyMethod () {
	}


	private int i = 0;

	@Port(name = "motion")
	public void motion (Object message) {
		System.out.println("\n\n\n\n\t\t\t" + message + "\n\n\n");
		/*if (i == 0) {
		   i++;*/
		new Thread() {
			@Override
			public void run () {
				try {
					// reconfigure using kev script to unbind all motor event sender on the kinect
					// reconfigure using kev script to bind our motor port to the motor of the kinect
					String[] previouslyBounds = KinectReconfigurationTester
							.bind(getModelService().getLastModel(), getKevScriptEngineFactory(),
									KinectTester.this.getName(),
									getNodeName());
					// send start_scan even
					if (isPortBinded("start_scan")) {
						getPortByName("start_scan", MessagePort.class).process("start_scan");
					}
					Thread.sleep(2000);
					// starting to send motor event to scan from bottom to top
					if (isPortBinded("motor")) {

						StdKevoreeMessage msg = new StdKevoreeMessage();
						msg.putValue("percent", new Integer(0));
						getPortByName("motor", MessagePort.class).process(msg);

						Thread.sleep(2000);

						msg = new StdKevoreeMessage();
						msg.putValue("percent", new Integer(100));
						getPortByName("motor", MessagePort.class).process(msg);

					}
					Thread.sleep(3000);
					// send stop_scan event
					if (isPortBinded("stop_scan")) {
						getPortByName("stop_scan", MessagePort.class).process("stop_scan");
					}
					// reconfigure using kev script to bind all motor event sender on the kinect
					// reconfigure using kev script to unbind our motor port to the motor of the kinect
					KinectReconfigurationTester
							.unbind(getModelService().getLastModel(), getKevScriptEngineFactory(),
									KinectTester.this.getName(), getNodeName(), previouslyBounds);
//		}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
}
