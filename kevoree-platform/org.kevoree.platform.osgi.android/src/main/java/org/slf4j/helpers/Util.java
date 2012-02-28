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


/**
 *
 * An internal utility class.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class Util {
    
  static final public void report(String msg, Throwable t) {
    System.err.println(msg);
    System.err.println("Reported exception:");
    t.printStackTrace();
  }
  
  static final public void report(String msg) {
    System.err.println("SLF4J: " +msg);
  }
}
