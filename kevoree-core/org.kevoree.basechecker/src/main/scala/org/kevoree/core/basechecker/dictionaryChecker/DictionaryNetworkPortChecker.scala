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
package org.kevoree.core.basechecker.dictionaryChecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.{ContainerNode, Instance, ContainerRoot}
import java.util
import org.kevoree.framework.{Constants, KevoreePropertyHelper, KevoreePlatformHelper}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/09/12
 * Time: 07:57
 */
class DictionaryNetworkPortChecker extends CheckerService {
  def check(model: ContainerRoot) = {
    val violations: java.util.List[CheckerViolation] = new util.ArrayList[CheckerViolation]()
    val collectedPort = new util.HashMap[String, util.HashMap[String, util.HashSet[Object]]]()
    model.getNodes.foreach {
      instance => {
        instanceCollect(instance, collectedPort, instance.getName)
        instance.getComponents.foreach(c => instanceCollect(c, collectedPort, instance.getName))
      }
    }
    model.getHubs.foreach {hub =>
      instanceDCollect(hub, collectedPort)
    }
    model.getGroups.foreach {g =>
      instanceDCollect(g, collectedPort)
    }
    import scala.collection.JavaConversions._
    collectedPort.foreach{ nodeC =>
      nodeC._2.filter(portP => portP._2.size() >1).foreach{ portP =>
        val violation: CheckerViolation = new CheckerViolation
        violation.setMessage("Duplicated collected port usage "+portP._1)
        violation.setTargetObjects(portP._2.toList)
        violations.add(violation)
      }
    }
    violations
  }

  def instanceDCollect(ist: Instance, collector: util.HashMap[String, util.HashMap[String, util.HashSet[Object]]]) {
    val nodeConnected = new util.ArrayList[String]()
    ist match {
      case c: org.kevoree.Channel => {
        c.eContainer.asInstanceOf[ContainerRoot].getMBindings.filter(mb => mb.getHub == c).foreach {
          mb =>
            nodeConnected.add(mb.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
        }
      }
      case g: org.kevoree.Group => {
        g.getSubNodes.foreach {
          sub =>
            nodeConnected.add(sub.getName)
        }
      }
    }
    import scala.collection.JavaConversions._
    nodeConnected.foreach {
      node =>
        instanceCollect(ist, collector, node)
    }
  }


  def instanceCollect(ist: Instance, collector: util.HashMap[String, util.HashMap[String, util.HashSet[Object]]], nodeName: String) {
    var portFound: String = null
    ist.getTypeDefinition.getDictionaryType.map {
      dicType =>
        dicType.getDefaultValues.filter(dv => dv.getAttribute.getName == "port").foreach {
          dv =>
            portFound = dv.getValue
        }
    }
    ist.getDictionary.map {
      dic => {
        dic.getValues.filter(dv => dv.getAttribute.getName == "port" && (dv.getTargetNode.getOrElse(nodeName) == nodeName)).foreach {
          dv =>
            portFound = dv.getValue
        }
      }
    }
    if (portFound != null) {
      val ipFound = KevoreePropertyHelper.getNetworkProperty(ist.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot],nodeName,Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP).getOrElse("localhost").toString
      val nodeIDMethod = ipFound

      var nodeCollector = collector.get(nodeIDMethod)
      if (nodeCollector == null) {
        nodeCollector = new util.HashMap[String, util.HashSet[Object]]
        collector.put(nodeIDMethod, nodeCollector)
      }
      var nodePortCollector = nodeCollector.get(portFound)
      if (nodePortCollector == null) {
        nodePortCollector = new util.HashSet[Object]
        nodeCollector.put(portFound, nodePortCollector)
      }
      nodePortCollector.add(ist)
    }
  }

}
