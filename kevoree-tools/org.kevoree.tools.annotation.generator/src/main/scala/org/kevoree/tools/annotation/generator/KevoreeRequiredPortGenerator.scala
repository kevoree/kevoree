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

package org.kevoree.tools.annotation.generator

import java.io.File

import org.kevoree.framework.KevoreeGeneratorHelper
import javax.annotation.processing.Filer
import javax.tools.{StandardLocation}
import org.kevoree.{TypedElement, ContainerRoot, MessagePortType, PortTypeRef, ComponentType => KevoreeComponentType, ServicePortType}
import org.kevoree.annotation.ThreadStrategy
import scala.collection.JavaConversions._


object KevoreeRequiredPortGenerator {

  def generate(root: ContainerRoot, filer: Filer, ct: KevoreeComponentType, ref: PortTypeRef, targetNodeType: String) {
    // var portPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));

    val portPackage = new KevoreeGeneratorHelper().getTypeDefinitionGeneratedPackage(ct, targetNodeType)
    val portName = ct.getName + "PORT" + ref.getName
    val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(portPackage.replace(".", "/") + "/" + portName + ".scala"))
    val writer = wrapper.openWriter()

    writer.append("package " + portPackage + "\n")
    writer.append("import org.kevoree.framework.port._\n")
    writer.append("import scala.{Unit=>void}\n")
    writer.append("import " + new KevoreeGeneratorHelper().getTypeDefinitionBasePackage(ct) + "._\n")


    var baseName = ref.getRef.getName
    if(ref.getRef.isInstanceOf[MessagePortType]){
      baseName = "org.kevoree.framework.MessagePort"
    }

    ThreadingMapping.getMappings.get(Tuple2(ct.getName, ref.getName)) match {
      case ThreadStrategy.THREAD_QUEUE => {
        writer.append("class " + portName + "(component : " + ct.getName + ") extends " + baseName + " with KevoreeRequiredThreadPort {\n")
      }
      case ThreadStrategy.SHARED_THREAD => {
        writer.append("class " + portName + "(component : " + ct.getName + ") extends " + baseName + " with KevoreeRequiredExecutorPort {\n")
      }
      case ThreadStrategy.NONE => {
        writer.append("class " + portName + "(component : " + ct.getName + ") extends " + baseName + " with KevoreeRequiredNonePort {\n")
      }
      case _ => {
        writer.append("class " + portName + "(component : " + ct.getName + ") extends " + baseName + " with KevoreeRequiredPort {\n")
      }
    }


    writer.append("def getName : String = \"" + ref.getName + "\"\n")
    writer.append("def getComponentName : String = component.getName \n")

    ref.getRef match {
      case mPT: MessagePortType => {
        /* GENERATE METHOD MAPPING */
        writer.append("def process(o : Object) = {\n")
        writer.append("{this ! o}\n")
        /*
         writer.append("o match {\n")
           writer.append("case msg : org.kevoree.framework.KevoreeMessage => {this ! o}\n")
           writer.append("case _ => {\n")
           writer.append("val m = new org.kevoree.framework.message.StdKevoreeMessage\n")
           writer.append("m.setValue(\"*\",o)\n")
           writer.append("this ! o")
           writer.append("}\n")
         writer.append("}\n") */
        writer.append("}\n")
        writer.append("def getInOut = false\n")
      }

      case sPT: ServicePortType => {
        writer.append("def getInOut = true\n")
        /* CREATE INTERFACE ACTOR MOK */
        sPT.getOperations.foreach {
          op =>
          /* GENERATE METHOD SIGNATURE */
            writer.append("def " + op.getName + "(")
            var i = 0
            op.getParameters.sortWith((p1,p2)=>p2.getOrder > p1.getOrder).foreach {
              param =>
                if (i != 0) {
                  writer.append(",")
                }
                writer.append(GeneratorHelper.protectReservedWord(param.getName) + ":" + Printer.print(param.getType, '[', ']'))
                i = i +1
            }

            var rt = op.getReturnType.getName
            if (op.getReturnType.getGenericTypes.size > 0) {
              rt += op.getReturnType.getGenericTypes.collect {
                case s: TypedElement => s.getName
              }.mkString("[", ",", "]")
            }
            writer.append(") : " + rt + " ={\n")

            /* Generate method corpus */
            /* CREATE MSG OP CALL */
            writer.append("val msgcall = new org.kevoree.framework.MethodCallMessage\n")
            writer.append("msgcall.setMethodName(\"" + op.getName + "\")\n")
            op.getParameters.sortWith((p1,p2)=>p2.getOrder > p1.getOrder).foreach {
              param =>
                writer.append("msgcall.getParams.put(\"" + param.getName + "\"," + GeneratorHelper.protectReservedWord(param.getName) + ".asInstanceOf[AnyRef])\n")
            }

            writer.append("(this !? msgcall).asInstanceOf[" + rt + "]")
            writer.append("}\n")
        }
      }

    }

    writer.append("}\n")
    writer.close()
  }


}
