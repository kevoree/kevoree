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
package org.kevoree.core.basechecker.kevoreeVersionChecker

import org.kevoree.ContainerNode
import org.kevoree.framework.kaspects.TypeDefinitionAspect
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/03/13
 * Time: 14:17
 *
 * @author Erwan Daubert
 * @version 1.0
 */
 trait KevoreeNodeVersion {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val typeDefinitionAspect = new TypeDefinitionAspect()

  def getKevoreeVersion(node : ContainerNode): String = {
    try {

      val rDU = typeDefinitionAspect.foundRelevantDeployUnit(node.getTypeDefinition, node)
      if (rDU != null) {
        rDU.getRequiredLibs.find(du => du.getGroupName == "org.kevoree" && du.getUnitName == "org.kevoree.api") match {
          case None => {
            logger.error("org.kevoree.api deploy unit not found for " + node.getName)
            ""
          } // must never appear
          case Some(du) => du.getVersion
        }

      } else {
        logger.error("Relevant deploy unit  not found for " + node.getName)
        ""
      }
    } catch {
      case _@e => logger.debug("Unable to find kevoree version", e); ""
    }
  }

}
