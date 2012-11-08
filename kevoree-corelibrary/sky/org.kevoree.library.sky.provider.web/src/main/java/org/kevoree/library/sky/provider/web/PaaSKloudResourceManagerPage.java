package org.kevoree.library.sky.provider.web;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.sky.provider.api.PaaSManagerService;
import org.kevoree.library.sky.provider.api.SubmissionException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 15:54
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@Requires({
		@RequiredPort(name = "delegate", type = PortType.SERVICE, className = PaaSManagerService.class, optional = false)
})
@ComponentType
public class PaaSKloudResourceManagerPage extends KloudResourceManagerPage implements PaaSManagerService {

	@Override
	public void startPage () {
		super.startPage();
		generator = new PaaSKloudResourceManagerPageGenerator(this, getPattern());
	}

	@Override
	public void updatePage () {
		super.updatePage();
		generator = new PaaSKloudResourceManagerPageGenerator(this, getPattern());
	}

	@Override
	public void initialize (String id, ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", PaaSManagerService.class).initialize(id, model);
	}

	@Override
	public void add (String id, ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", PaaSManagerService.class).add(id, model);
	}

	@Override
	public void remove (String id, ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", PaaSManagerService.class).remove(id, model);
	}

	@Override
	public void merge (String id, ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", PaaSManagerService.class).merge(id, model);
	}

	@Override
	public void release (String id) throws SubmissionException {
		getPortByName("delegate", PaaSManagerService.class).release(id);
	}

	@Override
	public ContainerRoot getModel (String id) throws SubmissionException {
		return getPortByName("delegate", PaaSManagerService.class).getModel(id);
	}
}
