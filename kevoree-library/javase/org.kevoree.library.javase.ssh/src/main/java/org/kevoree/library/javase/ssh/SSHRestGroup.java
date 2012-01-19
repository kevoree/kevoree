package org.kevoree.library.javase.ssh;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.rest.RestGroup;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 17/01/12
 * Time: 14:50
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@DictionaryType({
		@DictionaryAttribute(name = SSHRestGroup.SSH_PUBLIC_KEY, optional = false)
})
@GroupType
@Library(name = "JavaSE")
public class SSHRestGroup extends RestGroup {
	public static final String SSH_PUBLIC_KEY = "SSH Public Key";
	// TODO ssh on the node then access the rest group using localhost..

	@Override
	public ContainerRoot pull (String targetNodeName) {
		return super.pull(targetNodeName);
	}

	@Override
	public void push (ContainerRoot model, String targetNodeName) {
		super.push(model, targetNodeName);
	}
}
