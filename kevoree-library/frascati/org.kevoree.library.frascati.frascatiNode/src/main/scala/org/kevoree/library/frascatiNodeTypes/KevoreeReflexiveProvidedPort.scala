package org.kevoree.library.frascatiNodeTypes

import org.kevoree.framework.port.KevoreeProvidedPort
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 12/03/12
 * Time: 13:55
 */

class KevoreeReflexiveProvidedPort(portName: String, instanceName: String, targetRef: AnyRef) extends KevoreeProvidedPort {
  def internal_process(msg: Any) {
    println("Wrapper :-) "+msg+"-"+targetRef)
    try {
      msg match {
        case opcall: org.kevoree.framework.MethodCallMessage => {
          val clazz = targetRef.getClass
          clazz.getMethods.find(m => m.getName == opcall.getMethodName) match {
            case Some(rmethod)=> {

              if(opcall.getParams.values().toArray.length == 0){
                reply(rmethod.invoke(targetRef))
              } else {
                //TODO BARAIS :-)-

                println(opcall.getParams.values().toArray)
                println(opcall.getParams.values().toArray.length)
                println(opcall.getParams.values().mkString(","))


                reply(rmethod.invoke(targetRef,opcall.getParams.values().toArray))
              }
            }
            case None => reply(null)
          }
        }
        case _@o => println("uncatch message , method not found in service declaration : " + o); reply(null)
      }
    } catch {
      case _@e => e.printStackTrace()
    }
  }

  def getName: String = portName

  def getComponentName: String = instanceName
}
