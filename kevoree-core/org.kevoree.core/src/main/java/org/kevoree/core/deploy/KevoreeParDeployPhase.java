package org.kevoree.core.deploy;

import org.kevoree.api.PrimitiveCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.kevoree.log.Log;
import java.util.concurrent.TimeUnit;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.core.KevoreeCoreBean;

public class KevoreeParDeployPhase extends KevoreeDeployPhase {
	
    public KevoreeParDeployPhase(KevoreeCoreBean core) {
    	super(core);
    }
    
    public boolean executeAllWorker(List<PrimitiveCommand> ps, Long timeout) {
    	if (ps.isEmpty()) {
    		return true;
    	} else {
    		
    		ExecutorService pool = Executors.newCachedThreadPool(new WorkerThreadFactory(String.valueOf(System.currentTimeMillis())));
            List<Worker> workers = new ArrayList<Worker>();
            for (PrimitiveCommand primitive : ps) {
                workers.add(new Worker(primitive));
            }
            try {
                Log.trace("Timeout = {}", timeout);
                List<Future<Boolean>> futures = pool.invokeAll(workers, timeout, TimeUnit.MILLISECONDS);
                for (Future<Boolean> f : futures) {
                	if (f.isDone() && !f.get()) {
                		return false;
                	}
                }
                return true;
            } catch (Exception ignore) {
                return false;
            } finally {
                pool.shutdownNow();
            }
    	}
    }
    
    public boolean execute() {
    	if (primitives.isEmpty()) {
            Log.trace("No primitive command to execute");
            return true;
        }
        String overrideTimeout = System.getProperty("kevoree.timeout");
        long timeout = maxTimeout;
        if (overrideTimeout != null) {
            try {
            	timeout = Long.parseLong(overrideTimeout);
            } catch (NumberFormatException e) {
                if (originCore.isAnyTelemetryListener()) {
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_WARNING, "Invalid System.property value for \"kevoree.timeout\". Must be a Long", e);
                }
                Log.warn("Invalid System.property value for \"kevoree.timeout\". Must be a Long");
            }
        }
        return executeAllWorker(primitives, timeout);
    }

    public void rollback() {
    	Log.trace("Rollback phase");
        if (successor != null) {
            Log.trace("Rollback sucessor first");
            successor.rollback();
        }
        if (!rollbackPerformed) {
        	List<PrimitiveCommand> clonedPrimitives = new ArrayList<PrimitiveCommand>(primitives);
        	Collections.reverse(clonedPrimitives);
            for (PrimitiveCommand c : clonedPrimitives) {
                try {
                    Log.trace("Undo adaptation command {} ", c.getClass().getName());
                    c.undo();
                } catch (Exception e) {
                    if (originCore.isAnyTelemetryListener()) {
                        originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Error while executing primitive command {}", e);
                    }
                    Log.error("Error while executing primitive command {}", c);
                }
            }
            rollbackPerformed = true;
        }	
    }
    
    private class Worker implements Callable<Boolean> {
    	
    	private PrimitiveCommand cmd;
    	
    	public Worker(PrimitiveCommand primitive) {
    		this.cmd = primitive;
    	}
    	
    	@Override
    	public Boolean call() {
    		try {
    			boolean result = cmd.execute();
    			if (!result) {
    				if (originCore.isAnyTelemetryListener()) {
    					originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Cmd:["+cmd.toString()+"]", null);
    				}
    				Log.error("Error while executing primitive command {}", cmd);
    			}
    			return result;
    		} catch (Throwable e) {
                if (originCore.isAnyTelemetryListener()) {
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Cmd:["+cmd.toString()+"]", e);
                }
                Log.error("Exception while executing primitive command {} ", e, cmd);
                e.printStackTrace();
                return false;
    		}
    	}
    }
}