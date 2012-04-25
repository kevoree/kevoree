package org.kevoree.library.arduinoNodeType.generator.templates

import io.Source

/**
 * User: ffouquet
 * Date: 05/07/11
 * Time: 08:48
 */

object SimpleCopyTemplate {

    def copyFromClassPath(path : String) : String = {
      Source.fromInputStream(this.getClass.getClassLoader.getResourceAsStream(path),"utf-8").getLines().mkString("\n")
    }


}