package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.ComponentFragment;
import org.kevoree.annotation.Library;
import org.kevoree.annotation.Start;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 23/01/12
 * Time: 08:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@Library(name = "JavaSE")
@ComponentFragment
public class ParentAbstractPage extends AbstractPage {

	@Override
	@Start
    public void startPage() {
        handler.initRegex(this.getDictionary().get("urlpattern").toString() + "**");
        logger.debug("Parent abstract page start with pattern = {}", this.getDictionary().get("urlpattern").toString() + "**");
    }
}
