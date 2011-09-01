package org.kevoree.library.arduinoNodeType

import java.io.File
import org.slf4j.{LoggerFactory, Logger}

/**
 * User: ffouquet
 * Date: 18/06/11
 * Time: 11:13
 */

object FileHelper {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def cleanFolder(f: java.io.File) {
    if (f.exists()) {
      val children = f.list
      for (i <- 0 until children.length) {
        val subF = new java.io.File(f + java.io.File.separator + children(i))
        if (subF.isDirectory) {
          cleanFolder(subF)
        } else {
          subF.delete
        }
      }
      f.delete()
    } else {
      //  log.debug("Cleaning : folder : {} ,not exist",f.getName())
    }
  }
  def createAndCleanDirectory(f : File){
    if(f.exists()){
      logger.debug("Clean Folder => "+f.getAbsolutePath);
      cleanFolder(f)
    }
    f.mkdir()

  }


}