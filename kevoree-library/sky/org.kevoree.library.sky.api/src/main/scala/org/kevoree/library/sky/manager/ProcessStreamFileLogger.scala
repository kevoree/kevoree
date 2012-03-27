package org.kevoree.library.sky.manager

import java.io._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/03/12
 * Time: 10:05
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class ProcessStreamFileLogger (inputStream: InputStream, file: File) extends Runnable {
  override def run () {
    try {
      if (!file.exists()) {
        file.createNewFile()
      }
      val outputStream = new FileWriter(file)
      val readerIn = new BufferedReader(new InputStreamReader(inputStream))
      var lineIn = readerIn.readLine()
      while (lineIn != null) {
        //            logger.info(lineIn)
        outputStream.write(lineIn + "\n")
        outputStream.flush()
        lineIn = readerIn.readLine()
      }
    } catch {
      case _@e => e.printStackTrace()
    }
  }
}
