package org.kevoree.core.impl.deploy

import org.kevoree.api.PrimitiveCommand
import java.util.concurrent.Callable
import java.util.ArrayList
import org.kevoree.log.Log
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/12/2013
 * Time: 09:26
 */

class KevoreeSeqDeployPhase : KevoreeDeployPhase {
    var primitives: MutableList<PrimitiveCommand> = ArrayList<PrimitiveCommand>()
    var maxTimeout: Long = 30000
    fun setMaxTime(mt: Long) {
        maxTimeout = Math.max(maxTimeout, mt)
    }
    override var sucessor: KevoreeDeployPhase? = null

    override fun populate(cmd: PrimitiveCommand) {
        primitives.add(cmd)
        rollbackPerformed = false
    }

    override fun runPhase(): Boolean {
        if (primitives.size == 0) {
            Log.debug("Empty phase !!!")
            return true
        }
        try {
            var result = true
            for(primitive in primitives) {
                result = primitive.execute()
                if(!result){
                    break;
                }
            }
            return result
        } catch (e:Throwable){
            e.printStackTrace()
            return false
        }
    }

    var rollbackPerformed = false

    override fun rollBack() {
        Log.debug("Rollback phase")
        if (sucessor != null) {
            Log.debug("Rollback sucessor first")
            sucessor?.rollBack()
        }
        if(!rollbackPerformed){
            // SEQUENCIAL ROOLBACK
            for(c in primitives.reverse()){
                try {
                    Log.debug("Undo adaptation command {} ", c.javaClass.getName())
                    c.undo()
                } catch (e: Exception) {
                    Log.warn("Exception during rollback", e)
                }
            }
            rollbackPerformed = true
        }
    }
}