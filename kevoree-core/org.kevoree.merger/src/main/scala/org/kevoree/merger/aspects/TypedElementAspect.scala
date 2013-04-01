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
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import org.kevoree.merger.aspects.KevoreeAspects._

case class TypedElementAspect(e : TypedElement) {

  private val logger = LoggerFactory.getLogger(this.getClass)
  def isModelEquals(remote : TypedElement) : Boolean = {
    val nameEquality = e.getName == remote.getName
    val genericEquality = e.getGenericTypes.forall(p=> remote.getGenericTypes.exists(remoteP => remoteP.isModelEquals(p)  )  )
    val sizeEquality = e.getGenericTypes.size == remote.getGenericTypes.size
    if(!(nameEquality && genericEquality && sizeEquality)){
      logger.debug("{}", Array(e.getName, nameEquality, genericEquality, sizeEquality))
    }
    nameEquality && genericEquality && sizeEquality
  }
}
