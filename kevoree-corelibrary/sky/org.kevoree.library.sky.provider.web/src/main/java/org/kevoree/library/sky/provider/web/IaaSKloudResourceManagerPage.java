package org.kevoree.library.sky.provider.web;

import org.kevoree.annotation.ComponentType;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

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
public class IaaSKloudResourceManagerPage extends ParentAbstractPage {

	private IaaSKloudResourceManagerPageGenerator generator;

	@Override
	public void startPage () {
		super.startPage();
		generator = new IaaSKloudResourceManagerPageGenerator(this, getNodeName(), getPattern());
	}

	@Override
	public void updatePage () {
		super.updatePage();
		generator = new IaaSKloudResourceManagerPageGenerator(this, getNodeName(), getPattern());
	}

	private String getPattern () {
		String pattern = getDictionary().get("urlpattern").toString();
		if (pattern.endsWith("**")) {
			pattern = pattern.replace("**", "");
		}
		if (!pattern.endsWith("/")) {
			pattern = pattern + "/";
		}
		return pattern;
	}

	@Override
	public KevoreeHttpResponse process (KevoreeHttpRequest request, KevoreeHttpResponse response) {
		return generator.process(request, response);
	}
}
