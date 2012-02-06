package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.library.frascatiNodeTypes.primitives.AdaptatationPrimitiveFactory.AInstance
import java.io.PrintWriter
import org.kevoree.ContainerRoot
import org.slf4j.LoggerFactory
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.KevoreeDeployManager

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 15:32
 */

case class FrascatiAddInstance(adaptationPrimitive: AdaptationPrimitive,nodeName: String, bs: org.kevoree.api.Bootstraper) extends AInstance {

  val logger = LoggerFactory.getLogger(this.getClass)

  override def execute(): Boolean = {
    println("pass par la AddInstance")

    if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.ComponentInstance]) {
      val c_instance = adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance]
      val node = c_instance.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == nodeName).get
      val deployUnit = c_instance.getTypeDefinition.foundRelevantDeployUnit(node)
      if (c_instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean.endsWith(".composite")) {
        
        val kcl = bs.getKevoreeClassLoaderHandler.getKevoreeClassLoader(deployUnit)
        val compositeURL = kcl.getResource(c_instance.getTypeDefinition.getBean)
        AdaptatationPrimitiveFactory.frascati.getComposite(compositeURL.toString)
      } else {
        //TODO
        val s = generateComponent(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName, adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean, adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance])
        val f = java.io.File.createTempFile(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance].getName, "composite")
        val output = new java.io.FileOutputStream(f)
        val writer = new PrintWriter(output)
        writer.print(s);
        writer.flush()
        writer.close()
        output.close()
        AdaptatationPrimitiveFactory.frascati.getComposite(f.getAbsolutePath())
      }
      true
    } else {
      logger.error("Bad Mapping")
      false
    }

  }

  override def undo() = {}

}