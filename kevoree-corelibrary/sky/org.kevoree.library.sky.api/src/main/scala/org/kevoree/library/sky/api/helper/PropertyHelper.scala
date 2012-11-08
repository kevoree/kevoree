package org.kevoree.library.sky.api.helper

import java.io._
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 10:37
 */

object PropertyHelper {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def copyFile(inputFile: String, outputFile: String): Boolean = {
    if (new File(inputFile).exists()) {
      try {
        if (new File(outputFile).exists()) {
          new File(outputFile).delete()
        }
        val reader = new DataInputStream(new FileInputStream(new File(inputFile)))
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
        case _@e => logger.error("Unable to copy {} on {}", Array[AnyRef](inputFile, outputFile), e); false
      }
    } else {
      logger.debug("Unable to find {}", inputFile)
      false
    }
  }


}
