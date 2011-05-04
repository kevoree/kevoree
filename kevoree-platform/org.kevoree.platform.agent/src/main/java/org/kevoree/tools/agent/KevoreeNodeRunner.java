/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.tools.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class KevoreeNodeRunner {

	private Logger logger = LoggerFactory.getLogger(KevoreeNodeRunner.class);

	private Process nodePlatformProcess;
	private String platformJARPath;

	private String nodeName;
	private Integer basePort;

	public KevoreeNodeRunner(String nodeName, Integer basePort) {

		//URL url = new URL(null,"cvs://server/project/folder#version", new PaxMvnUrlStreamHandlerFactory());
		//System.setProperty("org.ops4j.pax.url.mvn.defaultRepositories", "http://maven.kevoree.org/release");
		//URL.setURLStreamHandlerFactory(new PaxMvnUrlStreamHandlerFactory()); // to use the maven URL handler
		this.nodeName = nodeName;
		this.basePort = basePort;

	}

	public void startNode() {
		try {
			System.out.println("StartNodeCommand");
			if (platformJARPath == null) {
				getJar();
			}
			nodePlatformProcess = Runtime.getRuntime().exec(new String[]{"java", "-Dnode.name=" + nodeName, "-Dnode.port=" + basePort, "-jar", platformJARPath});
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void stopKillNode() {
		System.out.println("KillNodeCommand");
		try {
			nodePlatformProcess.getOutputStream().write("stop 0".getBytes());
			nodePlatformProcess.getOutputStream().flush();
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error("The node cannot be killed. Try to force kill", e.getCause());
			nodePlatformProcess.destroy();
		}
	}

	/*private void getJar() { // from maven(doesn't work ...)
		try {
			URL mvnURL = new URL("mvn:http://maven.kevoree.org/release/!org.kevoree.platform/org.kevoree.platform.osgi.standalone");
			InputStream stream = mvnURL.openConnection().getInputStream();

			File f = File.createTempFile("org.kevoree.platform.osgi.standalone", ".jar");
			f.deleteOnExit();
			OutputStream outputStream = new FileOutputStream(f);

			byte[] bytes = new byte[1024];
			int length = 0;
			while ((length = stream.read()) != -1) {
				outputStream.write(bytes, 0, length);
			}
			outputStream.flush();
			outputStream.close();
			stream.close();

			platformJARPath = f.getAbsolutePath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	/*private void getJar() { // from the availables resources into the jar
		try {
			InputStream stream = this.getClass().getClassLoader().getResourceAsStream("org.kevoree.platform.osgi.standalone.jar");

			File f = new File(defaultJarFilePath);
			f.deleteOnExit();
			OutputStream outputStream = new FileOutputStream(f);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = stream.read()) != -1) {
				outputStream.write(bytes, 0, length);
			}
			outputStream.flush();
			outputStream.close();
			stream.close();

			platformJARPath = f.getAbsolutePath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	private void getJar() throws IOException {
		String jarLocation = System.getProperty("kevoree.location");
		if (jarLocation == null) {
			jarLocation = System.getProperty("user.dir") + "org.kevoree.platform.osgi.standalone" + getVersion() + ".jar";
		}
		if (new File(jarLocation).exists()) {
			platformJARPath = jarLocation;
		} else {
			throw new FileNotFoundException(jarLocation + " doesn't exist");
		}
	}

	private String getVersion() throws IOException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("META-INF/maven/org.kevoree.platform/org.kevoree.platform.agent/pom.properties");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		byte[] bytes = new byte[1024];
		int length;
		while ((length = stream.read()) != -1) {
			outputStream.write(bytes, 0, length);
		}
		outputStream.flush();
		outputStream.close();
		stream.close();

		StringBuilder builder = new StringBuilder(outputStream.toString());
		int index = builder.indexOf("version=");
		builder.delete(0, index);
		int end = builder.indexOf(System.getProperty("line.separator"));
		builder.delete(end, builder.length());
		int equalsSign = builder.indexOf("=");
		builder.delete(0, equalsSign + 1);

		return builder.toString();
	}

	/*public class PaxMvnUrlStreamHandlerFactory implements URLStreamHandlerFactory {

		Handler handler;

		public PaxMvnUrlStreamHandlerFactory() {
			handler = new Handler();
		}

		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
			return handler;
		}
	}*/
}
