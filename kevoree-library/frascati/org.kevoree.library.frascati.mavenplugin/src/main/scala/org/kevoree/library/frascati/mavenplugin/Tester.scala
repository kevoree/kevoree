package org.kevoree.library.frascati.mavenplugin

import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 30/01/12
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */

object Tester extends App {

  CompositeParser.parseCompositeFile(new File("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-library/frascati/org.kevoree.library.frascati.helloworld.pojo/src/main/resources/helloworld-pojo.composite"))

}