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

package org.kevoree.merger.aspects

import org.kevoree._
import scala.collection.JavaConversions._

case class PortAspect(p: Port) {

  private val portAspect = new org.kevoree.framework.kaspects.PortAspect()

  def removeAndUnbind() = {
    //REMOVE ALL BINDING BINDED TO
    val root = p.eContainer.eContainer.eContainer.asInstanceOf[ContainerRoot]
    val mbindings = root.getMBindings.filter(b => b.getPort == p) ++ List()
    mbindings.foreach {
      mb => root.removeMBindings(mb)
    }

    //REMOVE PORT
    if (portAspect.isProvidedPort(p)) {
      if (p.eContainer.asInstanceOf[ComponentInstance].getProvided.contains(p)) {
        p.eContainer.asInstanceOf[ComponentInstance].removeProvided(p)
      }
    } else {
      if (portAspect.isRequiredPort(p)) {
        if (p.eContainer.asInstanceOf[ComponentInstance].getRequired.contains(p)) {
          p.eContainer.asInstanceOf[ComponentInstance].removeRequired(p)
        }
      }
    }
  }
}

