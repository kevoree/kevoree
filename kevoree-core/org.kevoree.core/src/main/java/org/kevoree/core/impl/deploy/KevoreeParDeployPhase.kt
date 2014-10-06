package org.kevoree.core.impl.deploy

import org.kevoree.api.PrimitiveCommand
import java.util.ArrayList
import java.util.concurrent.Callable
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
 * Time: 09:24
 */

class KevoreeParDeployPhase(val originCore: KevoreeCoreBean) : KevoreeDeployPhase {
    var primitives: MutableList<PrimitiveCommand> = ArrayList<PrimitiveCommand>()
    var maxTimeout: Long = 30000
    fun setMaxTime(mt: Long) {
        maxTimeout = Math.max(maxTimeout, mt)
    }
    override var sucessor: KevoreeDeployPhase? = null
    inner class Worker(val primitive: PrimitiveCommand) : Callable<Boolean> {
        override fun call(): Boolean {
            try {
                var result = primitive.execute()
                if (!result) {
                    if (originCore.isAnyTelemetryListener()) {
                        originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Cmd:["+primitive.toString()+"]", null)
                    }
                    Log.error("Error while executing primitive command {} ", primitive)
                }
                return result
            } catch(e: Throwable) {
                if (originCore.isAnyTelemetryListener()) {
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Cmd:["+primitive.toString()+"]", e)
                }
                Log.error("Exception while executing primitive command {} ", e, primitive)
                e.printStackTrace()
                return false
            }
        }
    }

    fun executeAllWorker(ps: List<PrimitiveCommand>, timeout: Long): Boolean {
        return if (ps.isEmpty()) {
            true
        } else {
            val pool = java.util.concurrent.Executors.newCachedThreadPool(WorkerThreadFactory(System.currentTimeMillis().toString()))
            val workers = ArrayList<Worker>()
            for (primitive in ps) {
                workers.add(Worker(primitive))
            }
            try {
                Log.trace("Timeout = {}", timeout)
                val futures = pool.invokeAll(workers, timeout, TimeUnit.MILLISECONDS)
                futures.all { f ->
                    f.isDone() && ( f.get() as Boolean )
                }
            } catch (e: Exception) {
                false
            } finally {
                pool.shutdownNow()
            }
        }
    }

    override fun populate(cmd: PrimitiveCommand) {
        primitives.add(cmd)
        rollbackPerformed = false
    }

    override fun runPhase(): Boolean {
        if (primitives.size == 0) {
            Log.trace("Empty phase !!!")
            return true
        }
        val watchdogTimeout = System.getProperty("node.update.timeout")
        var watchDogTimeoutInt = maxTimeout
        if (watchdogTimeout != null) {
            try {
                watchDogTimeoutInt = Math.max(watchDogTimeoutInt, Integer.parseInt(watchdogTimeout.toString()).toLong())
            } catch (e: Exception) {
                if (originCore.isAnyTelemetryListener()) {
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR,"Invalid value for node.update.timeout system property (must be an integer)!", e)
                }
                Log.warn("Invalid value for node.update.timeout system property (must be an integer)!")
            }
        }
        return executeAllWorker(primitives, watchDogTimeoutInt)
    }

    var rollbackPerformed = false

    override fun rollBack() {
        Log.trace("Rollback phase")
        if (sucessor != null) {
            Log.trace("Rollback sucessor first")
            sucessor?.rollBack()
        }
        if (!rollbackPerformed) {
            // SEQUENCIAL ROOLBACK
            for (c in primitives.reverse()) {
                try {
                    Log.trace("Undo adaptation command {} ", c.javaClass.getName())
                    c.undo()
                } catch (e: Exception) {
                    if (originCore.isAnyTelemetryListener()) {
                        originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR,"Exception during rollback", e)
                    }
                    Log.warn("Exception during rollback", e)
                }
            }
            rollbackPerformed = true
        }
    }
}