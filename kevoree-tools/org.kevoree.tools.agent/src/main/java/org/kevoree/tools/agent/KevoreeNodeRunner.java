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

import org.ops4j.pax.url.mvn.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class KevoreeNodeRunner {

	private Logger logger = LoggerFactory.getLogger(KevoreeNodeRunner.class);

	private Process nodePlatformProcess;
	private String platformJARPath;

	public KevoreeNodeRunner(String nodeName, Integer basePort) {

		//URL url = new URL(null,"cvs://server/project/folder#version", new PaxMvnUrlStreamHandlerFactory());
		URL.setURLStreamHandlerFactory(new PaxMvnUrlStreamHandlerFactory());

	}

	public void startNode(String nodeName, Integer basePort) {
		if (platformJARPath == null) {
			getJar();
		}
		System.out.println("");

	}

	public void stopKillNode() {
		try {
			nodePlatformProcess.getOutputStream().write("stop 0".getBytes());
			nodePlatformProcess.getOutputStream().flush();
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error("The node cannot be killed. Try to force kill", e.getCause());
			nodePlatformProcess.destroy();
		}
	}

	private void getJar() {
		try {
			URL mvnURL = new URL("mvn:org.kevoree.platform/org.kevoree.platform.osgi.standalone	");
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class PaxMvnUrlStreamHandlerFactory implements URLStreamHandlerFactory {

		Handler handler;

		public PaxMvnUrlStreamHandlerFactory() {
			handler = new Handler();
		}

		@Override
		public URLStreamHandler createURLStreamHandler(String protocol) {
				return handler;
		}
	}

}
