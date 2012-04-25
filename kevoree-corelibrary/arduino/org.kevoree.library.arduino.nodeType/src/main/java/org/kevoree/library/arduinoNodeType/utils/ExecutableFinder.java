package org.kevoree.library.arduinoNodeType.utils;

import org.kevoree.library.arduinoNodeType.util.ArduinoResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * User: ffouquet
 * Date: 09/06/11
 * Time: 16:37
 */
public class ExecutableFinder {
	private static final Logger logger = LoggerFactory.getLogger(ExecutableFinder.class);

//	private static List<String> pathDirectories = new ArrayList<String>();

//	private static String path = "";

	//ONLY FOR UNIX / windows must use cmd.exe
	/* Try to build system path */
	/*private static List<String> getSysPaths () {
		if (pathDirectories.isEmpty()) {
			if (System.getenv() != null) {
				if (System.getenv().get("PATH") != null) {
					String line = System.getenv().get("PATH");
					String[] paths = line.split(File.pathSeparator);
					Collections.addAll(pathDirectories, paths);
				}
			}
		}
		return pathDirectories;
	}*/

	/*public static String getBinaryDirectory () {
		return path;
	}*/

	public static String getAbsolutePath (String execName/*, List<String> otherPaths*/) {

//		String path = "";

//		try {
		List<String> paths = ArduinoResourceHelper.getBinaryLocation();
		for (String pathTmp : paths) {
			if (new File(pathTmp + File.separator + execName).exists()) {
				return pathTmp + File.separator + execName;
			}
		}
		/*} catch (IOException e) {
			//e.printStackTrace();
			//logger.error("Unexcpected error while extracting binary files", e);
		}*/
		/*for (String s : getSysPaths()) {
			try {
				Process p = Runtime.getRuntime().exec(s + File.separator + execName);
				path = s + File.separator + execName;
			} catch (IOException e) {
				//NOTHING
			}
		}
		if (otherPaths != null) {
			for (String s : otherPaths) {
				try {
					Process p = Runtime.getRuntime().exec(s + File.separator + execName);
					path = s + File.separator + execName;
				} catch (IOException e) {
					//NOTHING
				}
			}
		}*/
		return "";
	}

	/*private static String copyFileFromStream (String fileName, String filePath, File folder) throws IOException {
		InputStream inputStream = ExecutableFinder.class.getClassLoader()
				.getResourceAsStream(filePath + File.separator + fileName);
		//if (inputStream != null) {
		File copy = new File(folder + File.separator + fileName);
		copy.deleteOnExit();
		OutputStream outputStream = new FileOutputStream(copy);
		byte[] bytes = new byte[1024];
		int length = inputStream.read(bytes);
		while (length > -1) {
			outputStream.write(bytes, 0, length);
			length = inputStream.read(bytes);
		}
		outputStream.flush();
		outputStream.close();
		inputStream.close();

		copy.setExecutable(true);
		copy.deleteOnExit();
		return folder.getAbsolutePath() + File.separator + fileName;
		//}
		//return null;
	}*/
}
