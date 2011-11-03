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
/* $Id: KevoreeGenerator.scala 12438 2010-09-15 15:14:50Z francoisfouquet $
 * License    : EPL 								
 * Copyright  : IRISA / INRIA / Universite de Rennes 1 */

package org.kevoree.tools.annotation.generator

import org.kevoree.ContainerRoot
import org.kevoree.MessagePortType
import org.kevoree.ServicePortType
import org.kevoree.ComponentType
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.framework.KevoreeGeneratorHelper
import javax.annotation.processing.Filer

object KevoreeGenerator {

  /* GENERATE WRAPPER FOR DECLARATIF PORT */
  def generatePortWrapper(root:ContainerRoot,filer:javax.annotation.processing.Filer,targetNodeType:String){
    root.getTypeDefinitions.filter(p=> p.isInstanceOf[ComponentType]).foreach{ctt=> var ct = ctt.asInstanceOf[ComponentType]
      ct.getProvided.foreach{ref=>


        val portPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct,targetNodeType)
        val portName = ct.getName+"PORT"+ref.getName;
        val wrapper = filer.createSourceFile(portPackage+"."+portName)
        val writer = wrapper.openWriter
        writer.append("package "+portPackage+";\n");
        writer.append("import org.kevoree.framework.AbstractPort;\n");
        writer.append("import "+KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct)+"._\n")
        writer.append("import "+ref.getRef.getName+";\n");
        writer.append("public class "+portName+" extends AbstractPort implements "+ref.getRef.getName+" {\n");
        //wrapper.append("public "+portName+"(Object c){setComponent(c);}\n") /* AVOID CIRCULAR REFERENCE */
        ref.getRef match {
          case sPT : ServicePortType=> sPT.getOperations.foreach{op=>
            writer.append("public "+op.getReturnType.get.getName+" "+op.getName+"(")
              op.getParameters.foreach{param=>
                writer.append(param.getType.get.print('<','>')+" "+param.getName)
                if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){
                  writer.append(",")
                }
              }
            writer.append("){\n");
              if(!op.getReturnType.get.getName.equals("void")) { writer.append("return ") }
            writer.append("(("+ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))+")getComponent()).")

              ref.getMappings.find(map=>{map.getServiceMethodName.equals(op.getName)}) match {
                case Some(mapping)=>writer.append(mapping.getBeanMethodName+"(")
                case None => println("WARNING METHOD NOT MAP "+op.getName)
              }

              //wrapper.append(ref.getMappings.find(map=>{map.getServiceMethodName.equals(op.getName)}).get.getBeanMethodName+"(")
              op.getParameters.foreach{param=>
                writer.append(param.getName)
                if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){
                  writer.append(",")
                }
              }
            writer.append(");}\n")
            }
          case mPT : MessagePortType => {
              ref.getMappings.find(map=>{map.getServiceMethodName.equals("process")}) match {
                case Some(mapping)=>{
                  writer.append("public void process(Object o){\n")
                  writer.append("(("+ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))+")getComponent()).")
                  writer.append(mapping.getBeanMethodName+"(o);\n")
                  writer.append("}\n")
                  }
                case None => println("WARNING METHOD NOT MAP process")
              }
            }
          case _=> //TODO MESSAGE PORT
        }

        writer.append("}\n");
        writer.close();
      }
    }
  }

  def generatePort(root:ContainerRoot,filer:Filer,targetNodeType:String){
    root.getTypeDefinitions.filter(td => td.getBean != "").filter(p=> p.isInstanceOf[ComponentType]).foreach{ctt=> var ct = ctt.asInstanceOf[ComponentType]
      ct.getProvided.foreach{ref=> KevoreeProvidedPortGenerator.generate(root, filer, ct, ref,targetNodeType)  }
      ct.getRequired.foreach{ref=> KevoreeRequiredPortGenerator.generate(root, filer, ct, ref,targetNodeType)  }
    }
  }


}
