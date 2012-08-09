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
package org.slf4j;


/**
 * <code>ILoggerFactory</code> instances manufacture {@link org.slf4j.Logger}
 * instances by name.
 *
 * <p>Most users retrieve {@link org.slf4j.Logger} instances through the static
 * {@link org.slf4j.LoggerFactory#getLogger(String)} method. An instance of of this
 * interface is bound internally with {@link org.slf4j.LoggerFactory} class at
 * compile time.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public interface ILoggerFactory {

  /**
   * Return an appropriate {@link org.slf4j.Logger} instance as specified by the
   * <code>name</code> parameter.
   *
   * <p>If the name parameter is equal to {@link org.slf4j.Logger#ROOT_LOGGER_NAME}, that is
   * the string value "ROOT" (case insensitive), then the root logger of the 
   * underlying logging system is returned.
   * 
   * <p>Null-valued name arguments are considered invalid.
   *
   * <p>Certain extremely simple logging systems, e.g. NOP, may always
   * return the same logger instance regardless of the requested name.
   * 
   * @param name the name of the Logger to return
   */
  public Logger getLogger(String name);
}
