package org.kevoree.tools.marShell

import interpreter.KevsInterpreterContext
import org.kevoree.ContainerRoot
import org.kevoree.cloner.ModelCloner
import parser.KevsParser
import interpreter.KevsInterpreterAspects._

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 08/11/11
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */

object KevsEngine {

  val modelCloner = new ModelCloner
  val parser = new KevsParser

  def executeScript(script : String,model : ContainerRoot) : Option[ContainerRoot] = {

    parser.parseScript(script) match {
      case Some(s)=> {
        val inputModel = modelCloner.clone(model)
        if(s.interpret(KevsInterpreterContext(inputModel))){
          
        }
      }
      case None => None
    }



    None
  }

}