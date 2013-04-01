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
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.merger

import org.kevoree.framework.kaspects.ContainerRootAspect
import resolver._
import org.kevoree._
import resolver.UnresolvedDictionaryAttribute
import resolver.UnresolvedNode
import resolver.UnresolvedNodeType
import resolver.UnresolvedPortTypeRef
import resolver.UnresolvedTypeDefinition
import scala.collection.JavaConversions._
import java.util

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 09/10/11
 * Time: 22:18
 */

trait CrossReferenceMerger {

  private val containerRootAspect = new ContainerRootAspect()

  def breakCrossRef(actualModel: ContainerRoot, modelToMerge: ContainerRoot) {

    //BREAK TOPOLOGY MODEL
    (actualModel.getNodeNetworks.toList ++ modelToMerge.getNodeNetworks).foreach {
      nn =>
        val initByNode = nn.getInitBy()
        if(initByNode != null) {
            nn.setInitBy(new UnresolvedNode(initByNode.getName(),initByNode.path()))
        }
        nn.setTarget(new UnresolvedNode(nn.getTarget.getName,nn.getTarget.path()))
    }

    //BREAK DEPLOY TARGET NODE TYPE
    (actualModel.getDeployUnits.toList ++ modelToMerge.getDeployUnits).foreach {
      dp =>
        val targetNodeType = dp.getTargetNodeType()
        if (targetNodeType!=null){
            dp.setTargetNodeType(new UnresolvedNodeType(targetNodeType.getName()))
        }
    }
    //BREAK EVERY CROSS REFERENCE
    (actualModel.getLibraries.toList ++ modelToMerge.getLibraries).foreach {
      library =>
        val subTypes = library.getSubTypes
        library.removeAllSubTypes()
        subTypes.foreach {
          libSubType =>
            library.addSubTypes(new UnresolvedTypeDefinition(libSubType.getName))
        }
    }
    (actualModel.getTypeDefinitions.toList ++ modelToMerge.getTypeDefinitions).foreach {
      typeDef =>
        typeDef.getSuperTypes.foreach {
          superType =>
            typeDef.removeSuperTypes(superType)
            typeDef.addSuperTypes(new UnresolvedTypeDefinition(superType.getName))
        }
    }




    (containerRootAspect.getAllInstances(actualModel) ++ containerRootAspect.getAllInstances(modelToMerge)).foreach {
      instance =>
        instance.setTypeDefinition(new UnresolvedTypeDefinition(instance.getTypeDefinition.getName))
        //BREAK PORT TYPE REF
        if (instance.isInstanceOf[ComponentInstance]) {
          val componentInstance: ComponentInstance = instance.asInstanceOf[ComponentInstance]
          componentInstance.getProvided.foreach {
            pport => {
              pport.setPortTypeRef(new UnresolvedPortTypeRef(pport.getPortTypeRef.getName))
            }

          }
          componentInstance.getRequired.foreach {
            rport => {
              rport.setPortTypeRef(new UnresolvedPortTypeRef(rport.getPortTypeRef.getName))
            }
          }

        }
        if (instance.isInstanceOf[ContainerNode]) {
          val n = instance.asInstanceOf[ContainerNode]
          n.getHosts.foreach {
            h =>
              n.removeHosts(h)
              n.addHosts(new UnresolvedChildNode(h.getName))
          }
        }

        //BREAK DICTIONARY

        val instanceDictionary = instance.getDictionary()
        if (instanceDictionary != null) {
            instanceDictionary.getValues().foreach {
              dictionaryValue =>
                dictionaryValue.setAttribute(new UnresolvedDictionaryAttribute(dictionaryValue.getAttribute.getName))
                val targetNode = dictionaryValue.getTargetNode()
                if (targetNode != null){
                    dictionaryValue.setTargetNode(new UnresolvedNode(targetNode.getName(),targetNode.path()))
                }
            }
        }
    }
  }
}