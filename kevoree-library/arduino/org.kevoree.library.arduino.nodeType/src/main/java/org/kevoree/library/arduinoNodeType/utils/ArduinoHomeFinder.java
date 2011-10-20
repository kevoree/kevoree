/*
package org.kevoree.library.arduinoNodeType.utils;

import org.kevoree.library.arduinoNodeType.util.ArduinoResourceHelper;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

*/
/**
 * User: ffouquet
 * Date: 08/06/11
 * Time: 18:07
 *//*

public class ArduinoHomeFinder {

	public static boolean checkArduinoHome () {

		String arduinoHome = ArduinoResourceHelper.getArduinoHome();
		if (arduinoHome != null && !arduinoHome.equals("")) {
			System.setProperty("arduino.home", arduinoHome);
		}


		if (System.getProperty("arduino.home") == null) {

			String userDir = System.getProperty("user.home");
			File kevoreeProps = new File(userDir + File.separator + ".kevoree.config");
			Properties properties = new Properties();
			if (kevoreeProps.exists()) {
				try {
					properties.load(new FileInputStream(kevoreeProps));
					System.setProperty("arduino.home", properties.getProperty("arduino.home"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				if (System.getProperty("os.name").toLowerCase().contains("mac")) {
					if (new File("/Applications/Arduino.app/Contents/Resources/Java").exists()) {
						System.out.println("OSX Default Path");
						System.setProperty("arduino.home", "/Applications/Arduino.app/Contents/Resources/Java");
					}
				} else {
					System.setProperty("arduino.home", guiAskForArduinoHome());
				}
			}
		}
		String previousArduinoHome = System.getProperty("arduino.home");
		if (previousArduinoHome == null) {
			System.out.println("Please install arduino environnement");
			return false;
		}


		File f = new File(previousArduinoHome);
		if (f.exists()) {
			//CHECK FOR OSX
			String osName = System.getProperty("os.name");
			if (osName.equals("Mac OS X") && f.getName().endsWith(".app")) {
				System.setProperty("arduino.home", f.getAbsolutePath() + "/Contents/Resources/Java");
			}
			return true;
		} else {
			String arduinoHome = ArduinoResourceHelper.getArduinoHome();
			if (arduinoHome != null && !arduinoHome.equals("")) {
				System.setProperty("arduino.home", arduinoHome);
				return true;
			} else {
				return false;
			}
		}

	}

	private static String guiAskForArduinoHome () {
		JFileChooser arduinoHomeFinder = new JFileChooser();
		arduinoHomeFinder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		arduinoHomeFinder.setDialogTitle("Please select Arduino Home base directory or executable");
		int result = arduinoHomeFinder.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			//STORE TO USER DIR
			String userDir = System.getProperty("user.home");
			File kevoreeProps = new File(userDir + File.separator + ".kevoree.config");
			Properties properties = new Properties();
			if (kevoreeProps.exists()) {
				try {
					properties.load(new FileInputStream(kevoreeProps));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			properties.setProperty("arduino.home", arduinoHomeFinder.getSelectedFile().getAbsolutePath());
			try {
				properties.store(new FileOutputStream(kevoreeProps), "Kevoree configuration file");
			} catch (IOException e) {
				e.printStackTrace();
			}

			return arduinoHomeFinder.getSelectedFile().getAbsolutePath();
		} else {
			return "";
		}
	}


}
*/
