package org.kevoree.library.javase.webserver;

import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 14/10/11
 * Time: 08:52
 * To change this template use File | Settings | File Templates.
 */


@Library(name = "JavaSE")
@ComponentFragment
@Provides({
        @ProvidedPort(name = "request", type = PortType.MESSAGE)
})
@Requires({
        @RequiredPort(name = "content", type = PortType.MESSAGE)
})
public class AbstractPage extends AbstractComponentType {

}
