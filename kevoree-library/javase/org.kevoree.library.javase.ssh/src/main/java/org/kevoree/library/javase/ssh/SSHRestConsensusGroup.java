package org.kevoree.library.javase.ssh;

import org.kevoree.annotation.GroupType;
import org.kevoree.annotation.Library;
import org.kevoree.library.rest.RestConsensusGroup;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 13/02/12
 * Time: 10:48
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@GroupType
@Library(name = "JavaSE")
public class SSHRestConsensusGroup extends RestConsensusGroup {
	@Override
	public boolean lock () {
		return super.lock();
	}

	@Override
	public boolean preUpdate () {
		return super.preUpdate();
	}
}
