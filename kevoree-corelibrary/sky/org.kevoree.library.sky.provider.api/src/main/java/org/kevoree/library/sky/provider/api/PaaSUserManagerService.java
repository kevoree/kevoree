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
public interface PaaSUserManagerService {

    public void initialize(ContainerRoot model) throws SubmissionException;

    public void add(ContainerRoot model) throws SubmissionException;

    public void remove(ContainerRoot model) throws SubmissionException;

    public void merge(ContainerRoot model) throws SubmissionException;

    public void release() throws SubmissionException;

    public ContainerRoot getModel() throws SubmissionException;
}
