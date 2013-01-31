package org.kevoree.library.arduinoNodeType.generator

import org.kevoree.TypeDefinition
import org.kevoree.tools.arduino.framework.RawTypeHelper
import scala.collection.JavaConversions._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 18/01/12
 * Time: 19:33
 * To change this template use File | Settings | File Templates.
 */

trait KevoreeInstanceGenerator extends KevoreeCAbstractGenerator {


  def generateDic(td : TypeDefinition){
    //GENERATE DICTIONARY VALUES POINTERS
    if (td.getDictionaryType()!=null) {
      td.getDictionaryType.getAttributes.foreach {
        attribute =>

          this match {
            case _ if (attribute.getDatatype.startsWith("enum=")) => {
              val enumValues: String = attribute.getDatatype.replaceFirst("enum=", "")
              var maxLenght: Int = 0
              enumValues.split(",").foreach {
                value => maxLenght = scala.math.max(maxLenght, value.size)
              }
              context b "char " + attribute.getName + "[" + (maxLenght + 1) + "];"
            }
            case _ if (attribute.getDatatype.startsWith("raw=")) => {
              val rawType: String = attribute.getDatatype.replaceFirst("raw=", "")

              if(RawTypeHelper.isArduinoTypeArray(rawType) ==true)
              {
                // is an array  type
                context b (RawTypeHelper.getArduinoTypeArray(rawType,attribute.getName))
              }
              else
              {
                 // is a simple type
                context b (RawTypeHelper.getArduinoType(rawType)+" "+attribute.getName+";")
              }

            }
            case _ => {
              context b "char " + attribute.getName + "[MAX_UNTYPED_DICTIONARY];"
            }
          }
      }
    }
  }

}