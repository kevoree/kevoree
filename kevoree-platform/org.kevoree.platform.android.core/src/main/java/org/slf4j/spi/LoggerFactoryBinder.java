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
package org.slf4j.spi;

import org.slf4j.ILoggerFactory;

/**
 * An internal interface which helps the static {@link org.slf4j.LoggerFactory} 
 * class bind with the appropriate {@link org.slf4j.ILoggerFactory} instance.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public interface LoggerFactoryBinder {

  /**
   * Return the instance of {@link org.slf4j.ILoggerFactory} that
   * {@link org.slf4j.LoggerFactory} class should bind to.
   *
   * @return the instance of {@link org.slf4j.ILoggerFactory} that
   * {@link org.slf4j.LoggerFactory} class should bind to.
   */
  public ILoggerFactory getLoggerFactory();

  /**
   * The String form of the {@link org.slf4j.ILoggerFactory} object that this
   * <code>LoggerFactoryBinder</code> instance is <em>intended</em> to return.
   *
   * <p>This method allows the developer to intterogate this binder's intention
   * which may be different from the {@link org.slf4j.ILoggerFactory} instance it is able to
   * yield in practice. The discrepency should only occur in case of errors.
   *
   * @return the class name of the intended {@link org.slf4j.ILoggerFactory} instance
   */
  public String getLoggerFactoryClassStr();
}
