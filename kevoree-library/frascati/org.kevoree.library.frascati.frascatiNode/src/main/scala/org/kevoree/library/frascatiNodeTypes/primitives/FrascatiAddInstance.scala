package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.api.PrimitiveCommand
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoreeAdaptation.AdaptationPrimitive
import org.kevoree.ContainerRoot
import org.ow2.frascati.FraSCAti
import org.slf4j.LoggerFactory
import org.objectweb.fractal.api.Component
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeDeployManager, KevoreeMapping}
import org.kevoree.library.frascatiNodeTypes.{FrascatiNode, FrascatiClassLoaderWrapper}
import org.kevoree.kcl.KevoreeJarClassLoader


/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 15:32
 */

case class FrascatiAddInstance(adaptationPrimitive: AdaptationPrimitive, frascati: FraSCAti, nodeName: String, bs: org.kevoree.api.Bootstraper) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)

  override def execute(): Boolean = {
    if (adaptationPrimitive.getRef.isInstanceOf[org.kevoree.ComponentInstance]) {
      val c_instance = adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance]
      val node = c_instance.getTypeDefinition.eContainer.asInstanceOf[ContainerRoot].getNodes.find(n => n.getName == nodeName).get
      val deployUnit = c_instance.getTypeDefinition.foundRelevantDeployUnit(node)
      if (c_instance.getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean.endsWith(".composite")) {
        val kcl = bs.getKevoreeClassLoaderHandler.getKevoreeClassLoader(deployUnit)
        val compositeURL = kcl.getResource(c_instance.getTypeDefinition.getBean)

        //  println(compositeURL+"-"+compositeURL.getPath+"-"+compositeURL.getFile)


        // new Thread(){
        //    override def run() {
        

        
        val component: Component = frascati.getComposite(compositeURL.toString, new FrascatiClassLoaderWrapper(classOf[FrascatiNode].getClassLoader.asInstanceOf[KevoreeJarClassLoader]))
        KevoreeDeployManager.addMapping(KevoreeMapping(c_instance.getName, c_instance.getClass.getName, component))
        //   }
        // }.start()


      } else {

        /*
        val s = ScaGenerator.generateComponent(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getName, adaptationPrimitive.getRef.asInstanceOf[org.kevoree.Instance].getTypeDefinition.asInstanceOf[org.kevoree.ComponentType].getBean, adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance])
        val f = java.io.File.createTempFile(adaptationPrimitive.getRef.asInstanceOf[org.kevoree.ComponentInstance].getName, "composite")
        val output = new java.io.FileOutputStream(f)
        val writer = new PrintWriter(output)
        writer.print(s);
        writer.flush()
        writer.close()
        output.close()
        frascati.getComposite(f.getAbsolutePath)
        */


      }
      true
    } else {
      logger.error("Bad Mapping")
      false
    }

  }

  def undo() {}

}