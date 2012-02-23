package org.kevoree.library.rest;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreeElementHelper;
import org.kevoree.library.rest.consensus.ConsensusClient;
import org.kevoree.library.rest.consensus.ConsensusRootService;
import org.kevoree.library.rest.consensus.LockManager;
import org.kevoree.library.rest.consensus.PullConsensusActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 09:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@DictionaryType({
		@DictionaryAttribute(name = "lock_timeout", defaultValue = "1000", dataType = Long.class, optional = false),
		@DictionaryAttribute(name = "lock_percent", defaultValue = "51", dataType = Integer.class, optional = false),
		@DictionaryAttribute(name = "pull_interval", defaultValue = "10000", dataType = Long.class, optional = false)
})
@GroupType
@Library(name = "JavaSE")
public class RestConsensusGroup extends RestGroup {

	// TODO phase d'init
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private LockManager lockManager;
	private PullConsensusActor pullConsensus;
	private int lockPercent;
	private ConsensusClient consensusClient;
	private long hash;
	private long futureHash;
	private Random r;

	public long getHash () {
		return hash;
	}

	public void setHash (long hash) {
		this.hash = hash;
	}

	@Override
	public void startRestGroup () {
		super.startRestGroup();
		r = new Random();
		String lockTimeoutString = this.getDictionary().get("lock_timeout").toString();
		long lockTimeout = 1000;
		try {
			lockTimeout = Long.parseLong(lockTimeoutString);
		} catch (NumberFormatException e) {
			logger.debug("Unable to parse <lock_timeout> attribute. Please check this value.");
		}
		String pullIntervalOption = this.getDictionary().get("pull_interval").toString();
		long pullInterval = 10000;
		try {
			pullInterval = Long.parseLong(pullIntervalOption);
		} catch (NumberFormatException e) {
			logger.debug("Unable to parse <pull_interval> attribute. Please check this value.");
		}

		lockManager = new LockManager(lockTimeout, this, r);
		consensusClient = new ConsensusClient(this.getName(), lockTimeout);
		consensusClient.initialize();
		lockManager.start();
		pullConsensus = new PullConsensusActor(pullInterval, this, consensusClient);
	}

	@Override
	public void stopRestGroup () {
		super.stopRestGroup();
		lockManager.stop();
		pullConsensus.stop();
		consensusClient.kill();
	}

	public boolean lock (long remoteHash) {
		logger.debug("Asking for locking the node");
		if (hash == 0 || hash == remoteHash) {
			if (!lockManager.isLock() && lockManager.lock()) {
				logger.debug("the node is now locked");
				hash = remoteHash;
			} else {
				logger.debug("Unable to lock the node. Maybe a previous lock is always active");
			}
		}
		return lockManager.isLock();
	}

	public long unlock (long newHash) {
		if (lockManager.unlock()) {
			logger.debug("Update is done => hash is incremented");
			hash = newHash;
		} /*else {
			logger.debug("Update fails");
		}*/
		return hash;
	}

	@Override
	public boolean triggerPreUpdate (ContainerRoot currentModel, ContainerRoot futureModel) {
		logger.debug("Starting consensus about a new update on {}", this.getNodeName());
		Group g = KevoreeElementHelper.getGroupElement(this.getName(), currentModel).get();

		long tmpHash = hash;
		if (tmpHash == 0) {
			tmpHash = r.nextLong();
		}

		// try to lock all nodes
		int lockConsensus = consensusClient.acquireRemoteLocks(g, this.getNodeName(), currentModel, tmpHash, futureModel);
		// check if at least <lock_percent> nodes are locked
		if (lockConsensus >= lockPercent) {
			// initialize hash to compute consensus
			if (hash == 0) {
				logger.debug("Initialize random hash");
				hash = tmpHash;
			}
			logger.debug("Lock is acquired on {} nodes", lockConsensus);
			// send model to propose the update
			consensusClient.sendModel(g, this.getNodeName(), currentModel, futureModel);
			logger.debug("Model has been sent");
			return true;
		} else {
			logger.warn("Unable to acquire global lock because only {} nodes have accepted the lock. Update cannot be done!", lockConsensus);
			// unable to obtain a consensus to apply a new model, must unlock all the remote nodes
			consensusClient.unlock(g, this.getNodeName(), currentModel, hash);
			return false;
		}
	}

	@Override
	public void triggerModelUpdate () {
		logger.debug("Ending consensus...");
		// compute the minimum number of node we need to get a consensus
		if (lockPercent == 0) {
			String lockPercentString = this.getDictionary().get("lock_percent").toString();
			lockPercent = 51;
			try {
				lockPercent = Integer.parseInt(lockPercentString);
			} catch (NumberFormatException e) {
				logger.debug("Unable to parse <lock_percent> attribute. Please check this value.");
			}
			// convert the lockPercent into a number of nodes
			lockPercent = ((int) ((lockPercent * this.getModelElement().getSubNodesForJ().size()) / 100f + 0.5));
			logger.debug("Define the minimum number of node to get a consensus to {}", lockPercent);
		}
		if (hash != 0) {
			hash = r.nextLong();
			// unlock all nodes
			logger.debug("Ask to all nodes to unlock");
			consensusClient.unlock(this.getModelElement(), this.getNodeName(), this.getModelService().getLastModel(), hash);

		}
	}

	@Override
	public RootService getRootService (String id) {
		return new ConsensusRootService(id, this);
	}

	@Override
	public boolean updateModel (ContainerRoot model, String sender) {
		logger.debug("Try to update local node with a new model coming from {}", sender);
		if (lockManager.isLock()) {
			logger.debug("Kevoree core is locked so we need to use the lock callback interface");
			lockManager.update(model);
			return true;
		}
		return super.updateModel(model, sender);
	}
}
