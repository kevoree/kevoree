package org.kevoree.library.frascati.mavenplugin

import java.io.File
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.KevoreeFactory
import org.kevoree.impl.DefaultKevoreeFactory

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:44
 */

object Tester extends App {
        println("Hi")
  KevoreeXmiHelper.$instance.save("/tmp/test.kev",CompositeParser.parseCompositeFile(new DefaultKevoreeFactory().createContainerRoot,
new File("/opt/frascati-runtime-1.4/examples/helloworld-pojo/src/main/resources/helloworld-pojo.composite"),"1.5.1-SNAPSHOT","org.kevoree.library.javase","org.kevoree.library.javase.test","helloworld-pojo.composite"))
}