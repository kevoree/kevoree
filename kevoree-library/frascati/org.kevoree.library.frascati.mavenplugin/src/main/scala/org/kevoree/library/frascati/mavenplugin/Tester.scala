package org.kevoree.library.frascati.mavenplugin

import java.io.File
import org.kevoree.framework.KevoreeXmiHelper

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:44
 */

object Tester extends App {

  KevoreeXmiHelper.save("/tmp/test.kev",CompositeParser.parseCompositeFile(new File("/opt/frascati-runtime-1.4/examples/helloworld-pojo/src/main/resources/helloworld-pojo.composite"),"1.5.1-SNAPSHOT","org.kevoree.library.javase","org.kevoree.library.javase.test","helloworld-pojo.composite"))
}