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
 * Implementaitons of this interface are used to manufacture {@link org.slf4j.Marker}
 * instances.
 *
 * <p>See the section <a href="http://slf4j.org/faq.html#3">Implementing
 * the SLF4J API</a> in the FAQ for details on how to make your logging
 * system conform to SLF4J.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public interface IMarkerFactory {

  /**
   * Manufacture a {@link org.slf4j.Marker} instance by name. If the instance has been
   * created earlier, return the previously created instance. 
   * 
   * <p>Null name values are not allowed.
   *
   * @param name the name of the marker to be created, null value is
   * not allowed.
   *
   * @return a Marker instance
   */
  Marker getMarker(String name);
  
  /**
   * Checks if the marker with the name already exists. If name is null, then false 
   * is returned.
   * 
   * @return true id the marker exists, false otherwise. 
   */
  boolean exists(String name);
  
  /**
   * Detach an existing marker.
   * <p>
   * Note that after a marker is detached, there might still be "dangling" references
   * to the detached marker.
   * 
   * 
   * @param name The name of the marker to detach
   * @return whether the marker  could be detached or not
   */
  boolean detachMarker(String name);
  
  
  /**
   * Create a marker which is detached (even at birth) from this IMarkerFactory.
   *
   * @return a dangling marker
   * @since 1.5.1
   */
  Marker getDetachedMarker(String name);
}
