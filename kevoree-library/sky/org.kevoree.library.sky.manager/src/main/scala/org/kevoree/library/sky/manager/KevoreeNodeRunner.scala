package org.kevoree.library.sky.manager

import org.kevoree.framework.KevoreePropertyHelper
import org.kevoree.library.javase.ssh.SSHRestGroup
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
abstract class KevoreeNodeRunner (var nodeName: String, bootStrapModel: String) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[KevoreeNodeRunner])

  def startNode (): Boolean

  def stopNode (): Boolean

  def updateNode (modelPath: String): Boolean


  var outFile: File = null

  def getOutFile = outFile

  var errFile: File = null

  def getErrFile = errFile

  def configureSSH (model: ContainerRoot, path : String, ip : String) {
    // copy SSH Public Key if available
    // for each SSHRestGroup, We copy the Public SSH key on jail
    logger.debug("try to copy SSH Public keys ...")
    model.getGroups.filter(group =>
      (group.getTypeDefinition.getName == "SSHRestGroup" || isASubType(group.getTypeDefinition, "SSHRestGroup")) &&
        (group.getSubNodes.find(node => node.getName == nodeName) match {
          case None => false
          case Some(node) => true
        })).foreach {
      group =>
        val keyOption = KevoreePropertyHelper
          .getPropertyForGroup(model, group.getName, SSHRestGroup.SSH_PUBLIC_KEY)
        if (keyOption.isDefined) {
          logger.debug("try to copy SSH Public keys from {} ...", group.getName)
          // check if .ssh is available and create it else
          if (!new File(path + File.separator + "root" + File.separator + ".ssh" + File.separator +
            "authorized_keys").exists()) {
            new File(path + File.separator + "root" + File.separator + ".ssh").mkdirs()
          }
          addStringToFile(keyOption.get.toString,
                           path + File.separator + "root" + File.separator + ".ssh" + File.separator +
                             "authorized_keys")
          // configure sshd: BASIC CONFIGURATION USING TEMPLATE
          copyFileFromStream(this.getClass.getClassLoader.getResourceAsStream("sshd_config"),
                              path + File.separator + "etc" + File.separator + "ssh" + File.separator +
                                "sshd_config")
          replaceStringIntoFile("<ip_address>", ip,
                                 path + File.separator + "etc" + File.separator + "ssh" + File.separator +
                                   "sshd_config")
        }
    }
  }


  private def isASubType (nodeType: TypeDefinition, typeName: String): Boolean = {
    nodeType.getSuperTypes.find(td => td.getName == typeName || isASubType(td, typeName)) match {
      case None => false
      case Some(typeDefinition) => true
    }
  }

  private def copyFileFromStream (inputStream: InputStream, outputFile: String): Boolean = {
    logger.debug("trying to copy a stream into {}", outputFile)
    if (inputStream != null) {
      try {
        if (new File(outputFile).exists()) {
          new File(outputFile).delete()
        }
        val reader = new DataInputStream(inputStream)
        val writer = new DataOutputStream(new FileOutputStream(new File(outputFile)))

        val bytes = new Array[Byte](2048)
        var length = reader.read(bytes)
        while (length != -1) {
          writer.write(bytes, 0, length)
          length = reader.read(bytes)

        }
        writer.flush()
        writer.close()
        reader.close()
        true
      } catch {
        case _@e => logger.error("Unable to copy a stream into {}", outputFile, e); false
      }
    } else {
      logger.debug("The stream is undefined")
      false
    }
  }

  private def addStringToFile (data: String, outputFile: String) {
    logger.debug("trying to add \"{}\" into {}", data, outputFile)
    val stringBuilder = new StringBuilder
    stringBuilder append data + "\n"
    if (new File(outputFile).exists()) {
      val reader = new DataInputStream(new FileInputStream(new File(outputFile)))
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
    }

    copyStringToFile(stringBuilder.toString(), outputFile)
    logger.debug("adding \"{}\" into {} is done", data, outputFile)
  }

  private def copyStringToFile (data: String, outputFile: String) {
    logger.debug("trying to copy \"{}\" to {}", data, outputFile)
    try {
      if (new File(outputFile).exists()) {
        new File(outputFile).delete()
      }
      val writer = new DataOutputStream(new FileOutputStream(new File(outputFile)))

      writer.write(data.getBytes)
      writer.flush()
      writer.close()
      logger.debug("copying \"{}\" into {} uis done", data, outputFile)
    } catch {
      case _@e => logger.error("Unable to copy \"{}\" on {}", Array[AnyRef](data, outputFile), e); false
    }
  }

  private def replaceStringIntoFile (dataToReplace: String, newData: String, file: String) {
    logger.debug("trying to replace \"{}\" by \"{}\" into {}", Array[AnyRef](dataToReplace, newData, file))
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
      stringBuilder
        .replace(stringBuilder.indexOf(dataToReplace), stringBuilder.indexOf(dataToReplace) + dataToReplace.length(),
                  newData)

      copyStringToFile(stringBuilder.toString(), file)
      logger.debug("replacing \"{}\" by \"{}\" into {} is done", Array[AnyRef](dataToReplace, newData, file))
    } else {
      logger.debug("The file {} doesn't exist, anything can be replace.", file)
    }
  }
}

