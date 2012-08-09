/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
package org.slf4j.helpers;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * SubstituteLoggerFactory is an trivial implementation of
 * {@link org.slf4j.ILoggerFactory} which always returns the unique instance of NOPLogger.
 * 
 * <p>
 * It used as a temporary substitute for the real ILoggerFactory during its
 * auto-configuration which may re-enter LoggerFactory to obtain logger
 * instances. See also http://bugzilla.slf4j.org/show_bug.cgi?id=106
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SubstituteLoggerFactory implements ILoggerFactory {

  // keep a record of requested logger names
  final List loggerNameList = new ArrayList();

  public Logger getLogger(String name) {
    synchronized (loggerNameList) {
      loggerNameList.add(name);
    }
    return NOPLogger.NOP_LOGGER;
  }

  public List getLoggerNameList() {
    List copy = new ArrayList();
    synchronized (loggerNameList) {
      copy.addAll(loggerNameList);
    }
    return copy;
  }

}
