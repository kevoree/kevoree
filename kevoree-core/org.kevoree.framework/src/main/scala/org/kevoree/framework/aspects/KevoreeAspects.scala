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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.framework.aspects

import org.kevoree._
import scala.collection.JavaConversions._
import KevoreeAspects._

object KevoreeAspects{
  implicit def mbindingAspect(c : org.kevoree.MBinding) = MBindingAspect(c)
  implicit def instanceAspect(c : org.kevoree.Instance) = InstanceAspect(c)
  implicit def componentInstanceAspect(c : ComponentInstance) = ComponentInstanceAspect(c)
  implicit def typeAspect(ct : TypeDefinition) = TypeDefinitionAspect(ct)
  implicit def containerNodeAspect(cn : ContainerNode) = ContainerNodeAspect(cn)
  implicit def portAspect(p : Port ) = PortAspect(p)
 // implicit def bindingAspect(b : Binding ) = BindingAspect(b)
  implicit def typedElementAspect(b : TypedElement ) = TypedElementAspect(b)
  implicit def channelAspect(c:Channel) = ChannelAspect(c)
  implicit def dictionaryAspect(d:Dictionary) = DictionaryAspect(d)

  implicit def deployUnitAspect(d:DeployUnit) = DeployUnitAspect(d)


}






