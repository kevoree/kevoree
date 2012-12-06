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
package org.kevoree.extra.ecore.loader.test

import org.junit.Test
import org.kevoree.loader.ContainerRootLoader
import java.io.File
import org.kevoree.{ComponentInstance, ContainerNode}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 06/12/12
 * Time: 21:09
 */
class KMFQueryTest {

  @Test
  def testOppositeQuery(){
    val model = ContainerRootLoader.loadModel(new File(getClass.getResource("/unomas.kev").toURI)).get
    assert(model.findNodesByID("node0").getName == "node0")
    assert(model.findById("nodes[node0]").asInstanceOf[ContainerNode].getName == "node0")
    assert(model.findById("nodes[node0]/components[FakeConso145]").asInstanceOf[ComponentInstance].getName == "FakeConso145")
    assert(model.findById("adaptationPrimitiveTypes[UpdateDeployUnit]").asInstanceOf[org.kevoree.AdaptationPrimitiveType].getName == "UpdateDeployUnit")

  }

}
