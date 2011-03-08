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
import scala.collection.JavaConversions._
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.{GroupType, ContainerRoot, ChannelType, ComponentType}

object KevoreeFactoryGenerator {

  /* GENERATE FACTORY FOR COMPONENT & PORT  */
  def generateFactory(root: ContainerRoot, filer: Filer) {

    /* STEP COMPONENT TYPE DEFINITION */
    root.getTypeDefinitions.filter(p => p.isInstanceOf[ComponentType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ComponentType]
        val componentPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        val componentBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))
        val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(componentPackage.replace(".", "/") + "/" + factoryName + ".scala"), "UTF-8");

        //var wrapper = filer.createSourceFile(ct.getFactoryBean);
        wrapper.append("package " + componentPackage + "\n");
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("object " + factoryName + "{\n")

        /* create Component Actor */
        wrapper.append("def createComponentActor() : KevoreeComponent = {\n")
        wrapper.append("new KevoreeComponent(create" + ct.getName + "()){")

        if (ct.getStartMethod == null) {
          error("Start method is mandatory for component name => " + ct.getName);
        }
        if (ct.getStopMethod == null) {
          error("Stop method is mandatory for component name => " + ct.getName);
        }
        wrapper.append("def startComponent(){getKevoreeComponentType.asInstanceOf[" + componentBean + "]." + ct.getStartMethod + "()}\n")
        wrapper.append("def stopComponent(){getKevoreeComponentType.asInstanceOf[" + componentBean + "]." + ct.getStopMethod + "()}\n")

        if (ct.getUpdateMethod != null) {
          wrapper.append("override def updateComponent(){getKevoreeComponentType.asInstanceOf[" + componentBean + "]." + ct.getUpdateMethod + "()}\n")
        }


        wrapper.append("}}\n")

        /* create Component */
        wrapper.append("def " + "create" + ct.getName + "() : " + componentBean + " ={\n")
        wrapper.append("var newcomponent = new " + componentBean + "();\n")
        /* INJECT HOSTED PORT */
        ct.getProvided.foreach {
          ref =>
            var portName = ct.getName() + "PORT" + ref.getName();
            wrapper.append("newcomponent.getHostedPorts().put(\"" + ref.getName() + "\",create" + portName + "(newcomponent))\n")
        }
        ct.getRequired.foreach {
          ref =>
            var portName = ct.getName() + "PORT" + ref.getName();
            wrapper.append("newcomponent.getNeededPorts().put(\"" + ref.getName() + "\",create" + portName + "())\n")
        }
        wrapper.append("newcomponent}\n")

        /* CREATE NEW PROVIDED PORT & PROXY */
        ct.getProvided.foreach {
          ref =>
            var portName = ct.getName() + "PORT" + ref.getName();
            // var portNameProxy = ct.getName()+"PORTPROXY"+ref.getName();
            wrapper.append("def create" + portName + "(component : " + ct.getName + ") : " + portName + " ={ new " + portName + "(component)}\n")
          //  wrapper.append("def create"+portNameProxy+"() : "+portNameProxy+" = { new "+portNameProxy+"()}\n")
        }

        /* CREATE NEW REQUIRED PROXY */
        ct.getRequired.foreach {
          ref =>
            var portName = ct.getName() + "PORT" + ref.getName();
            //        var portNameProxy = ct.getName()+"PORTPROXY"+ref.getName();

            wrapper.append("def create" + portName + "() : " + portName + " ={ return new " + portName + "();}\n")

          //wrapper.append("public static "+portName+" create"+portName+"(){ return new "+portName+"();}\n")
          //wrapper.append("def create"+portNameProxy+"() : "+portNameProxy+" ={ return new "+portNameProxy+"();}\n")
        }

        wrapper.append("}\n")
        wrapper.close
    }

    /* STEP CHANNEL TYPE DEFINITION */
    root.getTypeDefinitions.filter(p => p.isInstanceOf[ChannelType]).foreach {
      ctt =>
        var ct = ctt.asInstanceOf[ChannelType]
        var channelTypePackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
        var factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        var componentBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))
        var wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(channelTypePackage.replace(".", "/") + "/" + factoryName + ".scala"), "UTF-8");

        //var wrapper = filer.createSourceFile(ct.getFactoryBean);
        wrapper.append("package " + channelTypePackage + "\n");
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("object " + factoryName + "{\n")

        wrapper.append("def createChannel()={new " + ct.getBean + " with ChannelTypeFragment {\n")


        if (ct.getStartMethod != null) {
          wrapper.append("override def startChannelFragment(){this.asInstanceOf[" + componentBean + "]." + ct.getStartMethod + "()}\n")
        }
        if (ct.getStopMethod != null) {
          wrapper.append("override def stopChannelFragment(){this.asInstanceOf[" + componentBean + "]." + ct.getStopMethod + "()}\n")
        }
        if (ct.getUpdateMethod != null) {
          wrapper.append("override def updateChannelFragment(){this.asInstanceOf[" + componentBean + "]." + ct.getUpdateMethod + "()}\n")
        }
        wrapper.append("}}}\n")
    }



    /* Group Type Step */
    root.getTypeDefinitions.filter(p => p.isInstanceOf[GroupType]).foreach {
      gt =>
        val ct = gt.asInstanceOf[GroupType]
        val groupTypePackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."));
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        val componentBean = ct.getFactoryBean.substring(0, ct.getFactoryBean.indexOf("Factory"))
        val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(groupTypePackage.replace(".", "/") + "/" + factoryName + ".scala"), "UTF-8");

        //var wrapper = filer.createSourceFile(ct.getFactoryBean);
        wrapper.append("package " + groupTypePackage + "\n");
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("object " + factoryName + "{\n")

        wrapper.append("def createGroup()={new " + ct.getBean + " with KevoreeGroup {\n")


        if (ct.getStartMethod != null) {
          wrapper.append("override def startGroup(){this.asInstanceOf[" + componentBean + "]." + ct.getStartMethod + "()}\n")
        }
        if (ct.getStopMethod != null) {
          wrapper.append("override def stopGroup(){this.asInstanceOf[" + componentBean + "]." + ct.getStopMethod + "()}\n")
        }
        if (ct.getUpdateMethod != null) {
          wrapper.append("override def updateGroup(){this.asInstanceOf[" + componentBean + "]." + ct.getUpdateMethod + "()}\n")
        }
        wrapper.append("}}}\n")
    }


  }

}
