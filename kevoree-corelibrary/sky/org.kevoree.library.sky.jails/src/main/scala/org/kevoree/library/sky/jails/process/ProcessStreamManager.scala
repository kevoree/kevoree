package org.kevoree.library.sky.jails.process

import java.io.{InputStream, InputStreamReader, BufferedReader}
import util.matching.Regex
import org.slf4j.LoggerFactory


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 13/03/12
 * Time: 11:02
 */

class ProcessStreamManager (resultActor: ResultManagementActor, inputStream: InputStream, outputRegexes: Array[Regex], errorRegexes: Array[Regex], p: Process) extends Runnable {
  private val logger = LoggerFactory.getLogger(this.getClass)

  override def run () {
    val outputBuilder = new StringBuilder
    var errorBuilder = false
    try {
      val reader = new BufferedReader(new InputStreamReader(inputStream))
      var line = reader.readLine()
      while (line != null) {

        outputRegexes.find(regex => {
          val m = regex.pattern.matcher(line)
          m.find()
        }) match {
          case Some(regex) => outputBuilder.append(line + "\n")
          case none =>
        }
        errorRegexes.find(regex => {
          val m = regex.pattern.matcher(line)
          m.find()
        }) match {
          case Some(regex) => errorBuilder = true; outputBuilder.append(line + "\n")
          case none =>
        }
        line = reader.readLine()
      }
    } catch {
      case _@e =>
    }
    logger.debug("waiting for the end of the process")
    val exitValue = p.waitFor()
    logger.debug("Process complete")
    if (errorBuilder || exitValue != 0) {
      resultActor.error(outputBuilder.toString())
      if (exitValue != 0) {
        resultActor.error("The process failed to end")
      }
    } else {
      resultActor.output(outputBuilder.toString())
    }
  }
}

