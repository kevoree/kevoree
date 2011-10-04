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
import com.sun.mirror.apt.Filer

import org.kevoree.ComponentType
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.framework.KevoreeGeneratorHelper

object KevoreeGenerator {

  /* GENERATE WRAPPER FOR DECLARATIF PORT */
  def generatePortWrapper(root:ContainerRoot,filer:Filer,targetNodeType:String){
    root.getTypeDefinitions.filter(p=> p.isInstanceOf[ComponentType]).foreach{ctt=> var ct = ctt.asInstanceOf[ComponentType]
      ct.getProvided.foreach{ref=>


        val portPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct,targetNodeType)
        val portName = ct.getName+"PORT"+ref.getName;
        val wrapper = filer.createSourceFile(portPackage+"."+portName);
        wrapper.append("package "+portPackage+";\n");
        wrapper.append("import org.kevoree.framework.AbstractPort;\n");
        wrapper.append("import "+KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct)+"._\n")
        wrapper.append("import "+ref.getRef.getName+";\n");
        wrapper.append("public class "+portName+" extends AbstractPort implements "+ref.getRef.getName+" {\n");
        //wrapper.append("public "+portName+"(Object c){setComponent(c);}\n") /* AVOID CIRCULAR REFERENCE */
        ref.getRef match {
          case sPT : ServicePortType=> sPT.getOperations.foreach{op=>
              wrapper.append("public "+op.getReturnType.get.getName+" "+op.getName+"(")
              op.getParameters.foreach{param=>
                wrapper.append(param.getType.get.print('<','>')+" "+param.getName)
                if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){
                  wrapper.append(",")
                }
              }
              wrapper.append("){\n");
              if(!op.getReturnType.get.getName.equals("void")) { wrapper.append("return ") }
              wrapper.append("(("+ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))+")getComponent()).")

              ref.getMappings.find(map=>{map.getServiceMethodName.equals(op.getName)}) match {
                case Some(mapping)=>wrapper.append(mapping.getBeanMethodName+"(")
                case None => println("WARNING METHOD NOT MAP "+op.getName)
              }

              //wrapper.append(ref.getMappings.find(map=>{map.getServiceMethodName.equals(op.getName)}).get.getBeanMethodName+"(")
              op.getParameters.foreach{param=>
                wrapper.append(param.getName)
                if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){
                  wrapper.append(",")
                }
              }
              wrapper.append(");}\n")
            }
          case mPT : MessagePortType => {
              ref.getMappings.find(map=>{map.getServiceMethodName.equals("process")}) match {
                case Some(mapping)=>{
                    wrapper.append("public void process(Object o){\n")
                    wrapper.append("(("+ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))+")getComponent()).")
                    wrapper.append(mapping.getBeanMethodName+"(o);\n")
                    wrapper.append("}\n")
                  }
                case None => println("WARNING METHOD NOT MAP process")
              }
            }
          case _=> //TODO MESSAGE PORT
        }

        wrapper.append("}\n");
        wrapper.close();
      }
    }
  }

  def generatePort(root:ContainerRoot,filer:Filer,targetNodeType:String){
    root.getTypeDefinitions.filter(p=> p.isInstanceOf[ComponentType]).foreach{ctt=> var ct = ctt.asInstanceOf[ComponentType]
      ct.getProvided.foreach{ref=> KevoreeProvidedPortGenerator.generate(root, filer, ct, ref,targetNodeType)  }
      ct.getRequired.foreach{ref=> KevoreeRequiredPortGenerator.generate(root, filer, ct, ref,targetNodeType)  }
    }
  }


}
