package org.kevoree.tools.annotation.generator

import com.sun.mirror.apt.Filer
import org.kevoree.framework.KevoreeGeneratorHelper
import org.kevoree.{NodeType, ContainerRoot}
import java.io.{PrintWriter, File}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 21/09/11
 * Time: 15:20
 */

object KevoreeAdaptationPrimitiveTypeGenerator {

  def generate (root: ContainerRoot, filer: Filer, nt: NodeType, targetNodeType: String) {
    val nodeTypePackage = KevoreeGeneratorHelper.getTypeDefinitionGeneratedPackage(nt, targetNodeType)

    val wrapper = filer.createTextFile(com.sun.mirror.apt.Filer.Location.SOURCE_TREE, "", new
        File(nodeTypePackage.replace(".", "/") + "/" + nt.getName + "_aspect.scala"), "UTF-8");

    wrapper.append("package " + nodeTypePackage + "\n");

    wrapper append ("trait " + nt.getName + "_aspect {\n")

    wrapper append ("public def getPrimitive(primitive : AdaptationPrimitive) : PrimitiveCommand = {\n")
    wrapper append ("primitive.getPrimitiveType().getName match {\n")

    if (AdaptationPrimitiveMapping.getMappings(nt).size > 0) {
      val mappings = AdaptationPrimitiveMapping.getMappings(nt)
      mappings.keySet.foreach {
        name => addCase(name, mappings(name), wrapper)
      }
    }

    wrapper append ("case _ => null\n")
    wrapper append ("}")

    AdaptationPrimitiveMapping.clear()

  }

  private def addCase (name: String, className: String, wrapper: PrintWriter) {
    wrapper append ("case " + name + " => {\n")
    wrapper append ("val command = new " + className + "()\n")
    wrapper append ("command.setRef(primitive.getRef())\n")
    wrapper append ("command.setTargetNodeName(primitive.getTargetNode().getName())\n")
    wrapper append ("}\n")
  }

}