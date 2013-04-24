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
import java.io.File
import org.kevoree.loader.XMIModelLoader
import org.kevoree.{Group, ComponentInstance, ContainerNode, ContainerRoot}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 06/12/12
 * Time: 21:09
 */
class KMFQueryTest {

  @Test
  def testOppositeQuery(){

    val loader = new XMIModelLoader()
    val model = loader.loadModelFromPath(new File(getClass.getResource("/unomas.kev").toURI)).get(0).asInstanceOf[ContainerRoot]
    assert(model.findNodesByID("node0").getName == "node0")
    assert(model.findByPath("nodes[node0]").asInstanceOf[ContainerNode].getName == "node0")
    assert(model.findByPath("nodes[node0]/components[FakeConso145]").asInstanceOf[ComponentInstance].getName == "FakeConso145")
    assert(model.findByPath("adaptationPrimitiveTypes[UpdateDeployUnit]").asInstanceOf[org.kevoree.AdaptationPrimitiveType].getName == "UpdateDeployUnit")

    assert(model.findNodesByID("node0").getName == "node0")
    assert(model.findByPath("nodes[{node0}]").asInstanceOf[ContainerNode].getName == "node0")
    assert(model.findByPath("nodes[{node0}]/components[{FakeConso145}]").asInstanceOf[ComponentInstance].getName == "FakeConso145")

    //println(model.findByPath("nodes[{node0}]/components[{FakeConso145}]/dictionary[]"))


    assert(model.findByPath("adaptationPrimitiveTypes[{UpdateDeployUnit}]").asInstanceOf[org.kevoree.AdaptationPrimitiveType].getName == "UpdateDeployUnit")

    val model2 = loader.loadModelFromPath(new File(getClass.getResource("/unomas2.kev").toURI)).get(0).asInstanceOf[ContainerRoot]
    assert(model2.findGroupsByID("editor_group").getName == "editor_group")
    assert(model2.findByPath("groups[editor_group]").asInstanceOf[Group].getName == "editor_group")
    assert(model2.findByPath("groups[editor_group]/subNodes[editor_node]").asInstanceOf[ContainerNode].getName == "editor_node")
    //assert(model2.findById("groups[editor_group]/{editor_node}").asInstanceOf[ContainerNode].getName == "editor_node")
    //assert(model2.findById("groups[editor_group]/editor_node").asInstanceOf[ContainerNode].getName == "editor_node")

    //assert(model2.findById("groups[editor_group]/subNodes[editor_node]/components[iaasPage]").asInstanceOf[ComponentInstance].getName == "iaasPage")
    //assert(model2.findById("groups[editor_group]/subNodes[{editor_node}]/components[iaasPage]").asInstanceOf[ComponentInstance].getName == "iaasPage")


  }

}
