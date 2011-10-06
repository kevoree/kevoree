package org.kevoree.library.javase.webcam;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/10/11
 * Time: 07:53
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@DictionaryType({
		@DictionaryAttribute(name = "DEVICE", defaultValue = "/dev/video0", optional = false)
})
@Library(name = "JavaSE")
public class Webcam extends AbstractComponentType {

	@Start
	public void start () throws Exception {
		String device = (String)this.getDictionary().get("DEVICE");
	}

	@Stop
	public void stop () {

	}

	@Update
	public void update () throws Exception {
		stop();
		start();
	}

	public static boolean isWindows () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}

	public static boolean isMac () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("mac"));
	}

	public static boolean isUnix () {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("nix") || os.contains("nux"));
	}

	public static boolean is64 () {
		String os = System.getProperty("os.arch").toLowerCase();
		return (os.contains("64"));
	}
}
