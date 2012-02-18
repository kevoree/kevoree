/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kevoree.library.arduinoNodeType;

import org.kevoree.api.Bootstraper;
import org.kevoree.library.arduinoNodeType.util.ArduinoResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wayoda.ang.project.Sketch;
import org.wayoda.ang.project.Target;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ffouquet
 */
public class ArduinoDeploy {
	private static final Logger logger = LoggerFactory.getLogger(ArduinoDeploy.class);

	protected ArrayList<String> baseCmd;
	/**
	 * @parameter
	 */
	private String baudrate;

	public void prepareCommands () {

		/*
				String binPrefix = System.getProperty("arduino.home") + "/hardware/tools/avr/bin";
				if (binPrefix != null && !binPrefix.endsWith(File.separator)) {
					binPrefix += File.separator;
				} */

		baseCmd = new ArrayList<String>();
		baseCmd.add(ArduinoToolChainExecutables.getAVRDUDE());
		String confPath = /*System.getProperty("arduino.home")*/
				ArduinoResourceHelper.getArduinoHome() + File.separator + "hardware" + File.separator + "tools"
						+ File.separator + "avr" + File.separator + "etc" + File.separator + "avrdude.conf";
		if (!confPath.startsWith(File.separator + "hardware")) {
			baseCmd.add("-C");
			baseCmd.add(confPath);
		}
	}

	public final void uploadSketch (Sketch sketch, Target target, String portName) {

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

		File hexFile = sketch.getFlash(target);
		if (hexFile == null) {
			logger.warn("No upload data found");
			throw new IllegalStateException();
		}

		ArrayList<String> cmd = new ArrayList<String>();
		cmd.addAll(baseCmd);
		cmd.add("-q");
		cmd.add("-q");
		cmd.add("-c");

		if (target.getKey().equals("mega2560")) {
			cmd.add("stk500v2");
		} else {
			cmd.add("arduino");
		}

        cmd.add("-F");
		cmd.add("-p");
		cmd.add(target.getMCU());
		cmd.add("-P");
		cmd.add(portName);
		cmd.add("-b");
		cmd.add(baudrate != null ? baudrate : target.getUploadSpeed());
		cmd.add("-D");
		cmd.add("-Uflash:w:" + hexFile.getPath() + ":i");
		execute(cmd);
	}

	protected final int execute (List<String> cmds) {
		String finalCommand = "";
		for (String cmd : cmds) {
			finalCommand = finalCommand + cmd + " ";
		}
		ArduinoCommandExec.execute(finalCommand);
		return 0;
	}
}
