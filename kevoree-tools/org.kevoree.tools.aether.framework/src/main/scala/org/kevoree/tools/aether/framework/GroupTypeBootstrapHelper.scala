package org.kevoree.tools.aether.framework

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

import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.framework.{KevoreeGeneratorHelper, AbstractGroupType}
import org.kevoree.{KevoreeFactory, GroupType, ContainerRoot}

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class GroupTypeBootstrapHelper extends KCLBootstrap {

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


  /* Bootstrap node type bundle in local osgi environment */
  private def installGroupTyp(groupType: GroupType): Option[ClassLoader] = {
    val superTypeBootStrap = groupType.getSuperTypes.forall(superType => installGroupTyp(superType.asInstanceOf[GroupType]).isDefined)
    if (superTypeBootStrap) {

      import org.kevoree.framework.aspects.KevoreeAspects._
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

}