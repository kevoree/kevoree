package org.kevoree.library.frascatiNodeTypes.primitives

import org.kevoree.DeployUnit
import org.kevoree.api.PrimitiveCommand
import org.slf4j.LoggerFactory
import java.util.Random
import org.kevoree.library.defaultNodeTypes.jcl.deploy.context.{KevoreeMapping, KevoreeDeployManager}
import org.kevoree.library.defaultNodeTypes.jcl.deploy.command.CommandHelper
import java.io.{FileInputStream, File}
import org.kevoree.framework.FileNIOHelper
import org.kevoree.kcl.KevoreeJarClassLoader

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/02/12
 * Time: 15:20
 */

case class FrascatiRemoveDeployUnit(du: DeployUnit,bootstrap : org.kevoree.api.Bootstraper, topFrascatiBootLoader: KevoreeJarClassLoader) extends PrimitiveCommand {

  val logger = LoggerFactory.getLogger(this.getClass)
  var lastTempFile: File = _
  var random = new Random

  def undo() {
    if (lastTempFile != null) {
      val newKCL = bootstrap.getKevoreeClassLoaderHandler.installDeployUnit(du, lastTempFile)
      topFrascatiBootLoader.addWeakClassLoader(newKCL)
      KevoreeDeployManager.bundleMapping.filter(bm => bm.ref.isInstanceOf[DeployUnit]).find(bm => CommandHelper.buildKEY(bm.ref.asInstanceOf[DeployUnit]) == CommandHelper.buildKEY(du)) match {
        case Some(bm) =>
        case None => KevoreeDeployManager.addMapping(KevoreeMapping(CommandHelper.buildKEY(du), du.getClass.getName, du))
      }
    }
  }

  //LET THE UNINSTALL
  def execute(): Boolean = {
    try {
      lastTempFile = File.createTempFile(random.nextInt() + "", ".jar")
      val jarStream = new FileInputStream(bootstrap.getKevoreeClassLoaderHandler.getCacheFile(du));
      FileNIOHelper.copyFile(jarStream, lastTempFile)
      jarStream.close()
      val previousDeployUnit = bootstrap.getKevoreeClassLoaderHandler.getKevoreeClassLoader(du)
      topFrascatiBootLoader.cleanupLinks(previousDeployUnit)
      bootstrap.getKevoreeClassLoaderHandler.removeDeployUnitClassLoader(du)
      KevoreeDeployManager.bundleMapping.filter(bm => bm.ref.isInstanceOf[DeployUnit]).foreach(bm => {
        if (CommandHelper.buildKEY(bm.ref.asInstanceOf[DeployUnit]) == CommandHelper.buildKEY(du)) {
          KevoreeDeployManager.removeMapping(bm)
        }
      })
      true
    } catch {
      case _@e => logger.debug("error ", e); false
    }
  }
}