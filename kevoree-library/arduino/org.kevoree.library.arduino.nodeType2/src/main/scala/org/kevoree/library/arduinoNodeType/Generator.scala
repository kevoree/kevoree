package org.kevoree.library.arduinoNodeType

import scala.collection.JavaConversions._
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import org.kevoree.Channel
import org.kevoree.ChannelType
import org.kevoree.ComponentInstance
import org.kevoree.ComponentType
import org.kevoree.ContainerNode
import org.kevoree.ContainerRoot
import org.kevoree.MessagePortType
import org.kevoree.NamedElement
import org.kevoree.Port
import org.kevoree.PortTypeRef
import org.kevoree.framework.ChannelTypeFragment
import org.kevoree.framework.KevoreePort
import org.kevoree.framework.message.FragmentBindMessage
import org.kevoree.framework.message.Message
import org.kevoree.framework.message.PortBindMessage
import org.kevoreeAdaptation.{AddInstance, AdaptationModel}
import scala.collection.immutable.HashMap
import org.kevoree.annotation.{Generate => KGenerate}
import org.osgi.framework.BundleContext


object Generator {

  def generate(adaptModel: AdaptationModel, nodeName: String,outputDir : String,bundleContext : BundleContext): Boolean = {

    val contextMap = HashMap[String, StringBuffer](
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
          //val loader = this.getClass.getClassLoader
          //val jclazz: Class[_] = loader.loadClass(instance.getTypeDefinition.getBean)

          //CREATE NEW INSTANCE
          var clazzFactory: Class[_] = null
          val activatorClassName = instance.getTypeDefinition.getFactoryBean.replaceAll("Factory", "Activator")
          if(bundleContext != null){
            clazzFactory = bundleContext.getBundle.loadClass(activatorClassName)
          }else {
            clazzFactory = this.getClass.getClassLoader.loadClass(activatorClassName)
          }
          val activatorInstance = clazzFactory.newInstance

          val reflectiveInstanceActor = clazzFactory.getMethod("callFactory").invoke(activatorInstance)
          val clazzActor = reflectiveInstanceActor.getClass

          val reflectiveInstance = clazzActor.getMethods.find(method => {
              method.getName == "getKevoreeComponentType"
            }) match {
            case Some(method) => {
                method.invoke(reflectiveInstanceActor)
              }
            case None => reflectiveInstanceActor
          }
          val clazz = reflectiveInstance.getClass


          //CREATE INSTANCE DICTIONARY
          val dictionary: java.util.HashMap[String, String] = new java.util.HashMap[String, String]
          if (instance.getTypeDefinition.getDictionaryType != null) {
            if (instance.getTypeDefinition.getDictionaryType.getDefaultValues != null) {
              instance.getTypeDefinition.getDictionaryType.getDefaultValues.foreach {
                dv =>
                dictionary.put(dv.getAttribute.getName, dv.getValue)
              }
            }
          }

          if (instance.getDictionary != null) {
            instance.getDictionary.getValues.foreach {
              v =>
              dictionary.put(v.getAttribute.getName, v.getValue)
            }
          }
          //REFLEXIVE SET DICTIONARY
          clazz.getMethods.find(method => method.getName == "setDictionary") match {
            case Some(method)=> method.invoke(reflectiveInstance, dictionary)
            case None => println("Dictionary not set !")
          }

          //REFLEXIVE SET NAME
          clazz.getMethods.find(method => method.getName == "setName") match {
            case Some(method)=> method.invoke(reflectiveInstance, instance.asInstanceOf[NamedElement].getName)
            case None => println("Dictionary not set !")
          }
          //REFLEXIVE SET NODENAME
          clazz.getMethods.find(method => method.getName == "setNodeName") match {
            case Some(method)=> method.invoke(reflectiveInstance, nodeName)
            case None => println("Dictionary not set !")
          }


          clazz.getMethods.foreach {
            method =>
            //println("method="+method)
            method.getAnnotations.foreach {
              annotation =>
              //println("annotation="+annotation)
              if (annotation.annotationType.toString.contains("org.kevoree.annotation.Generate")) {
                val generateAnnotation = annotation.asInstanceOf[KGenerate]
                contextMap.get(generateAnnotation.value.toString) match {
                  case Some(e) => {
                      method.invoke(reflectiveInstance, e)
                    }
                  case None => println("context not found " + generateAnnotation.value.toString)
                }
              }
            }
          }

          //SPECIFIQUE PROCESS
          instance match {
            case c: Channel => {
                val typeDef = c.getTypeDefinition.asInstanceOf[ChannelType]
                //INIT BINDINGS
                val channelInstance = reflectiveInstance.asInstanceOf[ChannelTypeFragment]
                channelInstance.start
                
                var node = c.eContainer.asInstanceOf[ContainerRoot].getNodes.find(node => node.getName ==nodeName).get
                c.eContainer.asInstanceOf[ContainerRoot].getMBindings.foreach{binding=>
                  if(binding.getHub == c){
                    if(binding.getPort.eContainer.eContainer == node){
                      if(binding.getPort.eContainer.asInstanceOf[ComponentInstance].getProvided.contains(binding.getPort)){
                        
                        println("provided port "+binding.getPort.getPortTypeRef.getName)
                        
                        //ADD COLLOCATED MBINDING
                        val tempPort = new KevoreePort(){
                          def getComponentName = binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName
                          def getName = binding.getPort.getPortTypeRef.getName
                          def internal_process(msg:Any) = {}
                        }
                        val bindmsg = new PortBindMessage
                        bindmsg.setComponentName(binding.getPort.eContainer.asInstanceOf[ComponentInstance].getName)
                        bindmsg.setNodeName(nodeName)
                        bindmsg.setPortName(binding.getPort.getPortTypeRef.getName)
                        bindmsg.setProxy(tempPort)
                  
                        channelInstance !? bindmsg
                      }
                    } else {
                      
                      
                      
                      println("Remote detected")
                      
                      //ADD REMOTE MBINDING
                      val fbindmsg = new FragmentBindMessage
                      fbindmsg.setChannelName(binding.getHub.getName)
                      fbindmsg.setFragmentNodeName(binding.getPort.eContainer.eContainer.asInstanceOf[ContainerNode].getName)
                      channelInstance !? fbindmsg
                    } 
                  }
                }

                clazz.getMethods.find(method => method.getName == "dispatch") match {
                  case Some(method) => contextMap.get("body").get.append(generateChannelDispatchMethod(c, method, reflectiveInstance))
                  case None => println("method dispatch not found")
                }
            
                channelInstance.stop
                
              }
            case ci: ComponentInstance => {
                val typeDef = ci.getTypeDefinition.asInstanceOf[ComponentType]
                ci.getProvided.foreach {
                  provided =>
                  provided.getPortTypeRef.getRef match {
                    case mpt: MessagePortType => {
                        provided.getPortTypeRef.getMappings.find(mapping => mapping.getServiceMethodName == "process") match {
                          case Some(mapping) => {
                              clazz.getMethods.find(method => method.getName == mapping.getBeanMethodName) match {
                                case Some(method) => {
                                    contextMap.get("body").get.append(generateMessageProvidedPort(ci, provided.getPortTypeRef, mpt, reflectiveInstance, method))
                                  }
                                case _ => println("method not found")
                              }
                            }
                          case None => println("Process not found")
                        }
                      }
                    case _@pt => println("Not supported " + pt)
                  }
                }
                ci.getRequired.foreach {
                  required =>
                  contextMap.get("body").get.append(generateMessageRequiredPort(ci, required.getPortTypeRef, required))
                }

                // clazz.getMethods.find(method=> meh)

              }
            case _ => println("uncatch instance")
          }

        })
    } catch {
      case _@e => e.printStackTrace
    }


    val finalResult = contextMap.get("header").get.toString + generateSetupMethod(contextMap.get("setup").get) + generateLoopMethod(contextMap.get("loop").get) + contextMap.get("body").get.toString
    // println(finalResult)

    val writer = new PrintWriter( new BufferedWriter(new FileWriter(outputDir+"/arduinoGenerated"+nodeName+".pde",false)));
    writer.println(finalResult.toString)
    writer.println("")
    writer.close()
    true
  }

  private def generateSetupMethod(sb: StringBuffer): String = {
    var result = new StringBuffer
    result.append("void setup(){\n")
    result.append(sb.toString)
    result.append("}\n")
    result.toString
  }

  private def generateLoopMethod(sb: StringBuffer): String = {
    var result = new StringBuffer
    result.append("void loop(){\n")
    result.append(sb.toString)
    result.append("}\n")
    result.toString
  }

  private def generateChannelDispatchMethod(channel: Channel, method: java.lang.reflect.Method, instance: Any): String = {
    var localContext = new StringBuffer
    localContext.append("void ")
    localContext.append(ArduinoMethodHelper.generateMethodNameChannelDispatch(channel.getName))
    localContext.append("(String param){\n")

    var message = new Message
    message.setContent(localContext)
    method.invoke(instance, message)

    localContext.append("}\n")
    localContext.toString
  }

  private def generateMessageProvidedPort(ci: ComponentInstance, ptr: PortTypeRef, mpt: MessagePortType, reflectiveInstance: Any, method: java.lang.reflect.Method): String = {
    var localContext = new StringBuffer
    localContext.append("void ")
    localContext.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(ci.getName, ptr.getName, PortUsage.provided))
    //localContext.append(" component_"+ci.getName+"_providedPort_"+ptr.getName+" ")
    localContext.append(" (String param){")
    localContext.append("\n")
    method.invoke(reflectiveInstance, localContext)
    localContext.append("\n}\n")
    localContext.toString
  }

  private def generateMessageRequiredPort(ci: ComponentInstance, ptr: PortTypeRef, port: Port): String = {
    var localContext = new StringBuffer
    localContext.append("void ")
    //localContext.append(" component_"+ci.getName+"_requiredPort_"+ptr.getName+" ")
    localContext.append(ArduinoMethodHelper.generateMethodNameFromComponentPort(ci.getName, ptr.getName, PortUsage.required))
    localContext.append(" (String param){")

    ci.eContainer.eContainer.asInstanceOf[ContainerRoot].getMBindings.find(mb => (mb.getPort == port)) match {
      case Some(binding) => {
          localContext.append("\n")
          localContext.append(ArduinoMethodHelper.generateMethodNameChannelDispatch(binding.getHub.getName))
          localContext.append("(param);")
          localContext.append("\n")
        }
      case None => println("no binding found")
    }

    localContext.append("\n}\n")
    localContext.toString
  }


}