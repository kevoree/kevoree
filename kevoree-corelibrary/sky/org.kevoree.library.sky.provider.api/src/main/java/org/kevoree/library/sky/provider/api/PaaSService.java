package org.kevoree.library.sky.provider.api;

import org.kevoree.ContainerRoot;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 09/10/12
 * Time: 18:20
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface PaaSService {
	// FIXME id parameter must be deleted when filtered channels can be used

	public void add(String id, ContainerRoot model) throws SubmissionException;

	public void remove(String id, ContainerRoot model) throws SubmissionException;

	public void merge(String id, ContainerRoot model) throws SubmissionException;

	public ContainerRoot getModel(String id) throws SubmissionException;

}
