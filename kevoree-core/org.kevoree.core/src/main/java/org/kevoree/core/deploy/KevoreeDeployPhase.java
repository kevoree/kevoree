package org.kevoree.core.deploy;

import java.util.ArrayList;
import java.util.List;

import org.kevoree.api.PrimitiveCommand;
import org.kevoree.core.KevoreeCoreBean;

public abstract class KevoreeDeployPhase {

	public KevoreeDeployPhase successor = null;
	protected KevoreeCoreBean originCore;
	protected List<PrimitiveCommand> primitives = new ArrayList<PrimitiveCommand>();
	protected long maxTimeout = 30000l;
	protected boolean rollbackPerformed = false;
	
	public KevoreeDeployPhase(KevoreeCoreBean core) {
		this.originCore = core;
	}
	
    public void setMaxTime(long timeout) {
    	maxTimeout = Math.max(maxTimeout, timeout);
    }

    public void populate(PrimitiveCommand cmd) {
    	primitives.add(cmd);
    	rollbackPerformed = true;
    }
	
	public abstract boolean execute();
	public abstract void rollback();	
}
