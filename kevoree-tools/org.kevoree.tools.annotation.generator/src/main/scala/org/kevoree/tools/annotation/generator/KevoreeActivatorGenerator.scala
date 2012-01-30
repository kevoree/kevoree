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
import org.kevoree.{GroupType, ContainerRoot, ChannelType, ComponentType}
import org.kevoree.framework.KevoreeGeneratorHelper
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

object KevoreeActivatorGenerator {
  def generateActivator(root: ContainerRoot, filer: Filer, targetNodeType : String) {
    root.getTypeDefinitions.filter(td => td.getBean != "").filter(td => td.getBean != "").filter(p => p.isInstanceOf[ComponentType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ComponentType]
        val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct,targetNodeType)
        val activatorName = ct.getName + "Activator"
        val wrapper =  filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(activatorPackage.replace(".", "/") + "/" + activatorName + ".scala"))
        /* GENERATE CONTENT */
        val writer = wrapper.openWriter()
        writer.append("package " + activatorPackage + ";\n");

        /* IMPORTED PACKAGE */
        writer.append("import java.util.Hashtable\n")
        writer.append("import org.kevoree.api.service.core.handler.KevoreeModelHandlerService\n")
       // writer.append("import org.osgi.framework.BundleActivator\n")
       // writer.append("import org.osgi.framework.BundleContext\n")
       // writer.append("import org.osgi.util.tracker.ServiceTracker\n")
        writer.append("import org.kevoree.framework.KevoreeActor\n")
        writer.append("import org.kevoree.framework.KevoreeComponent\n")
        writer.append("import org.kevoree.framework._\n")
        writer.append("import "+KevoreeGeneratorHelper.getTypeDefinitionBasePackage(ct)+"._\n")
        writer.append("class " + activatorName + " extends org.kevoree.framework.osgi.KevoreeComponentActivator {\n")
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        writer.append("def callFactory() : KevoreeComponent = { " + factoryName + ".createComponentActor() } ")
        writer.append("}\n")

        /* END CONTENT GENERATION */
        writer.close();
    }

    /* STEP CHANNEL TYPE DEFINITION */
    root.getTypeDefinitions.filter(td => td.getBean != "").filter(td => td.getBean != "").filter(p => p.isInstanceOf[ChannelType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ChannelType]
        val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(ct,targetNodeType)
        val activatorName = ct.getName + "Activator"
        val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(activatorPackage.replace(".", "/") + "/" + activatorName + ".scala"))
        /* GENERATE CONTENT */
        val writer = wrapper.openWriter()
        writer.append("package " + activatorPackage + ";\n");
        /* IMPORTED PACKAGE */
        writer.append("import java.util.Hashtable\n")
        writer.append("import org.kevoree.api.service.core.handler.KevoreeModelHandlerService\n")
      //  writer.append("import org.osgi.framework.BundleActivator\n")
     //   writer.append("import org.osgi.framework.BundleContext\n")
       // writer.append("import org.osgi.util.tracker.ServiceTracker\n")
        writer.append("import org.kevoree.framework.KevoreeActor\n")
        writer.append("import org.kevoree.framework._\n")
        writer.append("import org.kevoree.framework.KevoreeComponent\n")
        writer.append("class " + activatorName + " extends org.kevoree.framework.osgi.KevoreeChannelFragmentActivator {\n")
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        writer.append("def callFactory() : org.kevoree.framework.KevoreeChannelFragment = { " + factoryName + ".createChannel() } ")
        writer.append("}\n")
        /* END CONTENT GENERATION */
        writer.close();

    }

    root.getTypeDefinitions.filter(td => td.getBean != "").filter(td => td.getBean != "").filter(p => p.isInstanceOf[GroupType]).foreach {
      gt =>
        val groupType = gt.asInstanceOf[GroupType]
        val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(gt,targetNodeType)
        val activatorName = groupType.getName + "Activator"
        val wrapper = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", new String(activatorPackage.replace(".", "/") + "/" + activatorName + ".scala"))
        /* GENERATE CONTENT */
        val writer = wrapper.openWriter()
        writer.append("package " + activatorPackage + ";\n");
        /* IMPORTED PACKAGE */
        writer.append("import java.util.Hashtable\n")
        writer.append("import org.kevoree.api.service.core.handler.KevoreeModelHandlerService\n")
       // writer.append("import org.osgi.framework.BundleActivator\n")
       // writer.append("import org.osgi.framework.BundleContext\n")
      //  writer.append("import org.osgi.util.tracker.ServiceTracker\n")
        writer.append("import org.kevoree.framework.KevoreeActor\n")
        writer.append("import org.kevoree.framework._\n")
        writer.append("import org.kevoree.framework.KevoreeComponent\n")
        writer.append("class " + activatorName + " extends org.kevoree.framework.osgi.KevoreeGroupActivator {\n")
        val factoryName = groupType.getFactoryBean.substring(groupType.getFactoryBean.lastIndexOf(".") + 1)
        writer.append("def callFactory() : org.kevoree.framework.KevoreeGroup = { " + factoryName + ".createGroup() } ")
        writer.append("}\n")
        /* END CONTENT GENERATION */
        writer.close();
    }

  }
}
