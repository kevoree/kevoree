package org.kevoree.library.javase.webserver.servlet;

import org.kevoree.annotation.*;
import org.kevoree.library.javase.webserver.AbstractPage;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/07/12
 * Time: 22:16
 *
 * @author Erwan Daubert
 * @version 1.0
 */
@ComponentFragment
@Provides({
		@ProvidedPort(name = "filteredResponse", type = PortType.MESSAGE)
})
@Requires({
		@RequiredPort(name = "filteredRequest", type = PortType.MESSAGE)
})
public abstract class AbstractFilterPage extends AbstractPage {

}
