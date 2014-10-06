package org.kevoree.core.impl.deploy

import org.kevoree.api.PrimitiveCommand
import java.util.concurrent.Callable
import java.util.ArrayList
import org.kevoree.log.Log
import java.util.concurrent.TimeUnit
import org.kevoree.core.impl.KevoreeCoreBean
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import org.kevoree.api.telemetry.TelemetryEvent

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
                        originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR,"Cmd:["+primitive.toString()+"]",null)
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
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR,"Cmd:["+lastPrimitive.toString()+"]",e)
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
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR,"Exception during rollback", e)
                    //Log.warn("Exception during rollback", e)
                }
            }
            rollbackPerformed = true
        }
    }
}