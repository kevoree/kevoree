package org.kevoree.library.sky.provider.web;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;
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
@DictionaryType({
		@DictionaryAttribute(name = "urlpattern", optional = true, defaultValue = "/{login}/")
})
public class PaaSKloudUserResourceManagerPage extends KloudResourceManagerPage {

	@Override
	public void startPage () {
		super.startPage();
		generator = new PaaSKloudUserResourceManagerPageGenerator(this, getPattern());
	}

	@Override
	public void updatePage () {
		super.updatePage();
		generator = new PaaSKloudUserResourceManagerPageGenerator(this, getPattern());
	}
}
