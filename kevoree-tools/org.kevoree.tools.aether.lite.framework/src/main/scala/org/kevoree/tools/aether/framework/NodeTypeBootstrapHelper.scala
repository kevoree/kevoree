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
package org.kevoree.tools.aether.framework

import java.io.FileInputStream
import org.osgi.framework.{Bundle, BundleContext, BundleException}
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.{Constants, AbstractNodeType}
import org.kevoree.{NodeType, ContainerRoot, DeployUnit}

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class NodeTypeBootstrapHelper {

  private var bundle: Bundle = null
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getNodeTypeBundle = bundle

  def bootstrapNodeType(model: ContainerRoot, destNodeName: String, bundleContext: BundleContext): Option[AbstractNodeType] = {
    //LOCATE NODE
    val node = model.getNodes.find(node => node.getName == destNodeName)
    node match {
      case Some(node) => {
        val nodeTypeDeployUnitList = node.getTypeDefinition.getDeployUnits.toList
        if (nodeTypeDeployUnitList.size > 0) {
          logger.debug("nodeType installation => " + installNodeTyp(node.getTypeDefinition.asInstanceOf[NodeType], bundleContext))
          val clazz: Class[_] = bundle.loadClass(node.getTypeDefinition.getBean)
          val nodeType = clazz.newInstance.asInstanceOf[AbstractNodeType]
          //ADD INSTANCE DICTIONARY
          val dictionary: java.util.HashMap[String, AnyRef] = new java.util.HashMap[String, AnyRef]

         node.getTypeDefinition.getDictionaryType.map{ dictionaryType =>
            dictionaryType.getDefaultValues.foreach {
                dv =>
                  dictionary.put(dv.getAttribute.getName, dv.getValue)
              }
         }

          node.getDictionary.map {
            dictionaryModel =>
              dictionaryModel.getValues.foreach {
                v =>
                  dictionary.put(v.getAttribute.getName, v.getValue)
              }
          }

          dictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bundleContext.getBundle)
          nodeType.setDictionary(dictionary)
          nodeType.setNodeName(destNodeName)


          //INJECT SERVICE HANDLER
          val sr = bundleContext.getServiceReference(classOf[KevoreeModelHandlerService].getName)
          if (sr != null) {
            val s = bundleContext.getService(sr).asInstanceOf[KevoreeModelHandlerService]
            nodeType.setModelService(s)
          }


          //nodeType.push(destNodeName, model, bundle.getBundleContext)
          Some(nodeType)
        } else {
          logger.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?");
          None
        }
      }
      case None => logger.error("Node not found using name " + destNodeName); None
    }
  }

  private def installDeployUnit(du: DeployUnit, bundleContext: BundleContext): Boolean = {
    try {
      val arteFile = AetherUtil.resolveDeployUnit(du)
      if (arteFile != null) {
        bundle = bundleContext.installBundle("file:///" + arteFile.getAbsolutePath, new FileInputStream(arteFile))
        bundle.start()
        true
      } else {
        logger.error("Can't resolve node type")
        false
      }

    } catch {
      case e: BundleException if (e.getType == BundleException.DUPLICATE_BUNDLE_ERROR) => {
        bundle.update()
        true
      }
      case _@e => {
        logger.error("Can't install node type", e)
        false
      }
    }
  }

  /* Bootstrap node type bundle in local osgi environment */
  private def installNodeTyp(nodeType: NodeType, bundleContext: BundleContext): Boolean = {
    val superTypeBootStrap = nodeType.getSuperTypes.forall(superType => installNodeTyp(superType.asInstanceOf[NodeType], bundleContext))
    if (superTypeBootStrap) {
      nodeType.getDeployUnits.forall(ct => {
        ct.getRequiredLibs.forall {
          tp => installDeployUnit(tp, bundleContext)
        } && installDeployUnit(ct, bundleContext)
      })
    } else {
      false
    }
  }

}