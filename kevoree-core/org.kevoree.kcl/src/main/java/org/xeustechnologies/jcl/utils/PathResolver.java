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
package org.xeustechnologies.jcl.utils;

/**
 * Resolves the path to jar sources. Used by XmlContextLoader.
 * 
 * @author Kamran
 * 
 */
public interface PathResolver {

    /**
     * Resolves the path to class/jar source. Could return multiple file
     * paths/streams/urls.
     * 
     * Returns null if path could not be resolved.
     * 
     * @param path
     * @return Object[]
     */
    public Object[] resolvePath(String path);
}
