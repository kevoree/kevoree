package org.kevoree.library.sky.logging;

import org.kevoree.library.javase.nodejs.AbstractNodeJSComponentType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 22/08/12
 * Time: 22:09
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class LoggingMonitor extends AbstractNodeJSComponentType {
	@Override
	public String getMainFile () {
		return "server.js";
	}

	@Override
	public String getMainDir () {
		File tmpDir = createTempDir();
		if (tmpDir != null) {
			copyFile(tmpDir.getAbsolutePath(), "/log.html");
			copyFile(tmpDir.getAbsolutePath(), "/logger.js");
			copyFile(tmpDir.getAbsolutePath(), "/manager.js");
			copyFile(tmpDir.getAbsolutePath(), "/namespace.js");
            copyFile(tmpDir.getAbsolutePath(), "/jquery-1.8.0.min.js");
            copyFile(tmpDir.getAbsolutePath(), "/parser.js");
			copyFile(tmpDir.getAbsolutePath(), "/server.js");
			copyFile(tmpDir.getAbsolutePath(), "/socket.io.js");
			copyFile(tmpDir.getAbsolutePath(), "/socket.js");
			copyFile(tmpDir.getAbsolutePath(), "/static.js");
			copyFile(tmpDir.getAbsolutePath(), "/store.js");
			copyFile(tmpDir.getAbsolutePath(), "/transport.js");
			copyFile(tmpDir.getAbsolutePath(), "/util.js");
			copyFile(tmpDir.getAbsolutePath(), "/stores/memory.js");
			copyFile(tmpDir.getAbsolutePath(), "/stores/redis.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/flashsocket.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/htmlfile.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/http-polling.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/http.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/index.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/jsonp-polling.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/websocket.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/xhr-polling.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/websocket/default.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/websocket/hybi-07-12.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/websocket/hybi-16.js");
			copyFile(tmpDir.getAbsolutePath(), "/transports/websocket/index.js");

			return tmpDir.getAbsolutePath();
		} else {
			logger.error("Unable to create temporary directory");
			return null;
		}

	}

	public void copyFile (String tmpDir, String tmpFilePath) {
		boolean isMkdirs = true;
		// if the path include folder, they must be mkdirs before to copy the file
		if (tmpFilePath.substring(1).contains("/")) {
			String[] paths = tmpFilePath.substring(1).split("/");
			for (int i = 0; i < paths.length - 1; i++) {
				String path = "";
				for (int j = 0; j <= i ; j++) {
					path = path + paths[j];
				}
				if (!createDir(tmpDir, path).exists()) {
					isMkdirs = false;
					break;
				}
			}
		}
		if (isMkdirs) {
			File mainFile = new File(new File(tmpDir), tmpFilePath);
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(tmpFilePath);
			try {
				OutputStream out = new FileOutputStream(mainFile);
				byte buf[] = new byte[1024];
				int len;
				while ((len = inputStream.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				inputStream.close();
			} catch (Exception e) {
				logger.error("Error preparing NodeJS Hello", e);
			}
		}
	}

	private File createTempDir () {
		final String baseTempPath = System.getProperty("java.io.tmpdir");
		Random rand = new Random();
		int randomInt = 1 + rand.nextInt();
		File tempDir = new File(baseTempPath + File.separator + "tempDir" + randomInt);
		if (tempDir.exists() || tempDir.mkdir()) {
			tempDir.deleteOnExit();
			return tempDir;
		} else {
			return null;
		}
	}

	private File createDir (String tmpDir, String dirPath) {
		File tempDir = new File(tmpDir + File.separator + dirPath);
		if (tempDir.exists() || tempDir.mkdir()) {
			tempDir.deleteOnExit();
			return tempDir;
		} else {
			return null;
		}
	}


}
