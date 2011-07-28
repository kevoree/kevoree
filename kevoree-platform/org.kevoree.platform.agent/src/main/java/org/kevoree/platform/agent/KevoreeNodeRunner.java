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
package org.kevoree.platform.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class KevoreeNodeRunner {

	private Logger logger = LoggerFactory.getLogger(KevoreeNodeRunner.class);

	private Process nodePlatformProcess;
	private Thread outputStreamReader;
	private Thread errorStreamReader;
	private static String platformJARPath;

	private String nodeName;
	private Integer basePort;
    private String bootStrapModel;

	public KevoreeNodeRunner(String nodeName, Integer basePort, String bootStrapModel) {

		//URL url = new URL(null,"cvs://server/project/folder#version", new PaxMvnUrlStreamHandlerFactory());
		//System.setProperty("org.ops4j.pax.url.mvn.defaultRepositories", "http://maven.kevoree.org/release");
		//URL.setURLStreamHandlerFactory(new PaxMvnUrlStreamHandlerFactory()); // to use the maven URL handler
		this.nodeName = nodeName;
		this.basePort = basePort;
        this.bootStrapModel = bootStrapModel;
	}

	public void startNode() {
		try {
			System.out.println("StartNodeCommand");
			if (platformJARPath == null) {
				getJar();
			}
			String java = getJava();
			//System.out.println(java + " -Dnode.bootstrap="+bootStrapModel + " -Dnode.name=" + nodeName + " -Dnode.port=" + basePort + "-jar" + platformJARPath);
			nodePlatformProcess = Runtime.getRuntime().exec(new String[]{java,"-Dnode.bootstrap="+bootStrapModel, "-Dnode.name=" + nodeName, "-Dnode.port=" + basePort, "-jar", platformJARPath});
			//nodePlatformProcess = Runtime.getRuntime().exec(new String[]{"screen", "-A", "-m", "-d", "-S", nodeName, "java", "-Dnode.bootstrap="+bootStrapModel, "-Dnode.name="+nodeName, "-Dnode.port="+basePort, "-jar", platformJARPath, "2>&1", "|", "tee file.txt"});

			outputStreamReader = new Thread() {

				//private OutputStream outputStream;
				private InputStream stream = nodePlatformProcess.getInputStream();

				@Override
				public void run () {
					try {
				byte[] bytes = new byte[512];
				int i;
				while (true) {
					i = stream.read(bytes);
					/*if (i != -1) {
						//System.out.print(line);
						//outputStream.write(bytes, 0, i);

					}*/

				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
				}
			};

			errorStreamReader = new Thread() {

				//private OutputStream outputStream;
				private InputStream stream = nodePlatformProcess.getErrorStream();

				@Override
				public void run () {
					try {
				byte[] bytes = new byte[512];
				int i;
				while (true) {
					i = stream.read(bytes);
					/*if (i != -1) {
						//System.out.print(line);
						//outputStream.write(bytes, 0, i);

					}*/

				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
				}
			};

			outputStreamReader.start();
			errorStreamReader.start();

			//System.out.println("Node Started ! ");

			 System.out.println(nodePlatformProcess.exitValue());
		} catch (IOException e) {
			e.printStackTrace();
			//System.exit(1);
		} catch (IllegalThreadStateException e) {
			System.out.println("platform " + nodeName + ":" + basePort + " is running");
		}

	}

	public void stopKillNode() {
		System.out.println("KillNodeCommand");
		try {
			nodePlatformProcess.getOutputStream().write("stop 0".getBytes());
			nodePlatformProcess.getOutputStream().flush();
			Thread.sleep(1000);
			int exitValue = nodePlatformProcess.exitValue();
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error("The node cannot be killed. Try to force kill");
			nodePlatformProcess.destroy();
		} catch (InterruptedException e) {
			//e.printStackTrace();
			logger.error("The node cannot be killed. Try to force kill");
			nodePlatformProcess.destroy();
		} catch (IllegalThreadStateException e) {
			//e.printStackTrace();
			logger.error("The node cannot be killed. Try to force kill");
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
		System.out.println("Init jar platform");
		String jarLocation = System.getProperty("kevoree.location");
		if (jarLocation == null) {
			jarLocation = System.getProperty("user.dir") + File.separatorChar + "org.kevoree.platform.osgi.standalone" + "-" + getVersion() + ".jar";
		}
		if (new File(jarLocation).exists()) {
			platformJARPath = jarLocation;
		} else {
			throw new FileNotFoundException(jarLocation + " doesn't exist");
		}
		System.out.println("Init jar platform: " + platformJARPath + " => OK");
	}

	private String getVersion() throws IOException {
		//System.out.println("GetVersion");
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("META-INF/maven/org.kevoree.platform/org.kevoree.platform.agent/pom.properties");
		Properties prop = new Properties();
		prop.load(stream);

		return prop.getProperty("version");
	}

	private String getJava() {
		String java_home = System.getProperty("java.home");
		//System.out.println(java_home + File.separator + "bin" + File.separator + "java");
		return java_home + File.separator + "bin" + File.separator + "java";
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
