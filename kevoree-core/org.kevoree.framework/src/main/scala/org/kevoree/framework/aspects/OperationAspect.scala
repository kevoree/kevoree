package org.kevoree.framework.aspects

import org.kevoree.Operation
import scala.collection.JavaConversions._
import KevoreeAspects._

case class OperationAspect(selfOperation: Operation) {

  def contractChanged(otherOperation: Operation): Boolean = {
    "" match {
      case _ if (otherOperation.getParameters.size != selfOperation.getParameters.size) => true
      case _ => {
        val parameterChanged = otherOperation.getParameters.forall(otherParam => {
          selfOperation.getParameters.find(selfParam => selfParam.getName == otherParam.getName) match {
            case Some(selfParam) =>  {
              !selfParam.getType.isModelEquals(otherParam.getType)
            }
            case None => true
          }
        })
        val returnType = !selfOperation.getReturnType.isModelEquals(otherOperation.getReturnType)
        parameterChanged || returnType
      }
    }
    true
  }

}