package org.kevoree.library.sky.provider.web;

import org.kevoree.ContainerRoot;
import org.kevoree.annotation.*;
import org.kevoree.library.sky.provider.api.IaaSManagerService;
import org.kevoree.library.sky.provider.api.SubmissionException;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 16:23
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@DictionaryType({
		@DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/iaas")
})
@Requires({
		@RequiredPort(name = "delegate", type = PortType.SERVICE, className = IaaSManagerService.class, optional = false)
})
@ComponentType
public class IaaSKloudResourceManagerPage extends KloudResourceManagerPage implements IaaSManagerService {

	@Override
	public void startPage () {
		super.startPage();
		generator = new IaaSKloudResourceManagerPageGenerator(this, getPattern(), getNodeName());
	}

	@Override
	public void updatePage () {
		super.updatePage();
		generator = new IaaSKloudResourceManagerPageGenerator(this, getPattern(), getNodeName());
	}

	@Override
	public void add (ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", IaaSManagerService.class).add(model);
	}

	@Override
	public void addToNode (ContainerRoot model, String nodeName) throws SubmissionException {
		getPortByName("delegate", IaaSManagerService.class).addToNode(model, nodeName);
	}

	@Override
	public void remove (ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", IaaSManagerService.class).remove(model);
	}

	@Override
	public void merge (ContainerRoot model) throws SubmissionException {
		getPortByName("delegate", IaaSManagerService.class).merge(model);
	}

	@Override
	public void mergeToNode (ContainerRoot model, String nodeName) throws SubmissionException {
		getPortByName("delegate", IaaSManagerService.class).mergeToNode(model, nodeName);
	}
}
