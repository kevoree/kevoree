/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.library.arduinoNodeType.generator

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import org.kevoree.tools.arduino.framework.impl.DefaultArduinoGenerator
import org.kevoree.tools.arduino.framework.ArduinoGenerator

trait KevoreeCAbstractGenerator {
  
  private var private_context : GeneratorContext = null
  def context : GeneratorContext = {
    if(private_context == null){private_context = new GeneratorContext}
    private_context
  } 
  
  def initContext = {
    private_context = null
    context
  }
  
  val portCodeMap = new scala.collection.mutable.HashMap[String,Int]()
  val typeCodeMap = new scala.collection.mutable.HashMap[String,Int]()
  val propsCodeMap = new scala.collection.mutable.HashMap[String,Int]()
}
class GeneratorContext {
  
  val header = new StringBuilder
  val body = new StringBuilder
  def h (content : String) = { header.append(content);header.append("\n");  }
  def b (content : String) = { body.append(content);body.append("\n");  }

  val gen  = new DefaultArduinoGenerator
  def getGenerator : ArduinoGenerator  = gen
  
  def toFile(outputDir:String,nodeName:String)={
    val finalResult = 
      header.toString + 
    "\n" + 
    body.toString +
    "\n"
    val writer = new PrintWriter( new BufferedWriter(new FileWriter(outputDir+"/arduinoGenerated"+nodeName+".pde",false)));
    writer.println(finalResult.toString)
    writer.println("")
    writer.close()
  }
  
}