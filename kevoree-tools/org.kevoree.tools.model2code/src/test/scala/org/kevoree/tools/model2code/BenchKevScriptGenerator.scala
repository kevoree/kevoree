package org.kevoree.tools.model2code

import java.net.URI
import java.io.{PrintWriter, File}

/**
 * Created by IntelliJ IDEA.
 * User: gnain
 * Date: 12/12/11
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */

object BenchKevScriptGenerator extends App {

  val scriptFolder = new File(URI.create("file:/Users/gnain/sources/entimid/entimid-library/genlibs/scripts"))
  if (!scriptFolder.exists()) {
    scriptFolder.mkdirs()
  }
  generate1A1()
  generate1A2()
  generate1A3()
  generate1B1()
  generate1B2()
  generate1B3()
  generate1C1()
  generate1C2()
  generate1C3()

  def decorateWithBase(script: String): String = {
    "tblock {\n" +
      "\tmerge \"mvn:org.kevoree.library.model/org.kevoree.library.model.bootstrap/1.5.0-SNAPSHOT\"\n" +
      "\taddGroup sync : RestGroup\n" +
      "\taddNode node0 : JavaSENode\n" +
      "\taddToGroup sync node0\n\n" +
      script +
      "}"
  }

  def generate1B1() {
    val script1B1 = new File(scriptFolder.getAbsolutePath + "/1B1Script.kevs")
    if(script1B1.exists()) {return}
    val pr = new PrintWriter(script1B1, "utf-8")

    var script = "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.ABC1TestComponent0/1.0.0-SNAPSHOT\"\n"
    for(i <- 0 to 49) {
      script += "\taddComponent Instance"+i+"_1B1@node0 : ABC1TestComponent00\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }

  def generate1B2() {

    val script1B2 = new File(scriptFolder.getAbsolutePath + "/1B2Script.kevs")
    if(script1B2.exists()) {return}
    val pr = new PrintWriter(script1B2, "utf-8")

    var script = "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.B2TestComponent0/1.0.0-SNAPSHOT\"\n"
    for(i <- 0 to 49) {
      script += "\taddComponent Instance"+i+"_1B2@node0 : B2TestComponent0"+i+"\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }

  def generate1B3() {

    val script1B3 = new File(scriptFolder.getAbsolutePath + "/1B3Script.kevs")
    if(script1B3.exists()) {return}
    val pr = new PrintWriter(script1B3, "utf-8")

    var script = ""
    for(i <- 0 to 49) {
      script += "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.B3TestComponent"+i+"/1.0.0-SNAPSHOT\"\n"
      script += "\taddComponent Instance"+i+"_1B3@node0 : B3TestComponent"+i+"0\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }


  def generate1C1() {
    val script1C1 = new File(scriptFolder.getAbsolutePath + "/1C1Script.kevs")
    if(script1C1.exists()) {return}
    val pr = new PrintWriter(script1C1, "utf-8")

    var script = "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.ABC1TestComponent0/1.0.0-SNAPSHOT\"\n"
    for(i <- 0 to 99) {
      script += "\taddComponent Instance"+i+"_1C1@node0 : ABC1TestComponent00\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }

  def generate1C2() {

    val script1C2 = new File(scriptFolder.getAbsolutePath + "/1C2Script.kevs")
    if(script1C2.exists()) {return}
    val pr = new PrintWriter(script1C2, "utf-8")

    var script = "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.C2TestComponent0/1.0.0-SNAPSHOT\"\n"
    for(i <- 0 to 99) {
      script += "\taddComponent Instance"+i+"_1C2@node0 : C2TestComponent0"+i+"\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }

  def generate1C3() {

    val script1C3 = new File(scriptFolder.getAbsolutePath + "/1C3Script.kevs")
    if(script1C3.exists()) {return}
    val pr = new PrintWriter(script1C3, "utf-8")

    var script = ""
    for(i <- 0 to 99) {
      script += "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.C3TestComponent"+i+"/1.0.0-SNAPSHOT\"\n"
      script += "\taddComponent Instance"+i+"_1C3@node0 : C3TestComponent"+i+"0\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }


  def generate1A1() {
    val script1A1 = new File(scriptFolder.getAbsolutePath + "/1A1Script.kevs")
    if(script1A1.exists()) {return}
    val pr = new PrintWriter(script1A1, "utf-8")

    var script = "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.ABC1TestComponent0/1.0.0-SNAPSHOT\"\n"
    for(i <- 0 to 9) {
      script += "\taddComponent Instance"+i+"_1A1@node0 : ABC1TestComponent00\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }

  def generate1A2() {

    val script1A2 = new File(scriptFolder.getAbsolutePath + "/1A2Script.kevs")
    if(script1A2.exists()) {return}
    val pr = new PrintWriter(script1A2, "utf-8")

    var script = "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.A2TestComponent0/1.0.0-SNAPSHOT\"\n"
    for(i <- 0 to 9) {
      script += "\taddComponent Instance"+i+"_1A2@node0 : A2TestComponent0"+i+"\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }

  def generate1A3() {

    val script1A3 = new File(scriptFolder.getAbsolutePath + "/1A3Script.kevs")
    if(script1A3.exists()) {return}
    val pr = new PrintWriter(script1A3, "utf-8")

    var script = ""
    for(i <- 0 to 9) {
      script += "\tmerge \"mvn:org.entimid.genlib/org.entimid.genlib.A3TestComponent"+i+"/1.0.0-SNAPSHOT\"\n"
      script += "\taddComponent Instance"+i+"_1A3@node0 : A3TestComponent"+i+"0\n"
    }

    pr.print(decorateWithBase(script))
    pr.flush()
    pr.close()
  }
}