package org.kevoree.core.deploy;

import org.kevoree.api.PrimitiveCommand;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kevoree.log.Log;
import org.kevoree.core.KevoreeCoreBean;
import org.kevoree.api.telemetry.TelemetryEvent;

public class KevoreeSeqDeployPhase extends KevoreeDeployPhase {
    
    public KevoreeSeqDeployPhase(KevoreeCoreBean core) {
    	super(core);
    }
    
    public boolean execute() {
    	if (primitives.isEmpty()) {
    		return true;
    	} else {
    		PrimitiveCommand lastCmd = null;
    		try {
    			boolean result = true;
    			for (PrimitiveCommand cmd : primitives) {
    				lastCmd = cmd;
    				result = cmd.execute();
    				if (!result) {
        				if (originCore.isAnyTelemetryListener()) {
        					originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Cmd:["+cmd.toString()+"]", null);
        				}
        				Log.error("Error while executing primitive command {}", cmd);
        				break;
    				}
    			}
    			return result;
    		} catch (Throwable e) {
                if (originCore.isAnyTelemetryListener()) {
                    originCore.broadcastTelemetry(TelemetryEvent.Type.LOG_ERROR, "Cmd:["+lastCmd.toString()+"]", e);
                }
                Log.error("Exception while executing primitive command {} ", e, lastCmd);
                e.printStackTrace();
                return false;
    		}
    	}
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
}