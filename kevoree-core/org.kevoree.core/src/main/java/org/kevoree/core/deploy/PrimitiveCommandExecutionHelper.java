package org.kevoree.core.deploy;

import org.kevoree.ContainerNode;
import org.kevoree.api.NodeType;
import org.kevoree.api.PrimitiveCommand;
import org.kevoree.log.Log;
import org.kevoree.api.adaptation.AdaptationModel;
import org.kevoree.api.adaptation.AdaptationPrimitive;
import org.kevoree.api.adaptation.Step;
import org.kevoree.api.telemetry.TelemetryEvent;
import org.kevoree.core.KevoreeCoreBean;

public class PrimitiveCommandExecutionHelper {

    public static boolean execute(KevoreeCoreBean core, ContainerNode rootNode, AdaptationModel adaptionModel, NodeType nodeInstance, PrimitiveExecute afterUpdateFunc, PrimitiveExecute preRollBack, PrimitiveExecute postRollback) {
        Step orderedPrimitiveSet = adaptionModel.getOrderedPrimitiveSet();
        if (orderedPrimitiveSet != null) {
        	KevoreeDeployPhase phase = new KevoreeSeqDeployPhase(core);
        	boolean result = PrimitiveCommandExecutionHelper.executeStep(core, rootNode, orderedPrimitiveSet, nodeInstance, phase, preRollBack);
        	if (result) {
        		if (!afterUpdateFunc.exec()) {
        			preRollBack.exec();
        			phase.rollback();
        			postRollback.exec();
        		}
        	} else {
        		postRollback.exec();
        	}
        	return result;
        } else {
        	return afterUpdateFunc.exec();
        }
    }

    private static boolean executeStep(KevoreeCoreBean core, ContainerNode rootNode, Step step, NodeType nodeInstance, KevoreeDeployPhase phase, PrimitiveExecute preRollBack) {
        if (step == null) {
            return true;
        } else {
        	if (core.isAnyTelemetryListener()) {
        		core.broadcastTelemetry(TelemetryEvent.Type.DEPLOYMENT_STEP, step.getAdaptationType().name(), null);
        	}
        	boolean populateResult = PrimitiveCommandExecutionHelper.populate(step, nodeInstance, phase);
        	if (populateResult) {
                boolean phaseResult = phase.execute();
                if (phaseResult) {
                    Step nextStep = step.getNextStep();
                    boolean subResult = false;
                    if (nextStep != null) {
                        phase.successor = new KevoreeSeqDeployPhase(core);
                        subResult = executeStep(core, rootNode, nextStep, nodeInstance, phase.successor, preRollBack);
                    } else {
                        subResult = true;
                    }
                    if (!subResult) {
                        preRollBack.exec();
                        phase.rollback();
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    preRollBack.exec();
                    phase.rollback();
                    return false;
                }
            } else {
                Log.error("Adaptation primitives must all be mapped by a primitive command");
                return false;
            }
        }
    }

    private static boolean populate(Step step, NodeType nodeInstance, KevoreeDeployPhase phase) {
    	for (AdaptationPrimitive adapt : step.getAdaptations()) {
    		PrimitiveCommand cmd = nodeInstance.getPrimitive(adapt);
    		if (cmd != null) {
    			Log.trace("Populate primitive command = {}", cmd);
    			phase.populate(cmd);
    		} else {
    			Log.warn("Unable to find a primitive command for primitive adaptation {}", adapt);
    			return false;
    		}
    	}
    	return true;
    }
}