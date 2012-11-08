package org.kevoree.library.sky.provider.tester;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 03/11/12
 * Time: 18:22
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "Test")
@ComponentType
public class IaaSKloudManagerTester extends AbstractComponentType {

	@Start
	public void start () {
	}

	@Stop
	public void stop () {
	}
}
