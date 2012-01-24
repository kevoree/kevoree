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
package org.kevoree.framework;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/01/12
 * Time: 19:15
 */
public interface MavenResolver {
    
    public File resolveKevoreeArtifact(String artId,String groupId, String version);
    public File resolveArtifact(String artId,String groupId, String version, List<String> repos);

}
