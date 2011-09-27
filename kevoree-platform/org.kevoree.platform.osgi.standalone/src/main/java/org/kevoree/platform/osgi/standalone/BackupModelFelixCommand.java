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
package org.kevoree.platform.osgi.standalone;

import org.apache.felix.shell.Command;
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.StringTokenizer;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 27/09/11
 * Time: 13:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public class BackupModelFelixCommand implements Command {
	private static final Logger logger = LoggerFactory.getLogger(SendModelFelixCommand.class);

	private KevoreeModelHandlerService handler;

	public BackupModelFelixCommand(KevoreeModelHandlerService handler) {
		this.handler = handler;
	}

	public void backupModel (String model) {
		try {
			KevoreeXmiHelper.save(model, handler.getLastModel());
		} catch (Exception e) {
			logger.error("Unable to backup model", e);
		}

	}

	@Override
	public String getName () {
		return "backupModel";
	}

	@Override
	public String getUsage () {
		return "backupModel <model file path>";
	}

	@Override
	public String getShortDescription () {
		return "backupModel to the platform";
	}

	@Override
	public void execute (String line, PrintStream out, PrintStream err) {
		StringTokenizer st = new StringTokenizer(line, " ");
		if (st.countTokens() == 2) {
			// Ignore the command name.
			st.nextToken();
			backupModel(st.nextToken());
		} else {
			out.println("Unable to execute command (Invalid number of parameters\n" + "Usage: " + getUsage());
		}
	}
}
