package org.kevoree.library.javase.kinect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/08/11
 * Time: 21:49
 */
public class KinectNativeLibraryLoader {

	private static Logger logger = LoggerFactory.getLogger(KinectNativeLibraryLoader.class);

	public static String configure () {
		try {
			File folder = new File(System.getProperty("java.io.tmpdir") + File.separator + "libfreenect");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			String path = foundOSPath();
			String[] names = foundOSName();
			for (String name : names) {
				copyFileFromStream(name, path, folder);
			}
			logger.info("libfreenect copied in " + folder.getAbsolutePath());
			return folder.getAbsolutePath();
		} catch (IOException e) {
			logger.error("cannot copy dynamic libs for freenect", e);
			return ".";
		}
	}

	private static String foundOSPath () {
		if (isUnix()) {
			if (!is64()) {
				return "nativelib/Linux/i686-unknown-linux-gnu";
			} else if (is64()) {
				return "nativelib/Linux/i686-unknown-linux-gnu";
			}
		} else if (isMac()) {
			return "MacOs";
		} else if (isWindows()) {
			if (!is64()) {
				return "windows/x86";
			} else if (is64()) {
				return "windows/x86_64";
			}
		}
		return ".";
	}

	private static String[] foundOSName () {
		if (isUnix()) {
			if (!is64()) {
				return new String[]{"libfreenect.so", "libfreenect_sync.so"};
			} else if (is64()) {
				return new String[]{""};
			}
		} else if (isMac()) {
			return new String[]{"libfreenect.dylib","libfreenect_sync.dylib","libusb.dylib"};
		} else if (isWindows()) {
			if (!is64()) {
				return new String[]{""};
			} else if (is64()) {
				return new String[]{""};
			}
		}
		return null;
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

	private static String copyFileFromStream (String fileName, String filePath, File folder) throws IOException {
		InputStream inputStream = KinectNativeLibraryLoader.class.getClassLoader()
				.getResourceAsStream(filePath + "/" + fileName);
		if (inputStream != null) {
			File copy = new File(folder + File.separator + fileName);
			copy.deleteOnExit();
			OutputStream outputStream = new FileOutputStream(copy);
			byte[] bytes = new byte[1024];
			int length = inputStream.read(bytes);
			while (length > -1) {
				outputStream.write(bytes, 0, length);
				length = inputStream.read(bytes);
			}
			return folder.getAbsolutePath() + File.separator + fileName;
		}
		return null;
	}

	/*private void copyResult (String filePath, File folder) {
		val file = new File(filePath)
		if (file.exists()) {
		  val copy = new File(folder + File.separator + file.getName)
		  val inputStream = new FileInputStream(file)
		  val outpuStream = new FileOutputStream(copy)
		  var length: Int = 0
		  val bytes = new Array[Byte](1024)
		  length = inputStream.read(bytes)
		  while (length > -1) {
			outpuStream.write(bytes, 0, length)
			length = inputStream.read(bytes)
		  }
		}
	  }*/

}
