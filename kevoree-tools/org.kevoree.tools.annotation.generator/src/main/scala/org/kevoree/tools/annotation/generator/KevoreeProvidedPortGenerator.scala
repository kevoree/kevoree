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

package org.kevoree.tools.annotation.generator

import com.sun.mirror.apt.Filer
import java.io.File
import org.kevoree.ContainerRoot
import org.kevoree.MessagePortType
import org.kevoree.PortTypeRef
import org.kevoree.{ComponentType => KevoreeComponentType }
import org.kevoree.ServicePortType
import org.kevoree.framework.Constants
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._

object KevoreeProvidedPortGenerator {

  def generate(root:ContainerRoot,filer:Filer,ct: KevoreeComponentType,ref:PortTypeRef){
    var portPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
    var portName = ct.getName()+"PORT"+ref.getName();

    var wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(portPackage.replace(".", "/")+"/"+portName+".scala"), "UTF-8");

    wrapper.append("package "+portPackage+"\n");
    wrapper.append("import org.kevoree.framework.port._\n");
    wrapper.append("import scala.{Unit=>void}\n")
    wrapper.append("class "+portName+"(component : "+ct.getName+") extends "+ref.getRef().getName()+" with KevoreeProvidedPort {\n");

    wrapper.append("def getName : String = \""+ref.getName+"\"\n")

    ref.getRef match {
      case mPT : MessagePortType => {
          /* GENERATE METHOD MAPPING */
          wrapper.append("def process(o : Object) = {this ! o}\n")

          ref.getMappings.find(map=>{map.getServiceMethodName.equals(Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD)}) match {
            case Some(mapping)=>{
                /* GENERATE LOOP */
                wrapper.append("override def internal_process(msg : Any)=msg match {\n")
                /* CALL MAPPED METHOD */
                wrapper.append("case _ @ msg => component.")
                wrapper.append(mapping.getBeanMethodName+"(msg)\n")
                wrapper.append("}\n")
              }
            case None => {
                error("KevoreeProvidedPortGenerator::No mapping found for method '"+Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD+"' of MessagePort '" + ref.getName + "' in component '" + ct.getName + "'")
                error("No mapping found for method '"+Constants.KEVOREE_MESSAGEPORT_DEFAULTMETHOD+"' of MessagePort '" + ref.getName + "' in component '" + ct.getName + "'")
                
              }
          }
        }

      case sPT : ServicePortType=> {
          /* CREATE INTERFACE ACTOR MOK */
          sPT.getOperations.foreach{op=>
            /* GENERATE METHOD SIGNATURE */
            wrapper.append("def "+op.getName+"(")
            op.getParameters.foreach{param=>
              wrapper.append(param.getName+":"+param.getType.print('[',']'))
              if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){wrapper.append(",")}
            }
            wrapper.append(") : "+op.getReturnType.print('[',']')+" ={\n");


            /* Generate method corpus */
            /* CREATE MSG OP CALL */
            wrapper.append("var msgcall = new org.kevoree.framework.MethodCallMessage\n")
            wrapper.append("msgcall.setMethodName(\""+op.getName+"\");\n")
            op.getParameters.foreach{param=>
              wrapper.append("msgcall.getParams.put(\""+param.getName+"\","+param.getName+".asInstanceOf[AnyRef]);\n")
            }
            wrapper.append("(this !? msgcall).asInstanceOf["+op.getReturnType.print('[',']')+"]")
            wrapper.append("}\n")
          }
          /* CREATE ACTOR LOOP */
          wrapper.append("override def internal_process(msg : Any)=msg match {\n")
          wrapper.append("case opcall : org.kevoree.framework.MethodCallMessage => reply(opcall.getMethodName match {\n")
          sPT.getOperations.foreach{op=>
            /* FOUND METHOD MAPPING */
            ref.getMappings.find(map=>{map.getServiceMethodName.equals(op.getName)}) match {
              case Some(mapping)=> {
                  wrapper.append("case \""+op.getName+"\"=> component."+mapping.getBeanMethodName+"(")
                  op.getParameters.foreach{param=>
                    wrapper.append("opcall.getParams.get(\""+param.getName+"\").asInstanceOf["+param.getType.print('[',']')+"]")
                    if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){wrapper.append(",")}
                  }
                  wrapper.append(")\n")

                }
              case None => {
                  error("No mapping found for method '"+op.getName+"' of ServicePort '" + ref.getName + "' in component '" + ct.getName + "'")
                  //println("No mapping found for method '"+op.getName+"' of ServicePort '" + ref.getName + "' in component '" + ct.getName + "'")
                  
                }
            }
          }
          wrapper.append("case _ @ o => println(\"uncatch message , method not found in service declaration : \"+o);null \n")
          wrapper.append("})}\n")
        }

    }

    wrapper.append("}\n");
    wrapper.close();
  }

}
