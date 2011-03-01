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

package org.kevoree.adaptation.deploy.osgi.command

import org.kevoree.DeployUnit
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.slf4j.LoggerFactory

case class RemoveThirdPartyCommand(ct : DeployUnit, ctx : KevoreeDeployManager) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass);

  def execute() : Boolean= {
    logger.warn("Not implemented")
    true
  }

  def undo() = {
    logger.warn("Not implemented")
  }


}
