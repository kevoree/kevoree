package org.kevoree.library.arduinoNodeType

import utils.ExecutableFinder
import scala.collection.JavaConversions._

/**
 * User: ffouquet
 * Date: 09/06/11
 * Time: 17:26
 */

object ArduinoToolChainExecutables {

  val osxCROSSPATH = "/usr/local/CrossPack-AVR/bin"
  val arduinoRelativePath = "/hardware/tools/avr/bin"

  def getArduinoBin: String = {
    System.getProperty("arduino.home") + arduinoRelativePath
  }

  def getAVRDUDE(): String = {
    ExecutableFinder.getAbsolutePath("avrdude", List(getArduinoBin, osxCROSSPATH)) + getSuffixe
  }

  def getAVR_AR(): String = {
    ExecutableFinder.getAbsolutePath("avr-ar", List(getArduinoBin, osxCROSSPATH)) + getSuffixe
  }

  def getAVR_GCC(): String = {
    ExecutableFinder.getAbsolutePath("avr-gcc", List(getArduinoBin, osxCROSSPATH)) + getSuffixe
  }

  def getAVR_GPP(): String = {
    ExecutableFinder.getAbsolutePath("avr-g++", List(getArduinoBin, osxCROSSPATH)) + getSuffixe
  }

  def getAVR_OBJCOPY(): String = {
    ExecutableFinder.getAbsolutePath("avr-objcopy", List(getArduinoBin, osxCROSSPATH)) + getSuffixe
  }



  def getSuffixe = {
    if (System.getProperty("os.name").toLowerCase().contains("win")) {
      ".exe"
    } else {
      ""
    }
  }

}