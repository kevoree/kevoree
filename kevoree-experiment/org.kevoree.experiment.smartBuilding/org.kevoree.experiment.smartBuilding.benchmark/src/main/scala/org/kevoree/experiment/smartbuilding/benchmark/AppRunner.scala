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

  val expes = List(new Experiment1, new Experiment2, new Experiment3, new Experiment4, new Experiment5)

  var expe = expes(3)
  expe.boardPortName = "/dev/tty.usbmodem411"
  expe.boardTypeName = "mega2560"


  // expes.foreach{ expe =>
  runExperiment(expe);
  // }



  System.exit(0)


  def runExperiment(absExp: AbstractExperiment) {
    val frame = new JFrame("Kevoree Arduino Benchmark => " + absExp.getClass.getName);
    frame.getContentPane.add(SmartSensorsGUI.getPanel);
    frame.setSize(1024, 768);
    frame.setVisible(true);
    absExp.init()
    NativeLibUtil.standaloneRxTx()
    val tester = new TwoWayActors(expe.boardPortName);
    absExp.runExperiment(tester)
    tester.killConnection()
    absExp.saveRawDump()
    absExp.saveRowImage()
    absExp.saveRScript()
    frame.dispose()
    SmartSensorsGUI.clear()
  }


}
