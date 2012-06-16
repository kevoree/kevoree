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
 * A direct NOP (no operation) implementation of {@link org.slf4j.Logger}.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class NOPLogger extends MarkerIgnoringBase {

  private static final long serialVersionUID = -517220405410904473L;

  /**
   * The unique instance of NOPLogger.
   */
  public static final NOPLogger NOP_LOGGER = new NOPLogger();

  /**
   * There is no point in creating multiple instances of NOPLOgger, 
   * except by derived classes, hence the protected  access for the constructor.
   */
  protected NOPLogger() {
  }

  /**
   * Always returns the string value "NOP".
   */
  public String getName() {
    return "NOP";
  }

  /**
   * Always returns false.
   * @return always false
   */
  final public boolean isTraceEnabled() {
    return false;
  }

  /** A NOP implementation. */
  final public void trace(String msg) {
    // NOP
  }

  /** A NOP implementation.  */
  final public void trace(String format, Object arg) {
    // NOP
  }

  /** A NOP implementation.  */
  public final void trace(String format, Object arg1, Object arg2) {
    // NOP
  }

  /** A NOP implementation.  */
  public final void trace(String format, Object[] argArray) {
    // NOP
  }
  
  /** A NOP implementation. */
  final public void trace(String msg, Throwable t) {
    // NOP
  }

  /**
   * Always returns false.
   * @return always false
   */
  final public boolean isDebugEnabled() {
    return false;
  }

  /** A NOP implementation. */
  final public void debug(String msg) {
    // NOP
  }

  /** A NOP implementation.  */
  final public void debug(String format, Object arg) {
    // NOP
  }

  /** A NOP implementation.  */
  public final void debug(String format, Object arg1, Object arg2) {
    // NOP
  }

  /** A NOP implementation.  */
  public final void debug(String format, Object[] argArray) {
    // NOP
  }
  
  
  
  /** A NOP implementation. */
  final public void debug(String msg, Throwable t) {
    // NOP
  }

  /**
   * Always returns false.
   * @return always false
   */
  final public boolean isInfoEnabled() {
    // NOP
    return false;
  }


  /** A NOP implementation. */
  final public void info(String msg) {
    // NOP
  }

  /** A NOP implementation. */
  final  public void info(String format, Object arg1) {
    // NOP
  }

  /** A NOP implementation. */
  final public void info(String format, Object arg1, Object arg2) {
    // NOP
  }
  
  /** A NOP implementation.  */
  public final void info(String format, Object[] argArray) {
    // NOP
  }


  /** A NOP implementation. */
  final public void info(String msg, Throwable t) {
    // NOP
  }


  /**
   * Always returns false.
   * @return always false
   */
  final public boolean isWarnEnabled() {
    return false;
  }

  /** A NOP implementation. */
  final public void warn(String msg) {
    // NOP
  }

  /** A NOP implementation. */
  final public void warn(String format, Object arg1) {
    // NOP
  }

  /** A NOP implementation. */
  final public void warn(String format, Object arg1, Object arg2) {
    // NOP
  }
  
  /** A NOP implementation.  */
  public final void warn(String format, Object[] argArray) {
    // NOP
  }


  /** A NOP implementation. */
  final public void warn(String msg, Throwable t) {
    // NOP
  }


  /** A NOP implementation. */
  final public boolean isErrorEnabled() {
    return false;
  }

  /** A NOP implementation. */
  final public void error(String msg) {
    // NOP
  }

  /** A NOP implementation. */
  final public void error(String format, Object arg1) {
    // NOP
  }

  /** A NOP implementation. */
  final public void error(String format, Object arg1, Object arg2) {
    // NOP
  }
  
  /** A NOP implementation.  */
  public final void error(String format, Object[] argArray) {
    // NOP
  }


  /** A NOP implementation. */
  final public void error(String msg, Throwable t) {
    // NOP
  }
}
