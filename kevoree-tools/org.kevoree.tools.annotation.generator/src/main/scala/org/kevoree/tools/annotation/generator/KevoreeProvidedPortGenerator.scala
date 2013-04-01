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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.tools.annotation.generator

import scala.collection.JavaConversions._


import org.kevoree.framework.{KevoreeGeneratorHelper, Constants}
import javax.tools.StandardLocation
import org.kevoree.{TypedElement, ContainerRoot, MessagePortType, PortTypeRef, ComponentType => KevoreeComponentType, ServicePortType}
import org.kevoree.annotation.ThreadStrategy

object KevoreeProvidedPortGenerator {

  def generate(root: ContainerRoot, filer: javax.annotation.processing.Filer, ct: KevoreeComponentType, ref: PortTypeRef, targetNodeType: String) {
    val portPackage = new KevoreeGeneratorHelper().getTypeDefinitionGeneratedPackage(ct, targetNodeType)
    // var portPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
    val portName = ct.getName + "PORT" + ref.getName
    val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(portPackage.replace(".", "/") + "/" + portName + ".kt"))
    val writer = wrapper.openWriter()
    writer.append("package " + portPackage + "\n")
    writer.append("import org.kevoree.framework.port.*\n")
    writer.append("import " + new KevoreeGeneratorHelper().getTypeDefinitionBasePackage(ct) + ".*\n")
    var baseName = ref.getRef.getName
    if (ref.getRef.isInstanceOf[MessagePortType]) {
      baseName = "org.kevoree.framework.MessagePort"
    }

    ThreadingMapping.getMappings.get(Tuple2(ct.getName, ref.getName)) match {
      case ThreadStrategy.THREAD_QUEUE => {
        writer.append("class " + portName + "(val component : " + ct.getName + ") : " + baseName + " , KevoreeProvidedThreadPort {\n")
      }
      case ThreadStrategy.SHARED_THREAD => {
        writer.append("class " + portName + "(val component : " + ct.getName + ") : " + baseName + " , KevoreeProvidedExecutorPort {\n")
        writer.append("override var pool: PausablePortThreadPoolExecutor? = null\n")
      }
      case ThreadStrategy.NONE => {
        writer.append("class " + portName + "(val component : " + ct.getName + ") : " + baseName + " , KevoreeProvidedNonePort {\n")
      }
      case _ => {
        writer.append("class " + portName + "(val component : " + ct.getName + ") : " + baseName + " , KevoreeProvidedExecutorPort {\n")
        writer.append("override var pool: PausablePortThreadPoolExecutor? = null\n")
      }
    }

    writer.append("override var isPaused : jet.Boolean = true\n")

    writer.append("override fun getName() : String { return \"" + ref.getName + "\"}\n")
    writer.append("override fun getComponentName() : String? { return component.getName() }\n")

