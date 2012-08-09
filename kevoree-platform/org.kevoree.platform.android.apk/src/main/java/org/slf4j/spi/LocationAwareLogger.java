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
package org.slf4j.spi;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * An <b>optional</b> interface helping integration with logging systems capable of 
 * extracting location information. This interface is mainly used by SLF4J bridges 
 * such as jcl104-over-slf4j which need to provide hints so that the underlying logging
 * system can extract the correct location information (method name, line number, etc.).
 * 
 * 
 * @author Ceki Gulcu
 * @since 1.3
 */
public interface LocationAwareLogger extends Logger {

  final public int TRACE_INT = 00;
  final public int DEBUG_INT = 10;
  final public int INFO_INT = 20;
  final public int WARN_INT = 30;
  final public int ERROR_INT = 40;
  
  
  /**
   * Printing method with support for location information. 
   * 
   * @param marker
   * @param fqcn The fully qualified class name of the <b>caller</b>
   * @param level
   * @param message
   * @param t
   */  
  public void log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t);
  
}
