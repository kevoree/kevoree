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

package org.kevoree.tools.annotation.generator

import com.sun.mirror.apt.Filer
import java.io.File
import org.kevoree.ContainerRoot
import org.kevoree.MessagePortType
import org.kevoree.PortTypeRef
import org.kevoree.{ComponentType => KevoreeComponentType }
import org.kevoree.ServicePortType
import org.kevoree.framework.aspects.KevoreeAspects._
import scala.collection.JavaConversions._
import org.kevoree.framework.KevoreeGeneratorHelper

object KevoreeRequiredPortGenerator {

  def generate(root:ContainerRoot,filer:Filer,ct: KevoreeComponentType,ref:PortTypeRef,targetNodeType:String){
   // var portPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));

    val portPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct,targetNodeType)
    val portName = ct.getName()+"PORT"+ref.getName();
    val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(portPackage.replace(".", "/")+"/"+portName+".scala"), "UTF-8");

    wrapper.append("package "+portPackage+"\n");
    wrapper.append("import org.kevoree.framework.port._\n");
    wrapper.append("import scala.{Unit=>void}\n")
    wrapper.append("import "+KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct)+"._\n")
    wrapper.append("class "+portName+"(component : "+ct.getName+") extends "+ref.getRef().getName()+" with KevoreeRequiredPort {\n");

    wrapper.append("def getName : String = \""+ref.getName+"\"\n")
    wrapper.append("def getComponentName : String = component.getName \n")

    ref.getRef match {
      case mPT : MessagePortType => {
          /* GENERATE METHOD MAPPING */
          wrapper.append("def process(o : Object) = {this ! o}\n")
          wrapper.append("def getInOut = false\n")
        }

      case sPT : ServicePortType=> {
          wrapper.append("def getInOut = true\n")
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
              wrapper.append("msgcall.getParams.put(\""+param.getName+"\","+param.getName+");\n")
            }

            wrapper.append("(this !? msgcall).asInstanceOf["+op.getReturnType.print('[',']')+"]")
            wrapper.append("}\n")
          }
        }

    }

    wrapper.append("}\n");
    wrapper.close();
  }


}
