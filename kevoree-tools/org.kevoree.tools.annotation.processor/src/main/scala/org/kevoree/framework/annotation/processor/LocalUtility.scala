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

package org.kevoree.framework.annotation.processor

import org.kevoree.{ContainerRoot, PortType, TypedElement}
import scala.collection.JavaConversions._
import org.kevoree.impl.DefaultKevoreeFactory

object LocalUtility {
  var root: ContainerRoot = _

  val kevoreeFactory = new DefaultKevoreeFactory

  def generateLibURI(options: java.util.Map[String, String]) = {
    options.get("kevoree.lib.target")
  }

  def getOraddDataType(datatype: TypedElement): TypedElement = {
    root.getDataTypes.find({
      t =>
        if (t.getName.equals(datatype.getName)) {
          t.getGenericTypes.forall(genericType => datatype.getGenericTypes.contains(genericType))
        } else {
          false
        }
    }).getOrElse {
      root.addDataTypes(datatype)
      datatype
    }
  }

  def getOraddPortType(portType: PortType): PortType = {
    root.getTypeDefinitions.filter {
      st => st.isInstanceOf[PortType]
    }.find({
      pt => pt.asInstanceOf[PortType].getName.equals(portType.getName)
    }).getOrElse {
      root.addTypeDefinitions(portType)
      portType
    }.asInstanceOf[PortType]
  }
}