    ref.getRef match {
      case mPT: MessagePortType => {
        /* GENERATE METHOD MAPPING */
        writer.append("override fun process(p0 : Any?) {this.send(p0)}\n")
        ref.getMappings.find(map => {
          map.getServiceMethodName.equals(Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD)
        }) match {
          case Some(mapping) => {
            /* GENERATE LOOP */
            writer.append("override fun internal_process(o : Any?) : Any?{\n")
            writer.append("when(o){")

            /* CALL MAPPED METHOD */
            writer.append("else -> { try{component.")

            writer.append(mapping.getBeanMethodName + "(")
            if (mapping.getParamTypes != null && mapping.getParamTypes != "" && mapping.getParamTypes.split(",").size > 1) {

              val elemsT = mapping.getParamTypes.split(",")
              elemsT.foreach {
                t =>
                  t match {
                    case "java.lang.String" => writer.append("getName()")
                    case "java.lang.Object" => writer.append("o")
                    case _ => writer.append("null")
                  }
                  if (elemsT.last != t) {
                    writer.append(",")
                  }
              }
            } else {
              writer.append("o")
            }
            writer.append(");return null}catch(e:Exception){e.printStackTrace();println(\"Uncatched exception while processing Kevoree message\");return null}\n")
            writer.append("}}}\n")
          }
          case None => {
            error("KevoreeProvidedPortGenerator::No mapping found for method '" + Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD + "' of MessagePort '" + ref.getName + "' in component '" + ct.getName + "'")
            error("No mapping found for method '" + Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD + "' of MessagePort '" + ref.getName + "' in component '" + ct.getName + "'")
          }
        }
      }

      case sPT: ServicePortType => {
        /* CREATE INTERFACE ACTOR MOK */
        sPT.getOperations.foreach {
          op =>
          /* GENERATE METHOD SIGNATURE */
            writer.append("override fun " + op.getName + "(")
            var i = 0
            op.getParameters.sortWith((p1, p2) => p2.getOrder > p1.getOrder).foreach {
              param =>
                if (i != 0) {
                  writer.append(",")
                }
                writer.append(GeneratorHelper.protectReservedWord(param.getName) + ":" + GeneratorHelper.protectedType(Printer.print(param.getType, '<', '>'))+"?")
                i = i + 1
            }
            /* GENERATES RETURN TYPE in rt */
            var rt = op.getReturnType.getName

            if (op.getReturnType.getGenericTypes.size > 0) {
              rt += op.getReturnType.getGenericTypes.collect {
                case s: TypedElement => s.getName
              }.mkString("<", ",", ">")
            }
            writer.append(") : " + GeneratorHelper.protectedType(rt) + " {\n")


            /* Generate method corpus */
            /* CREATE MSG OP CALL */
            writer.append("val msgcall = org.kevoree.framework.MethodCallMessage()\n")
            writer.append("msgcall.setMethodName(\"" + op.getName + "\")\n")
            op.getParameters.sortWith((p1, p2) => p2.getOrder > p1.getOrder).foreach {
              param =>
                writer.append("msgcall.getParams()!!.put(\"" + param.getName + "\"," + GeneratorHelper.protectReservedWord(param.getName) + " as Any)\n")
            }
            writer.append("return this.sendWait(msgcall) as " + GeneratorHelper.protectedType(rt))
            writer.append("}\n")
        }
        /* CREATE ACTOR LOOP */
        writer.append("override fun internal_process(o : Any?):Any?{ when(o) {\n")
        writer.append("is org.kevoree.framework.MethodCallMessage -> { when((o as org.kevoree.framework.MethodCallMessage).getMethodName()) {\n")
        sPT.getOperations.foreach {
          op =>
          /* FOUND METHOD MAPPING */
            ref.getMappings.find(map => {
              map.getServiceMethodName.equals(op.getName)
            }) match {
              case Some(mapping) => {
                writer.append("\"" + op.getName + "\"-> { try { return component." + mapping.getBeanMethodName + "(")
                var i = 0
                op.getParameters.sortWith((p1, p2) => p2.getOrder > p1.getOrder).foreach {
                  param =>
                    if (i != 0) {
                      writer.append(",")
                    }
                    writer.append("if((o as org.kevoree.framework.MethodCallMessage).getParams()!!.containsKey(\"" + param.getName + "\")){")
                    writer.append("(o as org.kevoree.framework.MethodCallMessage).getParams()!!.get(\"" + param.getName + "\") as " + GeneratorHelper.protectedType(Printer.print(param.getType, '<', '>')) + "")
                    writer.append("}else{")
                    writer.append("(o as org.kevoree.framework.MethodCallMessage).getParams()!!.get(\"arg" + i + "\") as " + GeneratorHelper.protectedType(Printer.print(param.getType, '<', '>')) + "")
                    writer.append("}")
                    i = i + 1
                }
                writer.append(")} catch(e:Exception) {e.printStackTrace();println(\"Uncatched exception while processing Kevoree message\");return null }}\n")

              }
              case None => {
                sys.error("No mapping found for method '" + op.getName + "' of ServicePort '" + ref.getName + "' in component '" + ct.getName + "'")
                //println("No mapping found for method '"+op.getName+"' of ServicePort '" + ref.getName + "' in component '" + ct.getName + "'")

              }
            }
        }
        writer.append("else -> { println(\"uncatch message , method not found in service declaration : \");return null}\n")
        writer.append("}")
        writer.append("}")
        writer.append("else -> { println(\"Bad MSG\"+o);return null }")
        writer.append("}}\n")
      }

    }

    writer.append("}\n")
    writer.close()
  }

}
