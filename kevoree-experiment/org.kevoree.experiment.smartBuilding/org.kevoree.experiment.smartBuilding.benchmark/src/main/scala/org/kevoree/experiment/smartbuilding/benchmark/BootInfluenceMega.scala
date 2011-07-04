package org.kevoree.experiment.smartbuilding.benchmark

import util.Random
import org.kevoree.ContainerRoot
import org.kevoree.framework.KevoreeXmiHelper
import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.tools.marShell.ast.Script
import org.kevoree.tools.marShellTransform.{AdaptationModelWrapper, KevScriptWrapper}

/**
 * User: ffouquet
 * Date: 23/06/11
 * Time: 10:35
 */

class BootInfluenceMega extends AbstractExperiment {

  val random = new Random
  var knodeName = "kbenchmark"
  boardPortName = "/dev/tty.usbmodem411"
  boardTypeName = "mega2560"


  //STEP 0 : Init Base model
  val modelBase = this.getClass.getClassLoader.getResource("baseKBench.kev").getPath
  var model: ContainerRoot = KevoreeXmiHelper.load(modelBase)
  val model2Path = this.getClass.getClassLoader.getResource("baseKBenchStep2.kev").getPath
  var model2: ContainerRoot = KevoreeXmiHelper.load(model2Path)
  val model22Path = this.getClass.getClassLoader.getResource("baseKBenchStep22.kev").getPath
  var model22: ContainerRoot = KevoreeXmiHelper.load(model22Path)
  val model3Path = this.getClass.getClassLoader.getResource("baseKBenchStep3.kev").getPath
  var model3: ContainerRoot = KevoreeXmiHelper.load(model3Path)

  val models = List(model, model2, model22, model3)
  var previousModel: ContainerRoot = null

  override def init() {
    super.initNode(knodeName, model)
    previousModel = model
  }

  var twa: TwoWayActors = null

  def resetArduino() {
    if (twa != null) {
      twa.killConnection()
      val pPort = twa.portIdentifier.getName
      Thread.sleep(500)
      twa = new TwoWayActors(pPort)
      Thread.sleep(1000)
    }
  }

  def runExperiment(_twa: TwoWayActors) {
    twa = _twa;
    for (i <- 0 until 200) {
      doPhase(i)
    }
  }

  def doPhase(i: Int) {
    var newModel = models(random.nextInt(models.size))
    while (newModel == previousModel) {
      newModel = models(random.nextInt(models.size))
    }
    doStep(previousModel, newModel, i);
    previousModel = newModel
    Thread.sleep(200)
    //if ((i % 2) == 0) {
      resetArduino()
    //}
    interpetResult(i, twa.recString)
  }

  def doStep(modelA: ContainerRoot, modelB: ContainerRoot, i: Int) {
    val modelAtoB: AdaptationModel = kompare.kompare(modelA, modelB, knodeName)
    val baseScript: Script = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(modelAtoB))
    val resultScript: String = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    //println("ReconfSTEP=>" + resultScript)
    val randomToken = random.nextInt(9)
    (twa.sendAndWait(("$" + randomToken) + resultScript, ("ack" + randomToken), 2000))
    interpetResult(i, twa.recString)
    twa.recString = ""
  }

  def interpetResult(i: Int, s: String) {

    //  println(s)

    s.split('\n').foreach {
      line =>
      println(line)
        if (line.startsWith("mem")) {
          SmartSensorsGUI.putMemValue(i, Integer.parseInt(line.substring(3).trim()))
          addToRaw(i, "SDRAM", Integer.parseInt(line.substring(3).trim()))
        }
        if (line.startsWith("emem")) {
          println("EMEM=>"+line.substring(4).trim())
          SmartSensorsGUI.putEMemValue(i, Integer.parseInt(line.substring(4).trim()))
          addToRaw(i, "EEPROM", Integer.parseInt(line.substring(4).trim()))
        }

        if (line.startsWith("bootms")) {
          SmartSensorsGUI.putRTimeValue(i, Integer.parseInt(line.substring(6).trim()))
          addToRaw(i, "BOOTTIME", Integer.parseInt(line.substring(6).trim()))
        }
    }
  }


}