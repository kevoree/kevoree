package org.kevoree.library.sky.provider.web;

import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.Library;
import org.kevoree.library.javase.webserver.KevoreeHttpRequest;
import org.kevoree.library.javase.webserver.KevoreeHttpResponse;
import org.kevoree.library.javase.webserver.ParentAbstractPage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 24/10/12
 * Time: 15:54
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "SKY")
@ComponentFragment
public abstract class KloudResourceManagerPage extends ParentAbstractPage {
	KloudResourceManagerPageGenerator generator;

	String getPattern () {
		// TODO replace stuff like {login} by something like (.+)
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
