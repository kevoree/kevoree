package org.kevoree.library.sky.manager

import org.kevoree.framework.KevoreePropertyHelper
import org.slf4j.{LoggerFactory, Logger}
import org.kevoree.{TypeDefinition, ContainerRoot}
import java.io._

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

  def startNode (iaasModel : ContainerRoot,jailBootStrapModel : ContainerRoot): Boolean

  def stopNode (): Boolean

  //def updateNode (modelPath: String): Boolean


  var outFile: File = null

  def getOutFile = outFile

  var errFile: File = null

  def getErrFile = errFile

  /**
   * configure the ssh server
   * @param model
   * @param path
   * @param ip
   */
  def configureSSHServer (model: ContainerRoot, path: String, ip: String) {
    if (ip != null && ip != "") {
      logger.debug("configure ssh server ip")
      replaceStringIntoFile("<ip_address>", ip, path + File.separator + "etc" + File.separator + "ssh" + File.separator + "sshd_config")
    }
  }

  private def isASubType (nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  private def copyStringToFile (data: String, outputFile: String) {
    if (data != null && data != "") {
//      logger.debug("trying to copy \"{}\" to {}", data, outputFile)
      try {
        if (new File(outputFile).exists()) {
          new File(outputFile).delete()
        }
        val writer = new DataOutputStream(new FileOutputStream(new File(outputFile)))

        writer.write(data.getBytes)
        writer.flush()
        writer.close()
//        logger.debug("copying \"{}\" into {} uis done", data, outputFile)
      } catch {
        case _@e => logger.error("Unable to copy \"{}\" on {}", Array[AnyRef](data, outputFile), e)
      }
    }
  }

  private def replaceStringIntoFile (dataToReplace: String, newData: String, file: String) {
    if (dataToReplace != null && dataToReplace != "" && newData != null && newData != "") {
//      logger.debug("trying to replace \"{}\" by \"{}\" into {}", Array[AnyRef](dataToReplace, newData, file))
      if (new File(file).exists()) {
        try {
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
          stringBuilder.replace(stringBuilder.indexOf(dataToReplace), stringBuilder.indexOf(dataToReplace) + dataToReplace.length(), newData)

          copyStringToFile(stringBuilder.toString(), file)
//          logger.debug("replacing \"{}\" by \"{}\" into {} is done", Array[AnyRef](dataToReplace, newData, file))
        } catch {
          case _@e => logger.error("Unable to replace a string", e)
        }
      } else {
        logger.debug("The file {} doesn't exist, nothing can be replace.", file)
      }
    }
  }
}

