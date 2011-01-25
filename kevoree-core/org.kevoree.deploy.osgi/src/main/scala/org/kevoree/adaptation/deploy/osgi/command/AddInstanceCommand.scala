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
import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
import org.kevoree.framework.Constants
import org.kevoree.framework.FileHelper._
import scala.collection.JavaConversions._

case class AddInstanceCommand(c : Instance, ctx : KevoreeDeployManager,nodeName:String) extends PrimitiveCommand {

  def execute() : Boolean= {

    /* create bundle cache structure */
    var directory = ctx.bundle.getBundleContext.getDataFile("dyanmicBundle_"+c.getName)
    directory.mkdir

    var METAINFDIR = new File(directory.getAbsolutePath+"/"+"META-INF")
    METAINFDIR.mkdir

    // var directoryWrapper = ctx.bundle.getBundleContext.getDataFile("dyanmicBundle_"+c.getName+"Wrapper")
    // directoryWrapper.mkdir

    //var METAINFDIRWRAPPER = new File(directoryWrapper.getAbsolutePath+"/"+"META-INF")
    // METAINFDIRWRAPPER.mkdir


    //FOUND CT SYMBOLIC NAME
    var mappingFound =  ctx.bundleMapping.find({bundle =>bundle.name==c.getTypeDefinition.getName && bundle.objClassName==c.getTypeDefinition.getClass.getName}) match {
      case Some(bundle)=> bundle
      case None => println("Type Not Found"); return false; null;
    }

    /* STEP GENERATE COMPONENT INSTANCE BUNDLE */
    /* Generate File */
    var MANIFEST = new File(METAINFDIR+"/"+"MANIFEST.MF")

    var activatorPackage = c.getTypeDefinition.getFactoryBean().substring(0, c.getTypeDefinition.getFactoryBean().lastIndexOf("."))
    var activatorName = c.getTypeDefinition.getName()+"Activator"
    MANIFEST.write(List("Manifest-Version: 1.0",
                        "Bundle-SymbolicName: "+c.getName,
                        "Bundle-Version: 1",
                        "DynamicImport-Package: *",
                        "Bundle-ManifestVersion: 2",
                        "Bundle-Activator: "+activatorPackage+"."+activatorName,
                        Constants.KEVOREE_INSTANCE_NAME_HEADER+": "+c.getName,
                        Constants.KEVOREE_NODE_NAME_HEADER+": "+nodeName,
                        //   "Bundle-Blueprint: component.xml",
                        "Require-Bundle: "+mappingFound.bundle.getSymbolicName
      ))

    /*
     var MANIFESTWRAPPER = new File(METAINFDIRWRAPPER+"/"+"MANIFEST.MF")
     MANIFESTWRAPPER.write(List("Manifest-Version: 1.0",
     "Bundle-SymbolicName: "+c.getName+"Wrapper;blueprint.wait-for-dependencies:=true",
     "Bundle-Version: 1",
     "DynamicImport-Package: *",
     "Bundle-ManifestVersion: 2",
     "Bundle-Blueprint: componentWrapper.xml",
     "Require-Bundle: "+mappingFound.bundle.getSymbolicName
     ))
     */


    // var BLUEPRINTBASE = new File(directory+"/"+"component.xml")
    // BLUEPRINTBASE.write(AddComponentInstanceGenerator.generate(c))


    // var BLUEPRINTWRAPPER = new File(directoryWrapper+"/"+"componentWrapper.xml")
    //  BLUEPRINTWRAPPER.write(AddComponentInstanceWrapperGenerator.generate(c))

    // println(AddComponentInstanceGenerator.generate(c));
    println("Instance-DIRECTORY"+directory.getAbsolutePath)
    //  println("CI-DIRECTORY"+directoryWrapper.getAbsolutePath)
    try{
      var bundle= ctx.bundleContext.installBundle("assembly:"+directory.getAbsolutePath);
      // var bundleWrapper= ctx.bundleContext.installBundle("assembly:"+directoryWrapper.getAbsolutePath);
      //    executedBundles = List(bundle,bundleWrapper)

      ctx.bundleMapping.add(KevoreeOSGiBundle(c.getName,c.getClass.getName,bundle))
      // ctx.bundleMapping.append(KevoreeOSGiBundle(c,c.getName,c.getClass,bundleWrapper))

      lastExecutionBundle = Some(bundle)

      bundle.start
      //   bundleWrapper.start
      mustBeStarted = true
      true
    } catch {
      case _ @ e => e.printStackTrace;false
    }
  }

  def undo() = {
    lastExecutionBundle match {
      case None =>
      case Some(b)=>{
          try{
            b.stop;b.uninstall
          }catch{
            case _ @ e => e.printStackTrace
          }
        }
    }


    /* TODO CALL refreshPackage */
  }

//  var lastExecutionBundle : Option[org.osgi.framework.Bundle] = None

}
