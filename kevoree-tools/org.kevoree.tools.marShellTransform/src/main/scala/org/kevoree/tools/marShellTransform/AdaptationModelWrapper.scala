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
package org.kevoree.tools.marShellTransform

import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.ast._
import org.kevoreeAdaptation._
import org.kevoree.{Group, Channel, ComponentInstance, ContainerNode}

object AdaptationModelWrapper {

  def generateScriptFromAdaptModel(model: AdaptationModel): Script = {
    val statments = new java.util.ArrayList[Statment]()
    model.getAdaptations.foreach {
      adapt =>
        adapt match {
          case statement: AddBinding => statments.add(AddBindingStatment(ComponentInstanceID(statement.getRef.getPort.eContainer.asInstanceOf[ComponentInstance].getName, Some(statement.getRef.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)), statement.getRef.getPort.getPortTypeRef.getName, statement.getRef.getHub.getName))
          case statement: AddInstance => {

            val props = new java.util.Properties
            if (statement.getRef.getDictionary != null) {
              statement.getRef.getDictionary.getValues.foreach {
                value =>
                  props.put(value.getAttribute.getName, value.getValue)
              }
            }

            statement.getRef match {
              case c: Group => statments.add(AddGroupStatment(c.getName, c.getTypeDefinition.getName, props))
              case c: Channel => statments.add(AddChannelInstanceStatment(c.getName, c.getTypeDefinition.getName, props))
              case c: ComponentInstance => {
                val cid = ComponentInstanceID(c.getName, Some(c.eContainer.asInstanceOf[ContainerNode].getName))
                statments.add(AddComponentInstanceStatment(cid, c.getTypeDefinition.getName, props))
              }
              //TODO
              case _@uncatchInstance => println("warning : uncatched=" + uncatchInstance)
            }
          }
          case statement: RemoveBinding => statments.add(RemoveBindingStatment(ComponentInstanceID(statement.getRef.getPort.eContainer.asInstanceOf[ComponentInstance].getName, Some(statement.getRef.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)), statement.getRef.getPort.getPortTypeRef.getName, statement.getRef.getHub.getName))
          case statement: RemoveInstance => {
            statement.getRef match {
              case c: Group => statments.add(RemoveGroupStatment(c.getName))
              case c: Channel => statments.add(RemoveChannelInstanceStatment(c.getName))
              case c: ComponentInstance => {
                val cid = ComponentInstanceID(c.getName, Some(c.eContainer.eContainer.asInstanceOf[ContainerNode].getName))
                statments.add(RemoveComponentInstanceStatment(cid))
              }

              case _@uncatchInstance => println("warning : uncatched=" + uncatchInstance)
            }
          }
          case _@unCatched => println("warning : uncatched=" + unCatched)
        }

    }
    Script(List(TransactionalBloc(statments.toList)))
  }

}
