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

package org.kevoree.adaptation.deploy.osgi.command

import java.io.File
import org.kevoree._
import framework.{KevoreeGeneratorHelper, Constants}
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
import org.kevoree.framework.FileHelper._
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

case class AddInstanceCommand (c: Instance, ctx: KevoreeDeployManager, nodeName: String) extends PrimitiveCommand {

  var logger = LoggerFactory.getLogger(this.getClass);

  def execute (): Boolean = {

    /* create bundle cache structure */
    val directory = ctx.bundle.getBundleContext.getDataFile("dyanmicBundle_" + c.getName)
    directory.mkdir

    val METAINFDIR = new File(directory.getAbsolutePath + "/" + "META-INF")
    METAINFDIR.mkdir

    // var directoryWrapper = ctx.bundle.getBundleContext.getDataFile("dyanmicBundle_"+c.getName+"Wrapper")
    // directoryWrapper.mkdir

    //var METAINFDIRWRAPPER = new File(directoryWrapper.getAbsolutePath+"/"+"META-INF")
    // METAINFDIRWRAPPER.mkdir


    //FOUND CT SYMBOLIC NAME
    val mappingFound = ctx.bundleMapping.find({
      bundle => bundle.name == c.getTypeDefinition.getName &&
        bundle.objClassName == c.getTypeDefinition.getClass.getName
    }) match {
      case Some(bundle) => bundle
      case None => logger.error("Type Not Found"); return false; null;
    }

    if (mappingFound != null) {
      //FOUND CURRENT NODE TYPE
      val nodeTypeName = c.getTypeDefinition.eContainer().asInstanceOf[ContainerRoot].getNodes
        .find(tn => tn.getName == nodeName).get.getTypeDefinition.getName

      /* STEP GENERATE COMPONENT INSTANCE BUNDLE */
      /* Generate File */
      val MANIFEST = new File(METAINFDIR + "/" + "MANIFEST.MF")

      val activatorPackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(c.getTypeDefinition, nodeTypeName)

      // val activatorPackage = c.getTypeDefinition.getFactoryBean.substring(0, c.getTypeDefinition.getFactoryBean.lastIndexOf("."))
      val activatorName = c.getTypeDefinition.getName + "Activator"
      MANIFEST.write(List("Manifest-Version: 1.0",
                           "Bundle-SymbolicName: " + c.getName,
                           "Bundle-Version: 1",
                           "DynamicImport-Package: *",
                           "Bundle-ManifestVersion: 2",
                           "Bundle-Activator: " + activatorPackage + "." + activatorName,
                           Constants.KEVOREE_INSTANCE_NAME_HEADER + ": " + c.getName,
                           Constants.KEVOREE_NODE_NAME_HEADER + ": " + nodeName,
                           "Require-Bundle: " + mappingFound.bundle.getSymbolicName
                         ))
      try {
        val bundle = ctx.bundleContext.installBundle("assembly:file://" + directory.getAbsolutePath);
        ctx.bundleMapping.add(KevoreeOSGiBundle(c.getName, c.getClass.getName, bundle))
        lastExecutionBundle = Some(bundle)
        bundle.start()
        mustBeStarted = true
        val typebundlestartLevel = ctx.getStartLevelServer.getBundleStartLevel(mappingFound.bundle)
        startLevel = Some(typebundlestartLevel + 1)
        true
      } catch {
        case _@e => logger.error("Error while deploy Kevoree Instance", e); false
      }
    } else {
      false
    }
  }

  def undo () {
    lastExecutionBundle match {
      case None =>
      case Some(b) => {
        try {
          b.stop();
          b.uninstall()
          (ctx.bundleMapping.filter(map => map.bundle == b).toList ++ List()).foreach {
            map =>
              ctx.bundleMapping.remove(map)
          }

        } catch {
          case _@e => // e.printStackTrace
        }
      }
    }


    /* TODO CALL refreshPackage */
  }

  //  var lastExecutionBundle : Option[org.osgi.framework.Bundle] = None

}
