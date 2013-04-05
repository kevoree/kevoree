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
package org.kevoree.tools.ui.editor.command;

import org.kevoree.tools.aether.framework.AetherUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 2/5/13
 * Time: 2:16 PM
 */
public class AetherResolver {

    public static File resolve(String u,String g,String v,List<String> urls){
        return AetherUtil.instance$.resolveMavenArtifact(u,g,v,urls);
    }

    public static File resolveKev(String u,String g,String v){
        return AetherUtil.instance$.resolveMavenArtifact(u,g,v,Arrays.asList("http://maven.kevoree.org/release","http://maven.kevoree.org/snapshots"));
    }

}
