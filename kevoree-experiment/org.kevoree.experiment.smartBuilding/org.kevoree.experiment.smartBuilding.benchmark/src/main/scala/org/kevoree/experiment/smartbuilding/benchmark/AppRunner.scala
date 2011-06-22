/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kevoree.experiment.smartbuilding.benchmark

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.kompare.KevoreeKompareBean
import org.kevoreeAdaptation.AdaptationModel
import org.kevoree.tools.marShell.ast.Script
import org.kevoree.tools.marShellTransform.{AdaptationModelWrapper, KevScriptWrapper}
import org.kevoree.experiment.smartbuilding.com.NativeLibUtil
import org.kevoree.{KevoreeFactory, ContainerRoot}
import java.io.File
import org.wayoda.ang.project.TargetDirectoryService
import org.kevoree.library.arduinoNodeType.{ArduinoGuiProgressBar, ArduinoNode}
import org.kevoree.library.arduinoNodeType.utils.ArduinoHomeFinder
import org.kevoree.library.javase.grapher.{GrapherFactory, Grapher}
import util.Random
import info.monitorenter.gui.chart.Chart2D
import java.awt.Color
import info.monitorenter.gui.chart.traces.Trace2DLtd
import javax.swing.{JFrame, JPanel}

object AppRunner extends App {

  ArduinoHomeFinder.checkArduinoHome()


  var frame = new JFrame("Kevoree Arduino Benchmark ");
  frame.getContentPane().add(SmartSensorsGUI.getPanel);
  frame.setSize(1024, 768);
  frame.setVisible(true);


  var knodeName = "kbenchmark"
  var kompare: KevoreeKompareBean = new KevoreeKompareBean

  //STEP 0 : Init Base model
  val modelBase = this.getClass.getClassLoader.getResource("baseKBench.kev").getPath
  var model: ContainerRoot = KevoreeXmiHelper.load(modelBase)
  val model2Path = this.getClass.getClassLoader.getResource("baseKBenchStep2.kev").getPath
  var model2: ContainerRoot = KevoreeXmiHelper.load(model2Path)
  val model22Path = this.getClass.getClassLoader.getResource("baseKBenchStep22.kev").getPath
  var model22: ContainerRoot = KevoreeXmiHelper.load(model22Path)

  val model3Path = this.getClass.getClassLoader.getResource("baseKBenchStep3.kev").getPath
  var model3: ContainerRoot = KevoreeXmiHelper.load(model3Path)

  var baseTime = System.currentTimeMillis()

  var node: ArduinoNode = new ArduinoNode
  node.getDictionary.put("boardTypeName", "atmega328")
  node.getDictionary.put("boardPortName", "/dev/tty.usbserial-A400g2se")
  node.getDictionary.put("incremental", "false")

  node.newdir = new File("arduinoGenerated" + knodeName)
  if (!node.newdir.exists) {
    node.newdir.mkdir
  }
  node.progress = new ArduinoGuiProgressBar {
    override def endTask() {}

    override def beginTask(s: String, i: java.lang.Integer) {}
  }
  var newdirTarget: File = new File("arduinoGenerated" + knodeName + "/target")
  org.kevoree.library.arduinoNodeType.FileHelper.createAndCleanDirectory(newdirTarget)

  TargetDirectoryService.rootPath = newdirTarget.getAbsolutePath
  node.outputPath = node.newdir.getAbsolutePath

  node.deploy(kompare.kompare(KevoreeFactory.eINSTANCE.createContainerRoot(), model, knodeName), knodeName)

  println("INIT_MS=" + (System.currentTimeMillis() - baseTime))


  NativeLibUtil.standaloneRxTx()
  var tester = new TwoWayActors("/dev/tty.usbserial-A400g2se");

  val models = List(model, model2, model22, model3)
  var previousModel = model
  val random = new Random

  for (i <- 0 until 200) {
    doPhase(i)
  }





  tester.killConnection()


  def doPhase(i:Int) {

    var newModel = models(random.nextInt(models.size))
    while (newModel == previousModel) {
      newModel = models(random.nextInt(models.size))
    }


    doStep(previousModel, newModel,i);
    previousModel = newModel
    Thread.sleep(50)
  }

  def doStep(modelA: ContainerRoot, modelB: ContainerRoot,i:Int) {
    val modelAtoB: AdaptationModel = kompare.kompare(modelA, modelB, knodeName)
    val baseScript: Script = KevScriptWrapper.miniPlanKevScript(AdaptationModelWrapper.generateScriptFromAdaptModel(modelAtoB))
    val resultScript: String = KevScriptWrapper.generateKevScriptCompressed(baseScript)
    //println("ReconfSTEP=>" + resultScript)
    val randomToken = random.nextInt(9)
    (tester.sendAndWait(("$" + randomToken) + resultScript, ("ack" + randomToken), 2000))
    interpetResult(i,tester.recString)
    tester.recString = ""
  }


  def interpetResult(i: Int, s: String) {
    //println("Res")
    // println(s)
    s.split('\n').foreach {
      line =>
        //println(line)
        if (line.startsWith("mem")) {
          SmartSensorsGUI.putMemValue(i, Integer.parseInt(line.substring(3).trim()))
        }
        if (line.startsWith("emem")) {
          SmartSensorsGUI.putEMemValue(i, Integer.parseInt(line.substring(4).trim()))
        }
        if (line.startsWith("ms")) {
          SmartSensorsGUI.putRTimeValue(i, Integer.parseInt(line.substring(2).trim()))
        }
    }
  }


}
