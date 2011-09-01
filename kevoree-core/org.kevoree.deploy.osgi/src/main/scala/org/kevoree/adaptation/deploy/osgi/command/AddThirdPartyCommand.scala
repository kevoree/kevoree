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
///**
// * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 	http://www.gnu.org/licenses/lgpl-3.0.txt
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package org.kevoree.adaptation.deploy.osgi.command
//
//import org.kevoree.adaptation.deploy.osgi.context.KevoreeDeployManager
//import org.kevoree.adaptation.deploy.osgi.context.KevoreeOSGiBundle
//import org.osgi.framework.BundleException
//import org.osgi.service.packageadmin.PackageAdmin
//import org.slf4j.LoggerFactory
//import scala.collection.JavaConversions._
//import org.kevoree.{ContainerRoot, DeployUnit}
//
//case class AddThirdPartyCommand(deployUnit: DeployUnit, ctx: KevoreeDeployManager) extends PrimitiveCommand {
//
//  var logger = LoggerFactory.getLogger(this.getClass)
//
//  def execute(): Boolean = {
//
//    var url: List[String] = List()
//    url = url ++ List(deployUnit.getUrl)
//    if (deployUnit.getUrl.contains("mvn:")) {
//      CommandHelper.buildPotentialMavenURL(deployUnit.eContainer.asInstanceOf[ContainerRoot]).foreach {
//        urlRepo =>
//		url = url ++ List(deployUnit.getUrl.replace("mvn:", "mvn:" + urlRepo + "!"))
//      }
//    }
//    //DEBUG
//    url.foreach({
//		u =>
//        logger.debug("potential url=" + u)
//	  })
//    url.exists({
//		u =>
//        installBundle(u)
//	  })
//  }
//
//  def installBundle(url: String): Boolean = {
//    logger.debug("CMD ADD ThirdParty EXECUTION => url=" + url);
//    /* Actually deploy only bundle from library  */
//    try {
//      lastExecutionBundle = Some(ctx.bundleContext.installBundle(url));
//      val symbolicName: String = lastExecutionBundle.get.getSymbolicName
//      ctx.bundleMapping.append(KevoreeOSGiBundle(deployUnit.getName, deployUnit.getClass.getName, lastExecutionBundle.get))
//      // lastExecutionBundle.get.start
//      mustBeStarted = true
//      true
//    } catch {
//      case e: BundleException if (e.getType == BundleException.DUPLICATE_BUNDLE_ERROR) => {
//		  logger.warn("ThirdParty conflict ! ")
//		  mustBeStarted = false
//		  true
//		}
//      case _@e => {
//        logger.error("Install error => ",e)
//		  false
//		}
//    }
//  }
//
//
//  def undo() = {
//    try {
//      lastExecutionBundle match {
//        case Some(bundle) => {
//			bundle.stop;
//			bundle.uninstall
//			val srPackageAdmin = ctx.bundleContext.getServiceReference(classOf[PackageAdmin].getName)
//			val padmin: PackageAdmin = ctx.bundleContext.getService(srPackageAdmin).asInstanceOf[PackageAdmin]
//			padmin.resolveBundles(Array(bundle))
//		  }
//        case None => //NOTHING CAN BE DOING HERE
//      }
//    } catch {
//      case _ =>
//    }
//
//    /* TODO CALL refreshPackage */
//  }
//
//
//}
