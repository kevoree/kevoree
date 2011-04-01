package org.kevoree.library.arduinoNodeType

import scala.collection.JavaConversions._
import org.kevoree.ComponentInstance
import org.kevoree.ComponentType
import org.kevoree.MessagePortType
import org.kevoree.PortTypeRef
import org.kevoreeAdaptation.{AddInstance, AdaptationModel}
import scala.collection.immutable.HashMap
import org.kevoree.annotation.{Generate => KGenerate }


object Generator {

  def generate(adaptModel: AdaptationModel, nodeName: String): Boolean = {
    
    var contextMap = HashMap[String,StringBuffer](
        ("loop" -> new StringBuffer),
        ("header" -> new StringBuffer),
        ("setup" -> new StringBuffer),
        ("body" -> new StringBuffer)
      )
    
    try {
      val instAdaptation = adaptModel.getAdaptations.filter(adt => adt.isInstanceOf[AddInstance])
      instAdaptation.foreach(instanceAdaptation => {
          //CREATE NEW INSTANCE
          val addInstance = instanceAdaptation.asInstanceOf[AddInstance]
          val instance = addInstance.getRef
          //TODO CHANGE
          val loader = this.getClass.getClassLoader
          val clazz: Class[_] = loader.loadClass(instance.getTypeDefinition.getBean)
          var reflectiveInstance = clazz.newInstance
          
          clazz.getMethods.foreach{method=>
            method.getAnnotations.foreach{annotation=>
              if(annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")){
                var generateAnnotation = annotation.asInstanceOf[KGenerate]
                contextMap.get(generateAnnotation.value.toString) match {
                  case Some(e)=> {
                      method.invoke(reflectiveInstance, e)
                  }
                  case None => println("context not found "+generateAnnotation.value.toString)
                }
              }
            }
          }

          //SPECIFIQUE PROCESS
          instance match {
            case ci : ComponentInstance => {
                var typeDef = ci.getTypeDefinition.asInstanceOf[ComponentType]
                ci.getProvided.foreach{provided=>
                  provided.getPortTypeRef.getRef match {
                    case mpt : MessagePortType => {
                        provided.getPortTypeRef.getMappings.find(mapping => mapping.getServiceMethodName == "process") match {
                          case Some(mapping)=> {
                              clazz.getMethods.find(method=> method.getName == mapping.getBeanMethodName) match {
                                case Some(method)=> {
                                    contextMap.get("body").get.append(generateMessageProvidedPort(ci,provided.getPortTypeRef,mpt,reflectiveInstance, method))
                                }
                                case _ => println("method not found")
                              } 
                          }
                          case None => println("Process not found")
                        }
                    }
                    case _ @ pt => println("Not supported "+pt)
                  }
                }
                ci.getRequired.foreach{ required =>
                  contextMap.get("body").get.append(generateMessageRequiredPort(ci,required.getPortTypeRef))                
                }
                
               // clazz.getMethods.find(method=> meh)
                
              }
            case _ =>
          }
       
        })
    } catch {
      case _ @ e =>
    }
      
    
    println("result=")
    
    var finalResult = contextMap.get("header").get.toString + generateSetupMethod(contextMap.get("setup").get) + generateLoopMethod(contextMap.get("loop").get) + contextMap.get("body").get.toString
    
    println(finalResult)
                             
    true
  }
  
  private def generateSetupMethod(sb : StringBuffer) : String = {
    var result = new StringBuffer
    result.append("void setup(){\n")
    result.append(sb.toString)
    result.append("}\n")
    result.toString
  }
  private def generateLoopMethod(sb : StringBuffer) : String = {
    var result = new StringBuffer
    result.append("void loop(){\n")
    result.append(sb.toString)
    result.append("}\n")
    result.toString
  }
  
  private def generateMessageProvidedPort(ci:ComponentInstance,ptr:PortTypeRef,mpt:MessagePortType,reflectiveInstance:Any,method : java.lang.reflect.Method) : String = {
    var localContext = new StringBuffer
    localContext.append("void")
    localContext.append(" component_"+ci.getName+"_providedPort_"+ptr.getName+" ")
    localContext.append(" (String param){")
    localContext.append("\n")
    method.invoke(reflectiveInstance, localContext)
    localContext.append("\n}\n")
    localContext.toString
  }
  private def generateMessageRequiredPort(ci:ComponentInstance,ptr:PortTypeRef) : String = {
    var localContext = new StringBuffer
    localContext.append("void")
    localContext.append(" component_"+ci.getName+"_requiredPort_"+ptr.getName+" ")
    localContext.append(" (String param){")
    //TODO CALL BINDING
    
    
    
    //TODO CALL BINDING
    localContext.append("\n}\n")
    localContext.toString
  }


}