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

import java.io.File
import org.kevoree.ContainerRoot
import org.kevoree.MessagePortType
import org.kevoree.PortTypeRef
import org.kevoree.{ComponentType => KevoreeComponentType }
import org.kevoree.ServicePortType
import org.kevoree.framework.aspects.KevoreeAspects._

import org.kevoree.framework.KevoreeGeneratorHelper
import javax.annotation.processing.Filer
import javax.tools.{StandardLocation}

object KevoreeRequiredPortGenerator {

  def generate(root:ContainerRoot,filer:Filer,ct: KevoreeComponentType,ref:PortTypeRef,targetNodeType:String){
   // var portPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));

    val portPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct,targetNodeType)
    val portName = ct.getName+"PORT"+ref.getName
    val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(portPackage.replace(".", "/")+"/"+portName+".scala"));
    val writer = wrapper.openWriter()

    writer.append("package "+portPackage+"\n");
    writer.append("import org.kevoree.framework.port._\n");
    writer.append("import scala.{Unit=>void}\n")
    writer.append("import "+KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct)+"._\n")
    writer.append("class "+portName+"(component : "+ct.getName+") extends "+ref.getRef.getName+" with KevoreeRequiredPort {\n");

    writer.append("def getName : String = \""+ref.getName+"\"\n")
    writer.append("def getComponentName : String = component.getName \n")

    ref.getRef match {
      case mPT : MessagePortType => {
          /* GENERATE METHOD MAPPING */
        writer.append("def process(o : Object) = {this ! o}\n")
        writer.append("def getInOut = false\n")
        }

      case sPT : ServicePortType=> {
        writer.append("def getInOut = true\n")
          /* CREATE INTERFACE ACTOR MOK */
          sPT.getOperations.foreach{op=>
            /* GENERATE METHOD SIGNATURE */
            writer.append("def "+op.getName+"(")
            op.getParameters.foreach{param=>
              writer.append(param.getName+":"+param.getType.get.print('[',']'))
              if(op.getParameters.indexOf(param) != (op.getParameters.size-1)){writer.append(",")}
            }
            writer.append(") : "+op.getReturnType.get.print('[',']')+" ={\n");

            /* Generate method corpus */
            /* CREATE MSG OP CALL */
            writer.append("var msgcall = new org.kevoree.framework.MethodCallMessage\n")
            writer.append("msgcall.setMethodName(\""+op.getName+"\");\n")
            op.getParameters.foreach{param=>
              writer.append("msgcall.getParams.put(\""+param.getName+"\","+param.getName+".asInstanceOf[AnyRef]);\n")
            }

            writer.append("(this !? msgcall).asInstanceOf["+op.getReturnType.get.print('[',']')+"]")
            writer.append("}\n")
          }
        }

    }

    writer.append("}\n");
    writer.close();
  }


}
