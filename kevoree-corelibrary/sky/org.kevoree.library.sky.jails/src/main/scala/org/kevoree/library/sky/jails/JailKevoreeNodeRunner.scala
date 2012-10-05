package org.kevoree.library.sky.jails

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

import nodeType.JailNode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import process.ProcessExecutor
import org.kevoree.library.sky.api.KevoreeNodeRunner
import java.io._
import org.kevoree.{KevoreeFactory, ContainerRoot}
import org.kevoree.framework.{KevoreeXmiHelper, Constants, KevoreePropertyHelper}


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
class JailKevoreeNodeRunner (nodeName: String, iaasNode: JailNode) extends KevoreeNodeRunner(nodeName) {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  val processExecutor = new ProcessExecutor(Integer.parseInt(iaasNode.getDictionary.get("jailCreationTimeout").toString), Integer.parseInt(iaasNode.getDictionary.get("jailStartTimeout").toString))

  //  var nodeProcess: Process = null

  def startNode (iaasModel: ContainerRoot, childBootstrapModel: ContainerRoot): Boolean = {
    logger.debug("Starting " + nodeName)
    // looking for currently launched jail
    val result = processExecutor.listIpJails(nodeName)
    if (result._1) {
      var newIp = "127.0.0.1"
      // check if the node have a inet address
      val ipOption = KevoreePropertyHelper.getStringNetworkProperty(iaasModel, nodeName, Constants.KEVOREE_PLATFORM_REMOTE_NODE_IP)
      if (ipOption.isDefined) {
        newIp = ipOption.get
      } else {
        // we create a new IP alias according to the existing ones
        newIp = PropertyHelper.lookingForNewIp(result._2, iaasNode.getNetwork, iaasNode.getMask)
      }
      if (processExecutor.addNetworkAlias(iaasNode.getNetworkInterface, newIp)) {
        // looking for the flavors
        var flavor = lookingForFlavors(iaasModel, nodeName)
        if (flavor == null) {
          flavor = iaasNode.getFlavor
        }
        // create the new jail
        if (processExecutor.createJail(flavor, nodeName, newIp, findArchive(nodeName))) {
          var jailPath = processExecutor.findPathForJail(nodeName)
          // find the needed version of Kevoree for the child node
          val version = findVersionForChildNode(nodeName, childBootstrapModel, iaasModel.getNodes.find(n => n.getName == iaasNode.getNodeName).get)
          // install the model on the jail
          val platformFile = iaasNode.getBootStrapperService.resolveKevoreeArtifact("org.kevoree.platform.standalone", "org.kevoree.platform", version)
          KevoreeXmiHelper.save(jailPath + File.separator + "root" + File.separator + "bootstrapmodel.kev", childBootstrapModel)
          if (PropertyHelper.copyFile(platformFile.getAbsolutePath, jailPath + File.separator + "root" + File.separator + "kevoree-runtime.jar")) {
            // specify limitation on jail such as CPU, RAM
            if (JailsConstraintsConfiguration.applyJailConstraints(iaasModel, nodeName)) {
              // configure ssh access
              configureSSHServer(iaasModel, jailPath, newIp)
              // launch the jail
              if (processExecutor.startJail(nodeName)) {
                logger.debug("{} started", nodeName)
                val jailData = processExecutor.findJail(nodeName)
                jailPath = jailData._1
                val jailId = jailData._2
                if (jailId != "-1") {
                  logger.debug("Jail {} is correctly configure, now we try to start the Kevoree Node", nodeName)
                  val logFile = System.getProperty("java.io.tmpdir") + File.separator + nodeName + ".log"
                  outFile = new File(logFile + ".out")
                  errFile = new File(logFile + ".err")
                  processExecutor.startKevoreeOnJail(jailId, KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "RAM").getOrElse("N/A"), nodeName, outFile, errFile)
                } else {
                  logger.error("Unable to find the jail {}", nodeName)
                  false
                }
              } else {
                logger.error("Unable to start the jail {}", nodeName)
                false
              }
            } else {
              logger.error("Unable to specify jail limitations on {}", nodeName)
              false
            }
          } else {
            logger.error("Unable to set the model the new jail {}", nodeName)
            false
          }
        } else {
          logger.error("Unable to create a new Jail {}", nodeName)
          false
        }
      } else {
        logger.error("Unable to define a new alias {} with {}", nodeName, newIp)
        false
      }
    } else {
      // if an existing one have the same name, then it is not possible to launch this new one (return false)
      logger.error("There already exists a jail with the same name or it is not possible to check this:\n {}", result._2)
      false
    }

  }

  def stopNode (): Boolean = {
    logger.info("stop " + nodeName)
    // looking for the jail that must be at least created
    val oldIP = processExecutor.findJail(nodeName)._3
    if (oldIP != "-1") {
      // stop the jail
      if (processExecutor.stopJail(nodeName)) {
        // delete the jail
        if (processExecutor.deleteJail(nodeName)) {
          // release IP alias to allow next IP select to use this one
          if (!processExecutor.deleteNetworkAlias(iaasNode.getNetworkInterface, oldIP)) {
            logger.warn("unable to release ip alias {} for the network interface {}", oldIP, iaasNode.getNetworkInterface)
          }
          // remove rctl constraint using rctl -r jail:<jailNode>
          if (!JailsConstraintsConfiguration.removeJailConstraints(nodeName)) {
            logger.warn("unable to remove rctl constraints about {}", nodeName)
          }
          true
        } else {
          logger.error("Unable to delete the jail {}", nodeName)
          false
        }
      } else {
        logger.error("Unable to stop the jail {}", nodeName)
        false
      }
    } else {
      // if there is no jail corresponding to the nodeName then it is not possible to stop and delete it
      logger.error("Unable to find the corresponding jail {}", nodeName)
      false
    }
  }

  private def lookingForFlavors (iaasModel: ContainerRoot, nodeName: String): String = {
    logger.debug("looking for specific flavor")
    val flavorsOption = KevoreePropertyHelper.getStringPropertyForNode(iaasModel, nodeName, "flavor")
    if (flavorsOption.isDefined) {
      flavorsOption.get
    } else {
      null
    }
  }

  private def findArchive (nodName: String): Option[String] = {
    // TODO
    //val archivesURL = iaasNode.getArchives


    None
  }
}

