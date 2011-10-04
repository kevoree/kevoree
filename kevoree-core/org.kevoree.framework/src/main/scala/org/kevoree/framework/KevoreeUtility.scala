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

package org.kevoree.framework

import org.kevoree._

object KevoreeUtility {

  var root : ContainerRoot = _

  def getOraddDataType(datatype : TypedElement) : TypedElement = {
    root.getDataTypes.find({t=>t.getName.equals(datatype.getName)}).getOrElse{
      root.addDataTypes(datatype)
      datatype
    }
  }

  def getOraddPortType(portType : PortType) : PortType = {
    root.getTypeDefinitions.filter{st=> st.isInstanceOf[PortType]}.find({pt=>pt.getName == portType.getName}).getOrElse{
      root.addTypeDefinitions(portType)
      portType
    }.asInstanceOf[PortType]
  }

  def getRelatedBinding(component : ComponentInstance,root: ContainerRoot) : java.util.List[MBinding] = {
    val res = new java.util.ArrayList[MBinding]();
    root.getMBindings.foreach{b=>
      component.getProvided.find({p=> b.getPort == p}) match {
        case Some(e)=> res.add(b)
        case None =>
      }
      component.getRequired.find({p=> b.getPort == p}) match {
        case Some(e)=> res.add(b)
        case None =>
      }
    }
    res
  }


  def getRelatedBinding(inst : Instance) : java.util.List[MBinding] = {
    val res = new java.util.ArrayList[MBinding]()
    inst match {
      case component : ComponentInstance => res.addAll(getRelatedBinding(component,component.eContainer.asInstanceOf[KevoreeContainer].eContainer.asInstanceOf[ContainerRoot]))
      case channel : Channel => {
          channel.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach{b=>
            if(b.getHub == channel){
              res.add(b)
            }
          }
        }
      case _ =>
    }
    res
  }



}
