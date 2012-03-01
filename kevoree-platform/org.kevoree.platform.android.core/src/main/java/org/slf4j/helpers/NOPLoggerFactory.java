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

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;


/**
 * NOPLoggerFactory is an trivial implementation of {@link
 * org.slf4j.ILoggerFactory} which always returns the unique instance of
 * NOPLogger.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class NOPLoggerFactory implements ILoggerFactory {
  
  public NOPLoggerFactory() {
    // nothing to do
  }
  
  public Logger getLogger(String name) {
    return NOPLogger.NOP_LOGGER;
  }

}
