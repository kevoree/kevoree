package org.kevoree.experiment.modelScript

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 28/05/11
 * Time: 14:56
 */

object ModificationApp extends Application {

  override def main (args: Array[String]) {
    val modificationGenerator = new ModificationGenerator(Configuration.ips)

    val stream = System.in
    var b = stream.read()
    while (b != -1 && b != 'q') {
      modificationGenerator.doAction("kspark0")
      b = stream.read()
    }
  }

}