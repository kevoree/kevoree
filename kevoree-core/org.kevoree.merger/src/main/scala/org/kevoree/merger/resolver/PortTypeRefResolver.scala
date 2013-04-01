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
package org.kevoree.merger.resolver

import org.kevoree.framework.kaspects.ContainerRootAspect
import org.slf4j.LoggerFactory
import org.kevoree.{ComponentType, ComponentInstance, ContainerRoot}
import scala.collection.JavaConversions._


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/03/12
 * Time: 10:28
 */

trait PortTypeRefResolver {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val containerRootAspect = new ContainerRootAspect()

  def resolvePortTypeRef(model: ContainerRoot) {
    containerRootAspect.getAllInstances(model).foreach {
      instance =>
      //WE ASSUME THAT TYPE DEFINITION IS ALREADY RESOLVED
        if (instance.isInstanceOf[ComponentInstance]) {
          val componentInstance: ComponentInstance = instance.asInstanceOf[ComponentInstance]
          componentInstance.getProvided.foreach {
            pport =>
              val ct = instance.getTypeDefinition.asInstanceOf[ComponentType]
              ct.getProvided.foreach {
                pp =>
                  if (pp.getName == pport.getPortTypeRef.getName) {
                    pport.setPortTypeRef(pp)
                  }
              }
              ct.getRequired.foreach {
                rp =>
                  if (rp.getName == pport.getPortTypeRef.getName) {
                    pport.setPortTypeRef(rp)
                  }
              }
          }
          componentInstance.getRequired.foreach {
            rport =>
              val ct = instance.getTypeDefinition.asInstanceOf[ComponentType]
              ct.getProvided.foreach {
                pp =>
                  if (pp.getName == rport.getPortTypeRef.getName) {
                    rport.setPortTypeRef(pp)
                  }
              }
              ct.getRequired.foreach {
                rp =>
                  if (rp.getName == rport.getPortTypeRef.getName) {
                    rport.setPortTypeRef(rp)
                  }
              }
          }
        }

    }
  }

}
