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
import org.kevoree.framework.{Constants, KevoreePlatformHelper}
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 22/09/12
 * Time: 07:57
 */
class DictionaryNetworkPortChecker extends CheckerService {
  def check (model: ContainerRoot) = {

    val violations: java.util.List[CheckerViolation] = new util.ArrayList[CheckerViolation]()
    val collectedPort = new util.HashMap[String, util.HashMap[String, util.HashMap[String, Object]]]()

    model.getNodes.foreach {
      instance => {
        instanceCollect(instance, collectedPort, instance.getName)
        instance.getComponents.foreach(c => instanceCollect(c, collectedPort, instance.getName))
      }
    }


    model.getHubs.foreach {
      hub =>
        instanceDCollect(model, hub, collectedPort)
    }

    model.getGroups.foreach {
      g =>
        instanceDCollect(model, g, collectedPort)
    }

    // println(collectedPort)

    import scala.collection.JavaConversions._
    collectedPort.foreach {
      nodeC =>
        nodeC._2.filter(portP => portP._2.size() > 1).foreach {
          portP =>
            val violation: CheckerViolation = new CheckerViolation
            violation.setMessage("Duplicated collected port usage " + portP._1 + "-" + portP._2.toList)
            var objs = List[Object]()
            portP._2.foreach{ pObj =>
                objs = objs ++ List(pObj._2)
            }
            violation.setTargetObjects(objs)
            violations.add(violation)
        }
    }
    violations
  }

  def instanceDCollect (model: ContainerRoot, ist: Instance, collector: util.HashMap[String, util.HashMap[String, util.HashMap[String, Object]]]) {
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


  def instanceCollect (ist: Instance, collector: util.HashMap[String, util.HashMap[String, util.HashMap[String, Object]]], nodeName: String) {
    var portFound: String = null
    if(ist.getTypeDefinition.getDictionaryType()!=null) {
      val dicType = ist.getTypeDefinition.getDictionaryType()
        dicType.getDefaultValues.filter(dv => dv.getAttribute.getName.equals("port") || dv.getAttribute.getName.endsWith("_port") || dv.getAttribute.getName.startsWith("port_")).foreach {
          dv =>
            portFound = dv.getValue
        }
    }
    if(ist.getDictionary() != null) {
      val dic = ist.getDictionary()
        dic.getValues.filter(dv => (dv.getAttribute.getName.equals("port") || dv.getAttribute.getName.endsWith("_port") || dv.getAttribute.getName.startsWith("port_")) && (dv.getTargetNode() == null || dv.getTargetNode().getName() == nodeName))
          .foreach {
          dv =>
            portFound = dv.getValue
        }

    }

    if (portFound != null) {
      var nodeIP = KevoreePlatformHelper.getProperty(ist.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot], nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
      if (nodeIP == "") {
        nodeIP = "localhost"
      }

      var nodeCollector = collector.get(nodeIP)
      if (nodeCollector == null) {
        nodeCollector = new util.HashMap[String, util.HashMap[String, Object]]
        collector.put(nodeIP, nodeCollector)
      }
      var nodePortCollector = nodeCollector.get(portFound)
      if (nodePortCollector == null) {
        nodePortCollector = new util.HashMap[String, Object]
        nodeCollector.put(portFound, nodePortCollector)
      }
      //if (ist.isInstanceOf[ComponentInstance]){
        nodePortCollector.put(nodeName+"."+ist.getName, ist)
      //} else {
      //  nodePortCollector.put(nodeName, ist)
     // }
    }
  }

}
