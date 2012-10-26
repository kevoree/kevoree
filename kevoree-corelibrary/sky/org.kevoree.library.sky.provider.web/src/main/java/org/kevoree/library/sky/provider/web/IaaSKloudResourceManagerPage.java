package org.kevoree.library.sky.provider.web;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
import org.kevoree.annotation.Library;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 16:23
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentType
@DictionaryType({
		@DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/nodes")
})
public class IaaSKloudResourceManagerPage extends KloudResourceManagerPage {

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
}
