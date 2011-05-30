///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package org.kevoree.library.arduinoNodeType
//
//object PortUsage extends Enumeration {
//  type PortUsage = Value
//  val provided, required = Value
//}
//
//object ArduinoMethodHelper {
//
//  import PortUsage._
//  def generateMethodNameFromComponentPort(componentName:String,portName:String,usage : PortUsage):String={
//    var result = new StringBuilder
//    result.append("component_")
//    result.append(componentName)
//    result.append("_")
//    usage match {
//      case PortUsage.provided => result.append("providedPort")
//      case PortUsage.required => result.append("requiredPort")
//    }
//    result.append("_")
//    result.append(portName)
//    result.toString
//  }
//  def generateMethodNameChannelDispatch(channelName :String)={
//    var result = new StringBuilder
//    result.append("channel")
//    result.append("_")
//    result.append(channelName)
//    result.append("_")
//    result.append("dispatch")
//    result.toString
//  }
//  
//}
