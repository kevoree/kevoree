
package org.kevoree.tools.aether.framework.min

import org.kevoree.DeployUnit
import org.kevoree.kcl.KevoreeJarClassLoader
import org.kevoree.tools.aether.framework.JCLContextHandler

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 05/02/12
 * Time: 17:50
 */

class MinJCLContextHandler : JCLContextHandler() {

  override fun installDeployUnitNoFileInternals(du: DeployUnit): KevoreeJarClassLoader? {
    val resolvedFile = org.kevoree.tools.aether.framework.min.AetherUtil.resolveDeployUnit(du)
    if (resolvedFile != null) {
      return installDeployUnitInternals(du, resolvedFile)
    } else {
      logger.error("Error while resolving deploy unit " + du.getUnitName())
      return null
    }
  }
}
