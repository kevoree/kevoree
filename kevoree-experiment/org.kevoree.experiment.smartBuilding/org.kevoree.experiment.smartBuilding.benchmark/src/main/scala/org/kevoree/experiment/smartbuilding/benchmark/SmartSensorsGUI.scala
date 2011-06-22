package org.kevoree.experiment.smartbuilding.benchmark

import info.monitorenter.gui.chart.Chart2D
import java.awt.Color
import javax.swing.{BoxLayout, JPanel}
import info.monitorenter.gui.chart.traces.{Trace2DSimple, Trace2DLtd}
import info.monitorenter.gui.chart.rangepolicies.RangePolicyMinimumViewport

/**
 * User: ffouquet
 * Date: 22/06/11
 * Time: 18:46
 */

object SmartSensorsGUI {

  var chartMem = new Chart2D();
  chartMem.setBackground(Color.DARK_GRAY);
  //chartMem.setGridColor(Color.ORANGE)

  var axisXMem = chartMem.getAxisX();
 // axisXMem.setPaintGrid(true)

  var axisYMem = chartMem.getAxisY();
  axisXMem.setStartMajorTick(false);
  axisXMem.setMajorTickSpacing(10);
 // axisXMem.setRangePolicy(new RangePolicyMinimumViewport(new info.monitorenter.util.Range(0,1700)));

  //axisYMem.setRange(new info.monitorenter.util.Range(0,1700))
  var traceMem = new Trace2DSimple();
  traceMem.setColor(Color.RED);
  chartMem.addTrace(traceMem);

  var chartEMem = new Chart2D();
  chartEMem.setBackground(Color.DARK_GRAY);
  var axisXEMem = chartEMem.getAxisX();
  axisXEMem.setStartMajorTick(false);
  axisXEMem.setMajorTickSpacing(10);
  var traceEMem = new Trace2DSimple();
  traceEMem.setColor(Color.YELLOW);
  chartEMem.addTrace(traceEMem);

  var chartRTime = new Chart2D();
  chartRTime.setBackground(Color.DARK_GRAY);
  var axisXRTime = chartRTime.getAxisX();
  axisXRTime.setStartMajorTick(false);
  axisXRTime.setMajorTickSpacing(10);
  var traceTTime = new Trace2DSimple();
  traceTTime.setColor(Color.ORANGE);
  chartRTime.addTrace(traceTTime);

  def getPanel = {
    val p = new JPanel()
    p.setLayout(new BoxLayout(p,BoxLayout.PAGE_AXIS))
    p.add(chartMem)
    p.add(chartEMem)
    p.add(chartRTime)
    p
  }

  def putMemValue(index:Int,i : Int){
       traceMem.addPoint(index,i)
  }
  def putEMemValue(index:Int,i : Int){
       traceEMem.addPoint(index,i)
  }
  def putRTimeValue(index:Int,i : Int){
       traceTTime.addPoint(index,i)
  }


}