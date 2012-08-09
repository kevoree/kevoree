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

import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticMarkerBinder;

/**
 * MarkerFactory is a utility class producing {@link org.slf4j.Marker} instances as
 * appropriate for the logging system currently in use.
 *
 * <p>
 * This class is essentially implemented as a wrapper around an
 * {@link org.slf4j.IMarkerFactory} instance bound at compile time.
 *
 * <p>
 * Please note that all methods in this class are static.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class MarkerFactory {
  static IMarkerFactory markerFactory;

  private MarkerFactory() {
  }

  static {
    try {
      markerFactory = StaticMarkerBinder.SINGLETON.getMarkerFactory();
    } catch (NoClassDefFoundError e) {
      markerFactory = new BasicMarkerFactory();

    } catch (Exception e) {
      // we should never get here
      Util.report("Unexpected failure while binding MarkerFactory", e);
    }
  }

  /**
   * Return a Marker instance as specified by the name parameter using the
   * previously bound {@link org.slf4j.IMarkerFactory}instance.
   *
   * @param name
   *          The name of the {@link org.slf4j.Marker} object to return.
   * @return marker
   */
  public static Marker getMarker(String name) {
    return markerFactory.getMarker(name);
  }

  /**
   * Create a marker which is detached (even at birth) from the MarkerFactory.
   *
   * @return a dangling marker
   * @since 1.5.1
   */
  public static Marker getDetachedMarker(String name) {
    return markerFactory.getDetachedMarker(name);
  }

  /**
   * Return the {@link org.slf4j.IMarkerFactory}instance in use.
   * 
   * <p>The IMarkerFactory instance is usually bound with this class at 
   * compile time.
   * 
   * @return the IMarkerFactory instance in use
   */
  public static IMarkerFactory getIMarkerFactory() {
    return markerFactory;
  }
}