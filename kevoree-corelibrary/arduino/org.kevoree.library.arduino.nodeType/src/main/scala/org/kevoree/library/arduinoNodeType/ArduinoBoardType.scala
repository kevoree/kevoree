package org.kevoree.library.arduinoNodeType

/**
 * User: ffouquet
 * Date: 27/06/11
 * Time: 17:19
 */

object ArduinoBoardType extends Enumeration {
  type ArduinoBoardType = Value
  val atmega328, atmega1280, uno, mega2560 = Value

  def getFromTypeName(s: String): ArduinoBoardType = {
    s match {
      case "atmega328" => atmega328
      case "mega1280" => atmega1280
      case "uno" => uno
      case "mega2560" => mega2560
      case _ => atmega328
    }
  }

}