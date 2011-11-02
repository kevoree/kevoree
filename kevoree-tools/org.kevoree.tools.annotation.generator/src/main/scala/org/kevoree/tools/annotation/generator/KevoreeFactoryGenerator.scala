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

import java.io.File

import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.framework.KevoreeGeneratorHelper
import org.kevoree._
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

object KevoreeFactoryGenerator {

  /* GENERATE FACTORY FOR COMPONENT & PORT  */
  def generateFactory(root: ContainerRoot, filer: Filer, targetNodeType: String) {

    /* STEP COMPONENT TYPE DEFINITION */
    root.getTypeDefinitions.filter(td => td.getBean != "").filter(p => p.isInstanceOf[ComponentType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ComponentType]
        //val componentPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));

        val componentPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct, targetNodeType)
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        val componentBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))
        val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(componentPackage.replace(".", "/") + "/" + factoryName + ".scala"))
        val writer = wrapper.openWriter()
        writer.append("package " + componentPackage + "\n");
        writer.append("import org.kevoree.framework._\n")
        writer.append("import " + KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct) + "._\n")
        writer.append("object " + factoryName + "{\n")

        /* create Component Actor */
        writer.append("def createComponentActor() : KevoreeComponent = {\n")
        writer.append("new KevoreeComponent(create" + ct.getName + "()){")

        if (ct.getStartMethod == "") {
          error("Start method is mandatory for component name => " + ct.getName);
        }
        if (ct.getStopMethod == "") {
          error("Stop method is mandatory for component name => " + ct.getName);
        }
        writer.append("def startComponent(){getKevoreeComponentType.asInstanceOf[" + componentBean + "]." +
          ct.getStartMethod + "()}\n")
        writer.append("def stopComponent(){getKevoreeComponentType.asInstanceOf[" + componentBean + "]." +
          ct.getStopMethod + "()}\n")

        if (ct.getUpdateMethod != "") {
          writer.append("override def updateComponent(){getKevoreeComponentType.asInstanceOf[" + componentBean + "]." +
            ct.getUpdateMethod + "()}\n")
        }


        writer.append("}}\n")

        /* create Component */
        writer.append("def " + "create" + ct.getName + "() : " + componentBean + " ={\n")
        writer.append("var newcomponent = new " + componentBean + "();\n")
        /* INJECT HOSTED PORT */
        ct.getProvided.foreach {
          ref =>
            val portName = ct.getName + "PORT" + ref.getName
            writer.append("newcomponent.getHostedPorts().put(\"" + ref.getName + "\",create" + portName +
              "(newcomponent))\n")
        }
        ct.getRequired.foreach {
          ref =>
            val portName = ct.getName + "PORT" + ref.getName
            writer.append("newcomponent.getNeededPorts().put(\"" + ref.getName + "\",create" + portName +
              "(newcomponent))\n")
        }
        writer.append("newcomponent}\n")

        /* CREATE NEW PROVIDED PORT & PROXY */
        ct.getProvided.foreach {
          ref =>
            val portName = ct.getName + "PORT" + ref.getName
            // var portNameProxy = ct.getName()+"PORTPROXY"+ref.getName();
            writer.append("def create" + portName + "(component : " + ct.getName + ") : " + portName + " ={ new " +
              portName + "(component)}\n")
          //  wrapper.append("def create"+portNameProxy+"() : "+portNameProxy+" = { new "+portNameProxy+"()}\n")
        }

        /* CREATE NEW REQUIRED PROXY */
        ct.getRequired.foreach {
          ref =>
            val portName = ct.getName + "PORT" + ref.getName
            //        var portNameProxy = ct.getName()+"PORTPROXY"+ref.getName();

            writer
              .append("def create" + portName + "(component : " + ct.getName + ") : " + portName + " ={ return new " +
              portName + "(component);}\n")

          //wrapper.append("public static "+portName+" create"+portName+"(){ return new "+portName+"();}\n")
          //wrapper.append("def create"+portNameProxy+"() : "+portNameProxy+" ={ return new "+portNameProxy+"();}\n")
        }

        writer.append("}\n")
        writer.close()
    }

    /* STEP CHANNEL TYPE DEFINITION */
    root.getTypeDefinitions.filter(td => td.getBean != "").filter(p => p.isInstanceOf[ChannelType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ChannelType]
        val channelTypePackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct, targetNodeType)
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        val componentBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))
        val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(channelTypePackage.replace(".", "/") + "/" + factoryName + ".scala"));

        val writer = wrapper.openWriter()
        writer.append("package " + channelTypePackage + "\n");
        writer.append("import org.kevoree.framework._\n")
        writer.append("object " + factoryName + "{\n")

        writer.append("def createChannel()={new " + ct.getBean + " with ChannelTypeFragment {\n")


        if (ct.getStartMethod != "") {
          writer.append("override def startChannelFragment(){this.asInstanceOf[" + componentBean + "]." +
            ct.getStartMethod + "()}\n")
        }
        if (ct.getStopMethod != "") {
          writer
            .append("override def stopChannelFragment(){this.asInstanceOf[" + componentBean + "]." + ct.getStopMethod +
            "()}\n")
        }
        if (ct.getUpdateMethod != "") {
          writer.append("override def updateChannelFragment(){this.asInstanceOf[" + componentBean + "]." +
            ct.getUpdateMethod + "()}\n")
        }
        writer.append("}}}\n")
        writer.close()
    }

    /* STEP NODE TYPE DEFINITION */
    /*root.getTypeDefinitions.filter(p => p.isInstanceOf[NodeType]).foreach {
      nt =>
        val ct = nt.asInstanceOf[NodeType]
        //val groupTypePackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
        val nodeTypePackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct, targetNodeType)
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        val nodeBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))
        val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "",
                                            new File(nodeTypePackage.replace(".", "/") + "/" + factoryName + ".scala"),
                                            "UTF-8");

        wrapper.append("package " + nodeTypePackage + "\n");
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("object " + factoryName + "{\n")

        // TODO

        wrapper append ("}")
    }*/

    /* Group Type Step */
    root.getTypeDefinitions.filter(td => td.getBean != "").filter(p => p.isInstanceOf[GroupType]).foreach {
      gt =>
        val ct = gt.asInstanceOf[GroupType]
        //val groupTypePackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
        val groupTypePackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct, targetNodeType)
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        val componentBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))

        val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(groupTypePackage.replace(".", "/") + "/" + factoryName + ".scala"))
        val writer = wrapper.openWriter()
        writer.append("package " + groupTypePackage + "\n");
        writer.append("import org.kevoree.framework._\n")
        writer.append("object " + factoryName + "{\n")

        writer.append("def createGroup()={new " + ct.getBean + " with KevoreeGroup {\n")


        if (ct.getStartMethod != "") {
          writer
            .append("override def startGroup(){this.asInstanceOf[" + componentBean + "]." + ct.getStartMethod + "()}\n")
        }
        if (ct.getStopMethod != "") {
          writer
            .append("override def stopGroup(){this.asInstanceOf[" + componentBean + "]." + ct.getStopMethod + "()}\n")
        }
        if (ct.getUpdateMethod != "") {
          writer.append("override def updateGroup(){this.asInstanceOf[" + componentBean + "]." + ct.getUpdateMethod +
            "()}\n")
        }
        writer.append("}}}\n")
        writer.close()
    }


  }

}
