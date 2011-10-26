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
package org.kevoree.core.basechecker.cyclechecker

import org.jgrapht.EdgeFactory
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 26/10/11
 * Time: 09:10
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class KevoreeMBindingEdgeFactory (model: ContainerRoot) extends EdgeFactory[Instance, MBinding] {

  override def createEdge (sourceVertex: Instance, targetVertex: Instance): MBinding = {
    if (sourceVertex.isInstanceOf[Channel] && targetVertex.isInstanceOf[ComponentInstance]) {
      model.getHubs.find(hub => hub.getName == sourceVertex.asInstanceOf[Channel].getName) match {
        case None => throw new
            RuntimeException("Edge factory failed because there is no corresponding Channel on model")
        case Some(hub) => hub.getRelatedBindings
          .find(mBinding => mBinding.getPort.eContainer.asInstanceOf[ComponentInstance].getName ==
          targetVertex.getName) match {
          case None => throw new
              RuntimeException("Edge factory failed because there is no corresponding ComponentInstance on model")
          case Some(mBinding) => mBinding
        }
      }
    } else if (targetVertex.isInstanceOf[Channel] && sourceVertex.isInstanceOf[ComponentInstance]) {
      model.getHubs.find(hub => hub.getName == targetVertex.asInstanceOf[Channel].getName) match {
        case None => throw new
            RuntimeException("Edge factory failed because there is no corresponding Channel on model")
        case Some(hub) => hub.getRelatedBindings
          .find(mBinding => mBinding.getPort.eContainer.asInstanceOf[ComponentInstance].getName ==
          sourceVertex.getName) match {
          case None => throw new
              RuntimeException("Edge factory failed because there is no corresponding ComponentInstance on model")
          case Some(mBinding) => mBinding
        }
      }
    } else {
      throw new RuntimeException("Edge factory failed because both vertices are not channel")
    }
  }
}