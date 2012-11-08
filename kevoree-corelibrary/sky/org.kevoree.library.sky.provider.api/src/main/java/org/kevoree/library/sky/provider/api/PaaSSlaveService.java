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
public interface PaaSSlaveService {

	public ContainerRoot getModel () throws SubmissionException;

}
