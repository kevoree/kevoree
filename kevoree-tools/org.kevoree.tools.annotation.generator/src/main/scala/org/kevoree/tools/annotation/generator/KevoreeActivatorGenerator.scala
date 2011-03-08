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

object KevoreeActivatorGenerator {
  def generateActivator(root: ContainerRoot, filer: Filer) {
    root.getTypeDefinitions.filter(p => p.isInstanceOf[ComponentType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ComponentType]
        val activatorPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."))
        val activatorName = ct.getName() + "Activator"
        val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(activatorPackage.replace(".", "/") + "/" + activatorName + ".scala"), "UTF-8")
        /* GENERATE CONTENT */
        wrapper.append("package " + activatorPackage + ";\n");

        /* IMPORTED PACKAGE */
        wrapper.append("import java.util.Hashtable\n")
        wrapper.append("import org.kevoree.api.service.core.handler.KevoreeModelHandlerService\n")
        wrapper.append("import org.osgi.framework.BundleActivator\n")
        wrapper.append("import org.osgi.framework.BundleContext\n")
        wrapper.append("import org.osgi.util.tracker.ServiceTracker\n")
        wrapper.append("import org.kevoree.framework.KevoreeActor\n")
        wrapper.append("import org.kevoree.framework.KevoreeComponent\n")
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("class " + activatorName + " extends org.kevoree.framework.osgi.KevoreeComponentActivator {\n")
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        wrapper.append("def callFactory() : KevoreeComponent = { " + factoryName + ".createComponentActor() } ")
        wrapper.append("}\n")

        /* END CONTENT GENERATION */
        wrapper.close();
    }

    /* STEP CHANNEL TYPE DEFINITION */
    root.getTypeDefinitions.filter(p => p.isInstanceOf[ChannelType]).foreach {
      ctt =>
        val ct = ctt.asInstanceOf[ChannelType]
        val activatorPackage = ct.getFactoryBean().substring(0, ct.getFactoryBean().lastIndexOf("."))
        val activatorName = ct.getName() + "Activator"
        val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(activatorPackage.replace(".", "/") + "/" + activatorName + ".scala"), "UTF-8")
        /* GENERATE CONTENT */
        wrapper.append("package " + activatorPackage + ";\n");
        /* IMPORTED PACKAGE */
        wrapper.append("import java.util.Hashtable\n")
        wrapper.append("import org.kevoree.api.service.core.handler.KevoreeModelHandlerService\n")
        wrapper.append("import org.osgi.framework.BundleActivator\n")
        wrapper.append("import org.osgi.framework.BundleContext\n")
        wrapper.append("import org.osgi.util.tracker.ServiceTracker\n")
        wrapper.append("import org.kevoree.framework.KevoreeActor\n")
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("import org.kevoree.framework.KevoreeComponent\n")
        wrapper.append("class " + activatorName + " extends org.kevoree.framework.osgi.KevoreeChannelFragmentActivator {\n")
        val factoryName = ct.getFactoryBean.substring(ct.getFactoryBean.lastIndexOf(".") + 1)
        wrapper.append("def callFactory() : org.kevoree.framework.KevoreeChannelFragment = { " + factoryName + ".createChannel() } ")
        wrapper.append("}\n")
        /* END CONTENT GENERATION */
        wrapper.close();

    }

    root.getTypeDefinitions.filter(p => p.isInstanceOf[GroupType]).foreach {
      gt =>
        val groupType = gt.asInstanceOf[GroupType]
        val activatorPackage = groupType.getFactoryBean().substring(0, groupType.getFactoryBean().lastIndexOf("."))
        val activatorName = groupType.getName() + "Activator"
        val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new File(activatorPackage.replace(".", "/") + "/" + activatorName + ".scala"), "UTF-8")
        /* GENERATE CONTENT */
        wrapper.append("package " + activatorPackage + ";\n");
        /* IMPORTED PACKAGE */
        wrapper.append("import java.util.Hashtable\n")
        wrapper.append("import org.kevoree.api.service.core.handler.KevoreeModelHandlerService\n")
        wrapper.append("import org.osgi.framework.BundleActivator\n")
        wrapper.append("import org.osgi.framework.BundleContext\n")
        wrapper.append("import org.osgi.util.tracker.ServiceTracker\n")
        wrapper.append("import org.kevoree.framework.KevoreeActor\n")
        wrapper.append("import org.kevoree.framework._\n")
        wrapper.append("import org.kevoree.framework.KevoreeComponent\n")
        wrapper.append("class " + activatorName + " extends org.kevoree.framework.osgi.KevoreeGroupActivator {\n")
        val factoryName = groupType.getFactoryBean.substring(groupType.getFactoryBean.lastIndexOf(".") + 1)
        wrapper.append("def callFactory() : org.kevoree.framework.KevoreeGroup = { " + factoryName + ".createGroup() } ")
        wrapper.append("}\n")
        /* END CONTENT GENERATION */
        wrapper.close();
    }

  }
}
