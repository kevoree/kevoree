package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.DeployUnit
import org.kevoree.api.PrimitiveCommand
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.library.defaultNodeTypes.jcl.deploy.command.CommandHelper
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 15:05
 */

case class FrascatiAddDedployUnit(du: DeployUnit, bs: org.kevoree.api.Bootstraper, topFrascatiBootLoader: KevoreeJarClassLoader) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)

  override def execute(): Boolean = {
    try {
      if (bs.getKevoreeClassLoaderHandler.getKevoreeClassLoader(du) == null) {
        val newKCL = bs.getKevoreeClassLoaderHandler.installDeployUnit(du)

        topFrascatiBootLoader.addSubClassLoader(newKCL)
        //newKCL.addSubClassLoader(topFrascatiBootLoader)

        KevoreeDeployManager.bundleMapping.filter(bm => bm.ref.isInstanceOf[DeployUnit]).find(bm => CommandHelper.buildKEY(bm.ref.asInstanceOf[DeployUnit]) == CommandHelper.buildKEY(du)) match {
          case Some(bm) =>
          case None => KevoreeDeployManager.addMapping(KevoreeMapping(CommandHelper.buildKEY(du), du.getClass.getName, du))
        }
      } 
      true
    } catch {
      case _@e => logger.debug("error ", e); false
    }
  }

  override def undo() {
    val previousKCL = bs.getKevoreeClassLoaderHandler.getKevoreeClassLoader(du)
    if (previousKCL != null) {
      topFrascatiBootLoader.cleanupLinks(previousKCL)
      bs.getKevoreeClassLoaderHandler.removeDeployUnitClassLoader(du)
    }
    KevoreeDeployManager.bundleMapping.filter(bm => bm.ref.isInstanceOf[DeployUnit]).foreach(bm => {
      if (CommandHelper.buildKEY(bm.ref.asInstanceOf[DeployUnit]) == CommandHelper.buildKEY(du)) {
        KevoreeDeployManager.removeMapping(bm)
      }
    })
  }


}
