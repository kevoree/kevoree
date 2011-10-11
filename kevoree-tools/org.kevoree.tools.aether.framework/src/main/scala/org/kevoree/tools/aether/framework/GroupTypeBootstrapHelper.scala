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
import java.io.FileInputStream
import org.osgi.framework.{Bundle, BundleContext, BundleException}
import org.slf4j.LoggerFactory
import org.kevoree.api.service.core.handler.KevoreeModelHandlerService
import org.kevoree.{GroupType, ContainerRoot, DeployUnit}
import org.kevoree.framework.{KevoreeGeneratorHelper, KevoreeGroup, AbstractGroupType, Constants}

/**
 * User: ffouquet
 * Date: 10/08/11
 * Time: 12:01
 */

class GroupTypeBootstrapHelper {

  private var bundle: Bundle = null
  private val logger = LoggerFactory.getLogger(this.getClass)

  def getGroupTypeBundle = bundle

  def bootstrapGroupType(model: ContainerRoot, destGroupName: String, bundleContext: BundleContext): Option[AbstractGroupType] = {
    //LOCATE NODE
    val optgroup = model.getGroups.find(group => group.getName == destGroupName)
    optgroup match {
      case Some(group) => {
        val groupTypeDeployUnitList = group.getTypeDefinition.getDeployUnits.toList
        if (groupTypeDeployUnitList.size > 0) {
          logger.debug("groupType installation => " + installNodeTyp(group.getTypeDefinition.asInstanceOf[GroupType], bundleContext))

          val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(group.getTypeDefinition, "JavaSENode")
          val activatorName = group.getTypeDefinition.getName + "Activator"
          val clazz: Class[_] = bundle.loadClass(activatorPackage+"."+activatorName)

          val groupActivator = clazz.newInstance.asInstanceOf[org.kevoree.framework.osgi.KevoreeGroupActivator]
          val groupType =  groupActivator.callFactory()

          //ADD INSTANCE DICTIONARY
          val dictionary: java.util.HashMap[String, AnyRef] = new java.util.HashMap[String, AnyRef]

          group.getTypeDefinition.getDictionaryType.map{ dictionaryType =>
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

          dictionary.put(Constants.KEVOREE_PROPERTY_OSGI_BUNDLE, bundleContext.getBundle)

          groupType.getDictionary().putAll(dictionary)
          println("afterSet"+groupType.getDictionary)
          groupType.setName(destGroupName)

          //INJECT SERVICE HANDLER
          val sr = bundleContext.getServiceReference(classOf[KevoreeModelHandlerService].getName)
          if (sr != null) {
            val s = bundleContext.getService(sr).asInstanceOf[KevoreeModelHandlerService]
            groupType.setModelService(s)
          }

          Some(groupType)
        } else {
          logger.error("NodeType deploy unit not found , have you forgotten to merge nodetype library ?");
          None
        }
      }
      case None => logger.error("Node not found using name " + destGroupName); None
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
  private def installNodeTyp(groupType: GroupType, bundleContext: BundleContext): Boolean = {
    val superTypeBootStrap = groupType.getSuperTypes.forall(superType => installNodeTyp(superType.asInstanceOf[GroupType], bundleContext))
    if (superTypeBootStrap) {
      groupType.getDeployUnits.forall(ct => {
        ct.getRequiredLibs.forall {
          tp => installDeployUnit(tp, bundleContext)
        } && installDeployUnit(ct, bundleContext)
      })
    } else {
      false
    }
  }

}