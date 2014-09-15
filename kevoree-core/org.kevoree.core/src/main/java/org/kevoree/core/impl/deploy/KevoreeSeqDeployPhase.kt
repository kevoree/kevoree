package org.kevoree.core.impl.deploy

import org.kevoree.api.PrimitiveCommand
import java.util.concurrent.Callable
import java.util.ArrayList
import org.kevoree.log.Log
import java.util.concurrent.TimeUnit
import org.kevoree.core.impl.KevoreeCoreBean
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 04/12/2013
 * Time: 09:26
 */

class KevoreeSeqDeployPhase(val originCore : KevoreeCoreBean) : KevoreeDeployPhase {
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
            return true
        }
        var lastPrimitive : PrimitiveCommand? = null
        try {
            var result = true
            for(primitive in primitives) {
                lastPrimitive = primitive
                result = primitive.execute()
                if(!result){
                    if(originCore.isAnyTelemetryListener()){
                        originCore.broadcastTelemetry("failed_command","Cmd:["+primitive.toString()+"]","")
                    }
                    //originCore.broadcastTelemetry("warn","Error during execution of "+primitive, e.toString())
                    Log.warn("Error during execution of {}",primitive)
                    break;
                }
            }
            return result
        } catch (e:Throwable){
            if(originCore.isAnyTelemetryListener()){
                try {
                    val boo = ByteArrayOutputStream()
                    val pr = PrintStream(boo)
                    e.printStackTrace(pr)
                    pr.flush()
                    pr.close()
                    originCore.broadcastTelemetry("failed_phase","Cmd:["+lastPrimitive.toString()+"]",String(boo.toByteArray()))
                } catch (e: Throwable){
                   e.printStackTrace()
                }
            }
            e.printStackTrace()
            return false
        }
    }

    var rollbackPerformed = false

    override fun rollBack() {
        Log.trace("Rollback phase")
        if (sucessor != null) {
            Log.trace("Rollback sucessor first")
            sucessor?.rollBack()
        }
        if(!rollbackPerformed){
            // SEQUENCIAL ROOLBACK
            for(c in primitives.reverse()){
                try {
                    Log.trace("Undo adaptation command {} ", c.javaClass.getName())
                    c.undo()
                } catch (e: Exception) {
                    originCore.broadcastTelemetry("error","Exception during rollback", e.toString())
                    //Log.warn("Exception during rollback", e)
                }
            }
            rollbackPerformed = true
        }
    }
}