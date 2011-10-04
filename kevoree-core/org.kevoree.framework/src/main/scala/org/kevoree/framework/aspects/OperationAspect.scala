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
package org.kevoree.framework.aspects

import org.kevoree.Operation
 import KevoreeAspects._

case class OperationAspect(selfOperation: Operation) {

  def contractChanged(otherOperation: Operation): Boolean = {
    "" match {
      case _ if (otherOperation.getParameters.size != selfOperation.getParameters.size) => true
      case _ => {
        val parameterChanged = otherOperation.getParameters.forall(otherParam => {
          selfOperation.getParameters.find(selfParam => selfParam.getName == otherParam.getName) match {
            case Some(selfParam) =>  {
              !selfParam.getType.get.isModelEquals(otherParam.getType.get)
            }
            case None => true
          }
        })
        val returnType = !selfOperation.getReturnType.get.isModelEquals(otherOperation.getReturnType.get)
        parameterChanged || returnType
      }
    }
    true
  }

}