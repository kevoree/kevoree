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

package org.kevoree.kompare.sub

import org.slf4j.LoggerFactory
import org.kevoree.{ContainerRoot, AdaptationPrimitiveType}

trait AbstractKompare {

  var logger = LoggerFactory.getLogger(this.getClass);

  def getAdaptationPrimitive(typeName: String, model: ContainerRoot): AdaptationPrimitiveType = {
    model.getAdaptationPrimitiveTypes.find(p => p.getName == typeName) match {
      case Some(p) => p
      case None => {
        logger.error("Error while searching for adaptation primitive type for name = " + typeName)
        null
      }
    }
  }

}
