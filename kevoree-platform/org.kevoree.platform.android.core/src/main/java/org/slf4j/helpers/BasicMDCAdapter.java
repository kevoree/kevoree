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

import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Basic MDC implementation, which can be used with logging systems that lack
 * out-of-the-box MDC support.
 * 
 * This code is largely based on logback's <a
 * href="http://svn.qos.ch/viewvc/logback/trunk/logback-classic/src/main/java/org/slf4j/impl/LogbackMDCAdapter.java">
 * LogbackMDCAdapter</a>.
 * 
 * @author Ceki Gulcu
 * @author Maarten Bosteels
 * 
 * @since 1.5.0
 */
public class BasicMDCAdapter implements MDCAdapter {

  private InheritableThreadLocal inheritableThreadLocal = new InheritableThreadLocal();

  /**
   * Put a context value (the <code>val</code> parameter) as identified with
   * the <code>key</code> parameter into the current thread's context map.
   * Note that contrary to log4j, the <code>val</code> parameter can be null.
   * 
   * <p>
   * If the current thread does not have a context map it is created as a side
   * effect of this call.
   * 
   * @throws IllegalArgumentException
   *                 in case the "key" parameter is null
   */
  public void put(String key, String val) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }
    HashMap map = (HashMap) inheritableThreadLocal.get();
    if (map == null) {
      map = new HashMap();
      inheritableThreadLocal.set(map);
    }
    map.put(key, val);
  }

  /**
   * Get the context identified by the <code>key</code> parameter.
   */
  public String get(String key) {
    HashMap hashMap = (HashMap) inheritableThreadLocal.get();
    if ((hashMap != null) && (key != null)) {
      return (String) hashMap.get(key);
    } else {
      return null;
    }
  }

  /**
   * Remove the the context identified by the <code>key</code> parameter.
   */
  public void remove(String key) {
    HashMap map = (HashMap) inheritableThreadLocal.get();
    if (map != null) {
      map.remove(key);
    }
  }

  /**
   * Clear all entries in the MDC.
   */
  public void clear() {
    HashMap hashMap = (HashMap) inheritableThreadLocal.get();
    if (hashMap != null) {
      hashMap.clear();
      // the InheritableThreadLocal.remove method was introduced in JDK 1.5
      // Thus, invoking clear() on previous JDK's will fail
      inheritableThreadLocal.remove();
    }
  }

  /**
   * Returns the keys in the MDC as a {@link java.util.Set} of {@link String}s The
   * returned value can be null.
   * 
   * @return the keys in the MDC
   */
  public Set getKeys() {
    HashMap hashMap = (HashMap) inheritableThreadLocal.get();
    if (hashMap != null) {
      return hashMap.keySet();
    } else {
      return null;
    }
  }
  /**
   * Return a copy of the current thread's context map. 
   * Returned value may be null.
   * 
   */
  public Map getCopyOfContextMap() {
    HashMap hashMap = (HashMap) inheritableThreadLocal.get();
    if (hashMap != null) {
      return new HashMap(hashMap);
    } else {
      return null;
    }
  }

  public void setContextMap(Map contextMap) {
    HashMap hashMap = (HashMap) inheritableThreadLocal.get();
    if (hashMap != null) {
      hashMap.clear();
      hashMap.putAll(contextMap);
    } else {
      hashMap = new HashMap(contextMap);
      inheritableThreadLocal.set(hashMap);
    }
  }

}
