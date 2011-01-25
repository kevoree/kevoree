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

package org.kevoree.framework.annotation.processor

import com.sun.mirror.apt.AnnotationProcessorEnvironment
import org.kevoree.ContainerRoot
import org.kevoree.PortType
import org.kevoree.PortType
import org.kevoree.TypedElement
import scala.collection.JavaConversions._

object LocalUtility {
  var root : ContainerRoot = _

  def generateLibURI(env:AnnotationProcessorEnvironment) = {
    "file://" + env.getOptions.find({op => op._1.contains("kevoree.lib.target")}).getOrElse{("key=","")}._1.split('=').toList.get(1)
  }

  def getOraddDataType(datatype : TypedElement) : TypedElement = {
    root.getDataTypes.find({t=>t.getName.equals(datatype.getName)}).getOrElse{
      root.getDataTypes.add(datatype)
      datatype
    }
  }

  def getOraddPortType(portType : PortType) : PortType = {
    root.getTypeDefinitions.filter{st=> st.isInstanceOf[PortType]}.find({pt=>pt.getName == portType.getName}).getOrElse{
      root.getTypeDefinitions.add(portType)
      portType
    }.asInstanceOf[PortType]
  }
}
