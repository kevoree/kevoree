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
import org.kevoree.api.service.core.script.KevScriptEngineFactory
import org.kevoree.api.Bootstraper
import org.kevoree.api.service.core.classloading.KevoreeClassLoaderHandler
import org.kevoree.framework.{AbstractGroupType, KevoreeGeneratorHelper, AbstractNodeType}
import org.kevoree.framework.aspects.KevoreeAspects._
import java.io.File
import org.kevoree.{DeployUnit, KevoreeFactory, GroupType, ContainerRoot}
import org.kevoree.extra.jcl.KevoreeJarClassLoader

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class NodeTypeBootstrapHelper extends Bootstraper with KCLBootstrap {

  val classLoaderHandler = new JCLContextHandler

  def bootstrapNodeType(model: ContainerRoot, destNodeName: String, mservice: KevoreeModelHandlerService, kevsEngineFactory: KevScriptEngineFactory): Option[org.kevoree.api.NodeType] = {
    //JCLContextHandler.clear()


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
            nodeType.setBootStrapperService(this)
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
  private def installNodeType(nodeType: org.kevoree.NodeType): Option[ClassLoader] = {
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

  def getKevoreeClassLoaderHandler: KevoreeClassLoaderHandler = {
    classLoaderHandler
  }


  def bootstrapGroupType(model: ContainerRoot, destGroupName: String, mservice: KevoreeModelHandlerService): Option[AbstractGroupType] = {
    //LOCATE NODE
    val optgroup = model.getGroups.find(group => group.getName == destGroupName)
    optgroup match {
      case Some(group) => {
        val groupTypeDeployUnitList = group.getTypeDefinition.getDeployUnits.toList
        if (groupTypeDeployUnitList.size > 0) {


          val kcl = installGroupTyp(group.getTypeDefinition.asInstanceOf[GroupType])
          if (kcl.isDefined) {
            val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(group.getTypeDefinition, "JavaSENode")
            val activatorName = group.getTypeDefinition.getName + "Activator"



            val clazz: Class[_] = kcl.get.loadClass(activatorPackage + "." + activatorName)

            val groupActivator = clazz.newInstance.asInstanceOf[org.kevoree.framework.osgi.KevoreeGroupActivator]
            val groupType = groupActivator.callFactory()

            //ADD INSTANCE DICTIONARY
            val dictionary: java.util.HashMap[String, AnyRef] = new java.util.HashMap[String, AnyRef]

            group.getTypeDefinition.getDictionaryType.map {
              dictionaryType =>
                dictionaryType.getDefaultValues.foreach {
                  dv =>
                    dictionary.put(dv.getAttribute.getName, dv.getValue)
                }
            }
            group.getDictionary.map {
              dictionaryModel =>
                dictionaryModel.getValues.foreach {
                  v =>
                    dictionary.put(v.getAttribute.getName, v.getValue)
                }
            }
            //    dictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bundleContext.getBundle)
            groupType.getDictionary().putAll(dictionary)
            groupType.setName(destGroupName)
            //INJECT SERVICE HANDLER
            //  val sr = bundleContext.getServiceReference(classOf[KevoreeModelHandlerService].getName)
            // if (sr != null) {
            //    val s = bundleContext.getService(sr).asInstanceOf[KevoreeModelHandlerService]
            groupType.setModelService(mservice)
            groupType.asInstanceOf[AbstractGroupType].setBootStrapperService(this)
            //  }
            Some(groupType)
          } else {
            None
          }
        } else {
          logger.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?");
          None
        }
      }
      case None => logger.error("Node not found using name " + destGroupName); None
    }
  }

  private def installGroupTyp(groupType: GroupType): Option[ClassLoader] = {
    val superTypeBootStrap = groupType.getSuperTypes.forall(superType => installGroupTyp(superType.asInstanceOf[GroupType]).isDefined)
    if (superTypeBootStrap) {
      //FAKE NODE TODO
      val fakeNode = KevoreeFactory.createContainerNode
      groupType.eContainer.asInstanceOf[ContainerRoot].getTypeDefinitions.find(td => td.getName == "JavaSENode").map {
        javaseTD =>
          fakeNode.setTypeDefinition(javaseTD)
      }
      val ct = groupType.foundRelevantDeployUnit(fakeNode)
      if (ct != null) {
        var kcl: ClassLoader = null
        val dpRes = ct.getRequiredLibs.forall {
          tp => installDeployUnit(tp).isDefined
        }
        val kcl_opt = installDeployUnit(ct)
        kcl_opt match {
          case Some(k) => {
            kcl = k
          }
          case _ =>
        }
        kcl_opt.isDefined && dpRes
        Some(kcl) //TODO
        //groupType.getDeployUnits.forall(ct => {
        /*
        logger.debug("require lib for " + ct.getUnitName + "->" + ct.getRequiredLibs.size)
        ct.getRequiredLibs.forall {
          tp => installDeployUnit(tp, bundleContext)
        } && installDeployUnit(ct, bundleContext)
        //})*/
      } else {
        None
      }
    } else {
      None
    }
  }

  def resolveArtifact(artId: String, groupId: String, version: String, repos: List[String]): File = AetherUtil.resolveMavenArtifact(artId,groupId,version,repos)

  def resolveKevoreeArtifact(artId: String, groupId: String, version: String): File = AetherUtil.resolveKevoreeArtifact(artId,groupId,version)

  def resolveDeployUnit(du: DeployUnit): File = AetherUtil.resolveDeployUnit(du)

  def close {
    classLoaderHandler.clear()
    classLoaderHandler.stop()
  }

  def clear {
    classLoaderHandler.clear()
  }

  def registerManuallyDeployUnit(artefactID: String,groupID:String,version:String,kcl:KevoreeJarClassLoader){
    val du = KevoreeFactory.createDeployUnit
    du.setUnitName(artefactID)
    du.setGroupName(groupID)
    du.setVersion(version)
    classLoaderHandler.manuallyAddToCache(du,kcl)
  }

}
