package org.kevoree.library.rest;

import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.framework.KevoreeXmiHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 09:56
 *
 * @author Erwan Daubert
 * @version 1.0
 */

@DictionaryType({
		@DictionaryAttribute(name = "lock_timeout", defaultValue = "1000", optional = false)
})
@GroupType
@Library(name = "JavaSE")
public class RestConsensusGroup extends RestGroup {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public boolean lock () {
		// TODO lock the node
		return false;
	}

	public byte[] getHashedModel () {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			KevoreeXmiHelper.saveStream(stream, this.getModelService().getLastModel());
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return md.digest(stream.toByteArray());
		} catch (NoSuchAlgorithmException e1) {
			logger.debug("Unable to build a Hash code of the model", e1);
			return new byte[0];
		}
	}

	@Override
	public boolean preUpdate () {
		// TODO try to lock all nodes

		return false;
	}

	@Override
	public void triggerModelUpdate () {
		// TODO check if all other nodes have done their update
			// if they have not done their update you need to rollback
		// TODO unlock all nodes
	}

	@Override
	public RootService getRootService (String id) {
		return new ConsensusRootService(id, this);
	}
}
