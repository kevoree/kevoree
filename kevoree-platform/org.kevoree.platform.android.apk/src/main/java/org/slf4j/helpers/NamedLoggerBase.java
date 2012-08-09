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

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves as base class for named logger implementation. More significantly, this
 * class establishes deserialization behavior. See @see #readResolve. 
 * 
 * @author Ceki Gulcu
 * @since 1.5.3
 */
abstract class NamedLoggerBase implements Logger, Serializable {

  private static final long serialVersionUID = 7535258609338176893L;

  protected String name;
  
  public String getName() {
    return name;
  }
  
  /**
   * Replace this instance with a homonymous (same name) logger returned 
   * by LoggerFactory. Note that this method is only called during 
   * deserialization.
   * 
   * <p>
   * This approach will work well if the desired ILoggerFactory is the one
   * references by LoggerFactory. However, if the user manages its logger hierarchy
   * through a different (non-static) mechanism, e.g. dependency injection, then
   * this approach would be mostly counterproductive.
   * 
   * @return logger with same name as returned by LoggerFactory
   * @throws java.io.ObjectStreamException
   */
  protected Object readResolve() throws ObjectStreamException {
    // using getName() instead of this.name works even for
    // NOPLogger
    return LoggerFactory.getLogger(getName());
  }

}
