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

import java.io.IOException;

public class KevoreeNodeRunner {

	private Logger logger = LoggerFactory.getLogger(KevoreeNodeRunner.class);

	private Process nodePlatformProcess;

	public KevoreeNodeRunner(String nodeName, Integer basePort) {

	}

	public void startNode() {
		
	
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

}
