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
import org.kevoree.{ ContainerRoot, DeployUnit}
import org.kevoree.framework.context.{KevoreeOSGiBundle, KevoreeDeployManager}
import org.kevoree.framework.{Bootstraper, Constants, AbstractNodeType}
import reflect.BeanProperty

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class NodeTypeBootstrapHelper extends Bootstraper {

  def this(bc : BundleContext){
    this()
    setBootstrapBundleContext(bc)
  }

  private var bundle: Bundle = null
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getNodeTypeBundle = bundle

  def bootstrapNodeType(model: ContainerRoot, destNodeName: String,
                        bundleContext: BundleContext): Option[AbstractNodeType] = {
    //LOCATE NODE
    val node = model.getNodes.find(node => node.getName == destNodeName)
    node match {
      case Some(node) => {
        val nodeTypeDeployUnitList = node.getTypeDefinition.getDeployUnits.toList
        if (nodeTypeDeployUnitList.size > 0) {
          logger.debug("nodeType installation => " +
            installNodeTyp(node.getTypeDefinition.asInstanceOf[org.kevoree.NodeType], bundleContext))
          //KevoreeDeployManager.addMapping(KevoreeOSGiBundle(node.getTypeDefinition.getName, node.getTypeDefinition.getClass.getName, lastBundleID))
          val clazz: Class[_] = bundle.loadClass(node.getTypeDefinition.getBean)


          val nodeType = clazz.newInstance.asInstanceOf[AbstractNodeType]
          //ADD INSTANCE DICTIONARY
          val dictionary: java.util.HashMap[String, AnyRef] = new java.util.HashMap[String, AnyRef]

          node.getTypeDefinition.getDictionaryType.map {
            dictionaryType =>
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

  var bundles = List[Bundle]()

  private def installDeployUnit(du: DeployUnit, bundleContext: BundleContext): Boolean = {
    try {
      val arteFile = AetherUtil.resolveDeployUnit(du)
      if (arteFile != null) {
        logger.debug("trying to install " + arteFile.getAbsolutePath)
        bundle = bundleContext.installBundle("file:///" + arteFile.getAbsolutePath, new FileInputStream(arteFile))
        //        bundle.start()
        KevoreeDeployManager.addMapping(KevoreeOSGiBundle(buildKEY(du), du.getClass.getName, bundle.getBundleId))
        bundles = bundles ++ List[Bundle](bundle)
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


  private def startDeployUnits(): Boolean = {
    try {
      bundles.foreach {
        bundle =>
          logger.debug("starting " + bundle.getSymbolicName)
          bundle.start()
      }
      true
    } catch {
      case _@e => logger.error("Can't install node type", e); false
    }
  }


  /* Bootstrap node type bundle in local osgi environment */
  private def installNodeTyp(nodeType: org.kevoree.NodeType, bundleContext: BundleContext): Boolean = {
    val superTypeBootStrap = nodeType.getSuperTypes
      .forall(superType => installNodeTyp(superType.asInstanceOf[org.kevoree.NodeType], bundleContext))
    if (superTypeBootStrap) {
      bundles = List[Bundle]()
      nodeType.getDeployUnits.forall(ct => {
        ct.getRequiredLibs.forall {
          tp => installDeployUnit(tp, bundleContext)
        } && startDeployUnits() && installDeployUnit(ct, bundleContext)
      })
    } else {
      logger.error("Super type of " + nodeType.getName + " was not completely installed")
      false
    }
  }


  def buildKEY(du: DeployUnit): String = {
    du.getName + "/" + buildQuery(du, None)
  }

  def buildQuery(du: DeployUnit, repoUrl: Option[String]): String = {
    val query = new StringBuilder
    query.append("mvn:")
    repoUrl match {
      case Some(r) => query.append(r); query.append("!")
      case None =>
    }
    query.append(du.getGroupName)
    query.append("/")
    query.append(du.getUnitName)
    du.getVersion match {
      case "default" =>
      case "" =>
      case _ => query.append("/"); query.append(du.getVersion)
    }
    query.toString
  }

  var bootstrapBundleContext : BundleContext = null

  def setBootstrapBundleContext(bc : BundleContext){
    bootstrapBundleContext = bc
  }


  def bootstrapNodeType(currentModel: ContainerRoot, nodeName: String): Option[org.kevoree.framework.NodeType] = {
    bootstrapNodeType(currentModel, nodeName, bootstrapBundleContext)
  }
}
