package org.kevoree.library.sky.provider;

import org.kevoree.ContainerRoot;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/10/12
 * Time: 18:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface HostService {

	public boolean deploy(String login, ContainerRoot model, String sshKey) throws SubmissionException;

	public boolean release(String login) throws SubmissionException;
}
