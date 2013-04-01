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
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.core.basechecker.kevoreeVersionChecker

import org.kevoree.api.service.core.checker.{CheckerViolation, CheckerService}
import org.kevoree.impl.DefaultKevoreeFactory
import org.kevoree.ContainerRoot
import collection.JavaConversions._
import java.util

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/04/12
 * Time: 16:52
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeNodeVersionChecker (nodeName: String) extends CheckerService with KevoreeNodeVersion {
  def check (model: ContainerRoot): java.util.List[CheckerViolation] = {
    val violations: java.util.List[CheckerViolation] = new util.ArrayList[CheckerViolation]()
    model.getNodes.find(node => node.getName == nodeName) match {
      case None =>
      case Some(node) => {
        val factory = new DefaultKevoreeFactory()
        val nodeKevoreeVersion = getKevoreeVersion(node)
        if (nodeKevoreeVersion != factory.getVersion) {
          val concreteViolation: CheckerViolation = new CheckerViolation()
          concreteViolation
            .setMessage("Node type " + nodeName + " needs different version of Kevoree that the one provided (requiredVersion=" + nodeKevoreeVersion +
            ",providedVersion=" + factory.getVersion)
          concreteViolation.setTargetObjects(List(node.asInstanceOf[AnyRef]))
          violations.add(concreteViolation)
        }
      }
    }
    violations
  }



}
