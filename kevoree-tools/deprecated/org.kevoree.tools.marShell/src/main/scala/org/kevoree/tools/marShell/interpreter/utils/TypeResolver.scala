package org.kevoree.tools.marShell.interpreter.utils

import org.kevoree.{TypeDefinition, ContainerRoot}

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 25/11/2013
 * Time: 14:15
 */
object TypeResolver {

  import scala.collection.JavaConversions._

  def resolve(model: ContainerRoot, typeName: String): TypeDefinition = {

    //TODO improve it, take last version
    model.getTypeDefinitions.find(td => td.getName == typeName).getOrElse(null)
  }

}
