package org.kevoree.library.rest;

import org.kevoree.ContainerRoot;
import org.kevoree.Group;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreeElementHelper;
import org.kevoree.library.rest.consensus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

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

	public long getHash () {
		return hash;
	}

	public void setHash (long hash) {
		this.hash = hash;
	}

	@Override
	public void startRestGroup () {
		super.startRestGroup();
		String lockTimeoutString = this.getDictionary().get("lock_timeout").toString();
		long lockTimeout = 1000;
		try {
			lockTimeout = Long.parseLong(lockTimeoutString);
		} catch (NumberFormatException e) {
			logger.debug("Unable to parse <lock_timeout> attribute. Please check this value.");
		}
		lockManager = new LockManager(lockTimeout, this.getModelService());
		String pullIntervalOption = this.getDictionary().get("pull_interval").toString();
		long pullInterval = 10000;
		try {
			pullInterval = Long.parseLong(pullIntervalOption);
		} catch (NumberFormatException e) {
			logger.debug("Unable to parse <pull_interval> attribute. Please check this value.");
		}
		consensusClient = new ConsensusClient(this.getName());
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

	public Tuple2<byte[], byte[]> lock (byte[] remoteCurrentHashModel, byte[] remoteFutureHashModel) {
		logger.debug("Asking for locking the node");
		byte[] currentHashedModel = HashManager.getHashedModel(this.getModelService().getLastModel());
		// check if the current model have the same hash code than the one send by the remote node
		if (HashManager.equals(currentHashedModel, remoteCurrentHashModel)) {
			logger.debug("The node can be locked according to the proposed models");
			// lock the node
			if (lockManager.lock()) {
				logger.debug("the node is now locked");
				// TODO keep the hash of the future model to use it on comparison
				return new Tuple2<byte[], byte[]>(remoteCurrentHashModel, remoteFutureHashModel);
			} else {
				logger.debug("Unable to lock the node. Maybe a previous lock is always active");
				return new Tuple2<byte[], byte[]>(currentHashedModel, currentHashedModel);
			}
		} else {
			logger.debug("The node can not be locked according to the proposed models");
			return new Tuple2<byte[], byte[]>(currentHashedModel, currentHashedModel);
		}
	}

	public Tuple2<byte[], byte[]> hashes () {
		byte[] currentHashedModel = HashManager.getHashedModel(this.getModelService().getLastModel());
		return new Tuple2<byte[], byte[]>(currentHashedModel, currentHashedModel);
	}

	public void unlock () {
		lockManager.unlock();
	}

	@Override
	public boolean triggerPreUpdate (ContainerRoot currentModel, ContainerRoot futureModel) {
		logger.debug("Starting consensus about a new update on {}", this.getNodeName());
		Group g = KevoreeElementHelper.getGroupElement(this.getName(), currentModel).get();
		
		if (hash == 0) {
			hash = System.currentTimeMillis();
			consensusClient.initializeConsensus(g, this.getNodeName(), currentModel, hash);
		}

		// try to lock all nodes
		int lockConsensus = consensusClient.acquireRemoteLocks(g, this.getNodeName(), currentModel, hash, futureModel);
		// check if at least <lock_percent> nodes are locked
		if (lockConsensus >= lockPercent) {
			logger.debug("Lock is acquired on {} nodes", lockConsensus);
			// send model to propose the update
			consensusClient.sendModel(g, this.getNodeName(), currentModel, futureModel);
			logger.debug("Model has been sent");
			return true;
		} else {
			logger.warn("Unable to acquire global lock because only {} nodes have accepted the lock. Update cannot be done!", lockConsensus);
			// unable to obtain a consensus to apply a new model, must unlock all the remote nodes
			consensusClient.unlock(g, this.getNodeName(), currentModel);
			return false;
		}
	}

	@Override
	public void triggerModelUpdate () {
		// compute the minimum number of node we need to get a consensus
		String lockPercentString = this.getDictionary().get("lock_percent").toString();
		lockPercent = 51;
		try {
			lockPercent = Integer.parseInt(lockPercentString);
		} catch (NumberFormatException e) {
			logger.debug("Unable to parse <lock_percent> attribute. Please check this value.");
		}
		// convert the lockPercent into a number of nodes
		lockPercent = ((int) ((lockPercent * this.getModelElement().getSubNodesForJ().size()) / 100f + 0.5));
		System.out.println("\t" + lockPercentString  + "\t" + this.getModelElement().getSubNodesForJ().size() + "\t" + lockPercent);
		// TODO check if all other nodes have done their update by asking for the model and comparing to the local one
		// if they have not done their update you need to rollback
		// unlock all nodes
		consensusClient.unlock(this.getModelElement(), this.getNodeName(), this.getModelService().getLastModel());
	}

	@Override
	public RootService getRootService (String id) {
		System.out.println("ConsensusRestGroup");
		return new ConsensusRootService(id, this);
	}

	@Override
	public boolean updateModel (ContainerRoot model) {
		logger.debug("Try to update local node with a new model");
		if (lockManager.isLock()) {
			logger.debug("Kevoree core is locked so we need to use the lock callback interface");
			lockManager.update(model);
			return true;
		}
		return super.updateModel(model);
	}
}
