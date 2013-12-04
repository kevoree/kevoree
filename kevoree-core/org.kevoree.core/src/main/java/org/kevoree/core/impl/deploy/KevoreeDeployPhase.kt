package org.kevoree.core.impl.deploy

import org.kevoree.api.PrimitiveCommand

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/12/2013
 * Time: 09:26
 */

trait KevoreeDeployPhase {
    fun rollBack()
    fun runPhase(): Boolean
    fun populate(cmd: PrimitiveCommand)
    var sucessor: KevoreeParDeployPhase?
}
