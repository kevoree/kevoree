package org.kevoree.tools.ui.editor.command

import org.kevoree.tools.ui.editor.KevoreeUIKernel
import org.kevoree.framework.aspects.KevoreeAspects._
import org.kevoree.tools.aether.framework.AetherUtil
import java.util.jar.{JarEntry, JarFile}
import org.kevoree.framework.KevoreeXmiHelper
import org.slf4j.LoggerFactory

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 01/03/12
 * Time: 14:55
 */

class AutoUpdateCommand extends Command {

  var kernel: KevoreeUIKernel = null

  def setKernel(k: KevoreeUIKernel) = kernel = k

  def execute(p: AnyRef) {

    val currentModel = kernel.getModelHandler.getActualModel
    currentModel.getNodes.foreach {
      node =>
        node.getUsedTypeDefinition.foreach {
          typeDef =>
            typeDef.getDeployUnits.foreach {
              du => {
                try {
                  var file = AetherUtil.resolveDeployUnit(du)
                  val jar = new JarFile(file)
                  val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")
                  val newmodel = KevoreeXmiHelper.loadStream(jar.getInputStream(entry))
                  if (newmodel != null) {
                    kernel.getModelHandler.merge(newmodel);
                    LoggerFactory.getLogger(this.getClass).info("AutoMerge => " + du.getUnitName)
                  }
                } catch {
                  case _@e =>
                }
              }

            }
        }


    }


  }

}
