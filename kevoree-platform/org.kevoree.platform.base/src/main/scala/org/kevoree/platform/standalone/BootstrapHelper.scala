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
package org.kevoree.platform.standalone

import org.slf4j.LoggerFactory
import org.kevoree.{ContainerNode, KevoreeFactory, ContainerRoot}
import java.io.File
import org.kevoree.api.service.core.script.KevScriptEngine
import io.Source

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/10/11
 * Time: 13:41
 */

object BootstrapHelper {

  val logger = LoggerFactory.getLogger(this.getClass)

  def initModelInstance(model: ContainerRoot, defType: String, groupType: String) {

    val nodeName = System.getProperty("node.name")
    if (!model.getNodes.exists(n => n.getName == nodeName)) {
      //CREATE DEFAULT
      var node : ContainerNode = null

      model.getTypeDefinitions.find(td => td.getName == defType) match {
        case Some(typeDefFound) => {
          logger.warn("Init default node instance for name " + nodeName)
          node = KevoreeFactory.createContainerNode
          node.setName(nodeName)
          node.setTypeDefinition(typeDefFound)
          model.addNodes(node)
        }
        case None => logger.error("Default type not found for name " + defType)
      }

      if (groupType != null) {
        model.getTypeDefinitions.find(td => td.getName == groupType) match {
          case Some(typeDefFound) => {
            val g = KevoreeFactory.createGroup
            g.setName("sync")
            g.setTypeDefinition(typeDefFound)
            g.addSubNodes(node)
            model.addGroups(g)
          }
          case None => logger.error("Default type not found for name " + defType)
        }
      }


    }
  }


  def generateFromKevS(scriptFile: File , kevEngine : KevScriptEngine): ContainerRoot = {
    kevEngine.addVariable("kevoree.version", KevoreeFactory.getVersion)
    import scala.collection.JavaConversions._
    System.getProperties.foreach{ p=>
      kevEngine.addVariable(p._1, p._2)
    }
    Source.fromFile(scriptFile).getLines().foreach{ line =>
      kevEngine.append(line)
    }
    kevEngine.interpret()
  }


}