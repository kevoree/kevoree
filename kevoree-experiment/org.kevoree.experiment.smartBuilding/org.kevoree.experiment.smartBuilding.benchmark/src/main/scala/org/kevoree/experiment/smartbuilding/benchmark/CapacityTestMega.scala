package org.kevoree.experiment.smartbuilding.benchmark

import util.Random
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.marShell.parser.KevsParser
import org.kevoree.tools.marShell.interpreter.KevsInterpreterAspects._
import java.lang.StringBuilder
import org.kevoree.tools.marShell.interpreter.KevsInterpreterContext
import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.tools.marShell.ast.Script
import org.kevoree.tools.marShellTransform.{AdaptationModelWrapper, KevScriptWrapper}
import org.eclipse.emf.ecore.util.EcoreUtil

/**
 * User: ffouquet
 * Date: 23/06/11
 * Time: 10:35
 */

class CapacityTestMega extends AbstractExperiment {

  boardPortName = "/dev/tty.usbmodem411"
  boardTypeName = "mega2560"

  val random = new Random
  var knodeName = "kbenchmark"
  var kevsParser = new KevsParser


  //STEP 0 : Init Base model
  val modelBase = this.getClass.getClassLoader.getResource("baseKCrashBench.kev").getPath
  var model: ContainerRoot = KevoreeXmiHelper.load(modelBase)



  override def init() {
    super.initNode(knodeName, model)
    Thread.sleep(1000)
  }

  var twa: TwoWayActors = null

  def runExperiment(_twa: TwoWayActors) {
    twa = _twa;
    for (i <- 0 until 100) {
      doPhase(i)
    }
  }

  def doPhase(i: Int) {
    val previousModel : ContainerRoot = EcoreUtil.copy(model)
    val sb = new StringBuilder
    sb append "tblock{ "
    sb append "addComponent c" + i + "@" + knodeName + ":Timer\n"
   // sb append "bind c" + i + ".toggle@" + knodeName + "=>ch\n"
    sb append "}"
    val script = kevsParser.parseScript(sb.toString)
    script.get.interpret(KevsInterpreterContext(model))
    doStep(previousModel, model, i);
    Thread.sleep(140)
    interpetResult(i, twa.recString)
  }

  def doStep(modelA: ContainerRoot, modelB: ContainerRoot, i: Int) {
    val modelAtoB: AdaptationModel = kompare.kompare(modelA, modelB, knodeName)
    val baseScript: Script = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(modelAtoB))
    val resultScript: String = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    //println("ReconfSTEP=>" + resultScript)
    val randomToken = random.nextInt(9)
    println(resultScript)
    (twa.sendAndWait(("$" + randomToken) + resultScript, ("ack" + randomToken), 2000))
    interpetResult(i, twa.recString)
    twa.recString = ""
  }


  def interpetResult(i: Int, s: String) {


    s.split('\n').foreach {
      line =>
      println(line)
        if (line.startsWith("mem")) {
          SmartSensorsGUI.putMemValue(i, Integer.parseInt(line.substring(3).trim()))
          addToRaw(i,"SDRAM",Integer.parseInt(line.substring(3).trim()))
        }
        if (line.startsWith("emem")) {
          SmartSensorsGUI.putEMemValue(i, Integer.parseInt(line.substring(4).trim()))
          addToRaw(i,"EEPROM",Integer.parseInt(line.substring(4).trim()))
        }

        if (line.startsWith("ms")) {
          SmartSensorsGUI.putRTimeValue(i, Integer.parseInt(line.substring(2).trim()))
          addToRaw(i,"DOWNTIME",Integer.parseInt(line.substring(2).trim()))
        }
    }
  }


}