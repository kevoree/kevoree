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

import org.kevoree.Operation
import org.kevoree.Parameter
import org.slf4j.LoggerFactory
import org.kevoree.merger.aspects.KevoreeAspects._
import scala.collection.JavaConversions._

case class OperationAspect(selfOperation: Operation) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def contractChanged(otherOperation: Operation): Boolean = {
    "" match {
      case _ if (otherOperation.getParameters.size != selfOperation.getParameters.size) => logger.debug("otherOperation.getParameters.size != selfOperation.getParameters.size"); true
      case _ => {
        var parameterChanged = otherOperation.getParameters.size != 0 && otherOperation.getParameters.forall(otherParam => {
          selfOperation.getParameters.find(selfParam => selfParam.getName == otherParam.getName) match {
            case Some(selfParam) => {
              !selfParam.getType.isModelEquals(otherParam.getType)
            }
            case None => logger.debug("Parameters are not found in previous operation {}", otherParam.getName); true
          }
        })

        if (parameterChanged) {
          val selfOpeInherit = selfOperation.getParameters.forall(p => p.getName.startsWith("arg"))
          val otherOpeInherit = otherOperation.getParameters.forall(p => p.getName.startsWith("arg"))
          if (otherOpeInherit && !selfOpeInherit) {
            val selfArr = selfOperation.getParameters.toArray
            val otherArr = otherOperation.getParameters.toArray
            var consistencyImpact = false
            for (i <- 0 until selfArr.length) {
              if (!selfArr(i).asInstanceOf[Parameter].getType.isModelEquals(otherArr(i).asInstanceOf[Parameter].getType)) {
                consistencyImpact = true
              }
            }
            if (!consistencyImpact) {
              parameterChanged = false
              logger.debug("Conflict resolved")
            } else {
              logger.debug("Conflict unresolved")
            }
          }
          if (!otherOpeInherit && selfOpeInherit) {
            val selfArr = selfOperation.getParameters.toArray
            val otherArr = otherOperation.getParameters.toArray
            var consistencyImpact = false
            for (i <- 0 until selfArr.length) {
              if (!selfArr(i).asInstanceOf[Parameter].getType.isModelEquals(otherArr(i).asInstanceOf[Parameter].getType)) {
                consistencyImpact = true
              }
            }
            if (!consistencyImpact) {
              parameterChanged = false
              for (i <- 0 until selfArr.length) {
                selfArr(i).asInstanceOf[Parameter].setName(otherArr(i).asInstanceOf[Parameter].getName)
              }
              logger.debug("Conflict resolved")
            } else {
              logger.debug("Conflict unresolved")
            }
          }


        }
        val returnType = !selfOperation.getReturnType.isModelEquals(otherOperation.getReturnType)
        /*  if (returnType){
            logger.debug("Return type changed {}=>{}",selfOperation.getName,Array(selfOperation.getReturnType,otherOperation.getReturnType))
          }
          if (parameterChanged){
            logger.debug("Parameters changed {}=>{}",selfOperation.getName,Array(otherOperation.getParameters.size,selfOperation.getParameters.size))
          } */
        parameterChanged || returnType
      }
    }
    //    true
  }

}