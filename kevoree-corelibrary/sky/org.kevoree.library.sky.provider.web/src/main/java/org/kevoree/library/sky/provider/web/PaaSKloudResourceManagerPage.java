package org.kevoree.library.sky.provider.web;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 15:54
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentType
public class PaaSKloudResourceManagerPage  extends KloudResourceManagerPage {

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
}
