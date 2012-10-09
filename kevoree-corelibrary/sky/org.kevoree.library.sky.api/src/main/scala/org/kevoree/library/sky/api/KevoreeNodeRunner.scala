package org.kevoree.library.sky.api

import nodeType.AbstractIaaSNode
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.{ContainerNode, KevoreeFactory, TypeDefinition, ContainerRoot}
import java.io._
import util.matching.Regex
import java.util.concurrent.Callable

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


/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 20/09/11
 * Time: 11:46
 *
 * @author Erwan Daubert
 * @version 1.0
 */
abstract class KevoreeNodeRunner (var nodeName: String) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[KevoreeNodeRunner])

  def startNode (iaasModel: ContainerRoot, jailBootStrapModel: ContainerRoot): Boolean

  def stopNode (): Boolean

  //def updateNode (modelPath: String): Boolean


  var outFile: File = null

  def getOutFile = outFile

  var errFile: File = null

  def getErrFile = errFile

  /**
   * configure the ssh server
   * @param path
   * @param ip
   */
  def configureSSHServer (path: String, ip: String) {
    if (ip != null && ip != "") {
      logger.debug("configure ssh server ip")
      try {
        replaceStringIntoFile("<ip_address>", ip, path + File.separator + "etc" + File.separator + "ssh" + File.separator + "sshd_config")
      } catch {
        case _@e =>
          logger.debug("Unable to configure ssh server", e)
      }
    }
  }

  private def isASubType (nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  @throws(classOf[Exception])
  private def copyStringToFile (data: String, outputFile: String) {
    if (data != null && data != "") {
      if (new File(outputFile).exists()) {
        new File(outputFile).delete()
      }
      val writer = new DataOutputStream(new FileOutputStream(new File(outputFile)))

      writer.write(data.getBytes)
      writer.flush()
      writer.close()
    }
  }

  @throws(classOf[java.lang.StringIndexOutOfBoundsException])
  private def replaceStringIntoFile (dataToReplace: String, newData: String, file: String) {
    if (dataToReplace != null && dataToReplace != "" && newData != null && newData != "") {
      if (new File(file).exists()) {
        val stringBuilder = new StringBuilder
        val reader = new DataInputStream(new FileInputStream(new File(file)))
        val writer = new ByteArrayOutputStream()

        val bytes = new Array[Byte](2048)
        var length = reader.read(bytes)
        while (length != -1) {
          writer.write(bytes, 0, length)
          length = reader.read(bytes)

        }
        writer.flush()
        writer.close()
        reader.close()
        stringBuilder append new String(writer.toByteArray)
        if (stringBuilder.indexOf(dataToReplace) == -1) {
          logger.debug("Unable to find {} on file {} so replacement cannot be done", dataToReplace, file)
        } else {
          stringBuilder.replace(stringBuilder.indexOf(dataToReplace), stringBuilder.indexOf(dataToReplace) + dataToReplace.length(), newData)

          copyStringToFile(stringBuilder.toString(), file)
        }
      } else {
        logger.debug("The file {} doesn't exist, nothing can be replace.", file)
      }
    }
  }


  def findVersionForChildNode (nodeName: String, model: ContainerRoot, iaasNode: ContainerNode): String = {
    logger.debug("looking for Kevoree version for node {}", nodeName)
    model.getNodes.find(n => n.getName == nodeName) match {
      case None => KevoreeFactory.getVersion
      case Some(node) => {
        logger.debug("looking for deploy unit")
        node.getTypeDefinition.getDeployUnits.find(d => d.getTargetNodeType.get.getName == iaasNode.getTypeDefinition.getName ||
          isASubType(iaasNode.getTypeDefinition, d.getTargetNodeType.get.getName)) match {
          case None => KevoreeFactory.getVersion
          case Some(d) => {
            logger.debug("looking for version of kevoree framework for the found deploy unit")
            d.getRequiredLibs.find(rd => rd.getUnitName == "org.kevoree" && rd.getGroupName == "org.kevoree.framework") match {
              case None => KevoreeFactory.getVersion
              case Some(rd) => rd.getVersion
            }
          }
        }

      }
    }
  }
}

