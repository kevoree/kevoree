package org.kevoree.library.sky.provider.api;

import org.kevoree.ContainerRoot;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/11/12
 * Time: 14:57
 *
 * @author Erwan Daubert
 * @version 1.0
 */
public interface PaaSManagerService {

	public void initialize (String id, ContainerRoot model) throws SubmissionException;

	public void add(String id, ContainerRoot model) throws SubmissionException;

	public void remove(String id, ContainerRoot model) throws SubmissionException;

	public void merge(String id, ContainerRoot model) throws SubmissionException;

	public void release (String id) throws SubmissionException;

	public ContainerRoot getModel(String id) throws SubmissionException;
}
