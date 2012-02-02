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

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.ContainerRoot
import org.kevoree.framework.{Bootstraper, AbstractNodeType}
import org.kevoree.api.service.core.script.KevScriptEngineFactory

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class NodeTypeBootstrapHelper extends Bootstraper with KCLBootstrap {

  def bootstrapNodeType (model: ContainerRoot, destNodeName: String, mservice: KevoreeModelHandlerService, kevsEngineFactory: KevScriptEngineFactory): Option[org.kevoree.framework.NodeType] = {
    JCLContextHandler.clear()
    //LOCATE NODE
    val nodeOption = model.getNodes.find(node => node.getName == destNodeName)
    nodeOption match {
      case Some(node) => {
        val nodeTypeDeployUnitList = node.getTypeDefinition.getDeployUnits.toList
        if (nodeTypeDeployUnitList.size > 0) {
          val classLoader = installNodeType(node.getTypeDefinition.asInstanceOf[org.kevoree.NodeType])
          if (classLoader.isDefined) {
            val clazz: Class[_] = classLoader.get.loadClass(node.getTypeDefinition.getBean)
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
            //   dictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bundleContext.getBundle)
            nodeType.setDictionary(dictionary)
            nodeType.setNodeName(destNodeName)
            //INJECT SERVICE HANDLER
            //  val sr = bundleContext.getServiceReference(classOf[KevoreeModelHandlerService].getName)
            // if (sr != null) {
            //   val s = bundleContext.getService(sr).asInstanceOf[KevoreeModelHandlerService]
            nodeType.setModelService(mservice)
            nodeType.setKevScriptEngineFactory(kevsEngineFactory)
            // }
            //nodeType.push(destNodeName, model, bundle.getBundleContext)
            Some(nodeType)
          } else {
            None
          }
          //KevoreeDeployManager.addMapping(KevoreeOSGiBundle(node.getTypeDefinition.getName, node.getTypeDefinition.getClass.getName, lastBundleID))
        } else {
          logger.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?");
          None
        }
      }
      case None => logger.error("Node not found using name " + destNodeName); None
    }
  }


  /* Bootstrap node type bundle in local environment */
  private def installNodeType (nodeType: org.kevoree.NodeType): Option[ClassLoader] = {
    val superTypeBootStrap = nodeType.getSuperTypes
      .forall(superType => installNodeType(superType.asInstanceOf[org.kevoree.NodeType]).isDefined)
    if (superTypeBootStrap) {
      var kcl: ClassLoader = null
      nodeType.getDeployUnits.forall(ct => {
        val dpRes = ct.getRequiredLibs.forall(tp => {
          val idp = installDeployUnit(tp)
          idp.isDefined
        })
        /*
        groupKCL.foreach(gcl =>
          groupKCL.filter(g => g != gcl).foreach(
            gcl_in => {
              gcl.addWeakClassLoader(gcl_in)
            }
          )
        )*/

        val kcl_opt = installDeployUnit(ct)
        kcl_opt match {
          case Some(k) => {
            kcl = k
          }
          case _ =>
        }
        kcl_opt.isDefined && dpRes
      })
      Some(kcl) //TODO
    } else {
      logger.error("Super type of " + nodeType.getName + " was not completely installed")
      None
    }
  }


}
