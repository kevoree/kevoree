package org.kevoree.experiment.trace.gui.alg

import org.jfree.data.category.{DefaultCategoryDataset, CategoryDataset}
import java.util.Calendar
import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import org.jfree.chart.{ChartUtilities, ChartFactory, JFreeChart}
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import java.awt.Color
import org.kevoree.experiment.trace.gui.VectorClockDisseminationCategoryItemLabelGenerator
import org.jfree.chart.axis.SymbolAxis

class VectorClockSingleDisseminationChartScala(ltrace: LinkedTrace) {

  var nodes: java.util.List[String] = new java.util.ArrayList[String]

  private def buildPlot(idCateg: java.lang.Integer, beginningOfTime: Long, ltrace: LinkedTrace, defaultCategoryDataset: DefaultCategoryDataset, previousTimeStamps: List[java.lang.Long]) {

    val calendar: Calendar = Calendar.getInstance
    calendar.setTimeInMillis(ltrace.trace.getTimestamp - beginningOfTime)
    val timeRepresentation = "" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND)

    if (!nodes.contains(ltrace.trace.getClientId)) {
      nodes.add(ltrace.trace.getClientId)
    }

    defaultCategoryDataset.addValue(nodes.indexOf(ltrace.trace.getClientId), idCateg, timeRepresentation)
    //Call successors , increment idCateg not for first but for all others
    var i: java.lang.Integer = new java.lang.Integer(0)

    val newTimeStampL: List[java.lang.Long] = previousTimeStamps ++ List[java.lang.Long](ltrace.trace.getTimestamp)

    ltrace.sucessors.foreach {
      successor =>

        buildPlot(new java.lang.Integer(idCateg.intValue + i.intValue), beginningOfTime, successor, defaultCategoryDataset, newTimeStampL)
        if (i != 0) {
          //INIT NULL VALUE
          previousTimeStamps.foreach {
            time =>
              defaultCategoryDataset.addValue(null, new java.lang.Integer(i.intValue), time)
          }

        }
        i = new java.lang.Integer(i.intValue + 1)
    }
  }

  private def buildCateg(): CategoryDataset = {
    val defaultcategorydataset = new DefaultCategoryDataset
    buildPlot(new java.lang.Integer(0), ltrace.trace.getTimestamp, ltrace, defaultcategorydataset, List())
    defaultcategorydataset
  }


  def buildChart(): JFreeChart = {
    val jfreechart: JFreeChart = ChartFactory.createLineChart("VectorClock updates", "Time", "nodes", buildCateg, PlotOrientation.VERTICAL, false, true, false)
    val categoryplot: CategoryPlot = jfreechart.getPlot.asInstanceOf[CategoryPlot]
    ChartUtilities.applyCurrentTheme(jfreechart)
    val lineandshaperenderer: LineAndShapeRenderer = categoryplot.getRenderer.asInstanceOf[LineAndShapeRenderer]

    var i = 0
    import scala.collection.JavaConversions._
    nodes.foreach {
      node =>
        lineandshaperenderer.setSeriesShapesVisible(i, true)
        lineandshaperenderer.setSeriesLinesVisible(i, true)
        categoryplot.getRenderer.setSeriesPaint(i, Color.RED)
        i = i + 1
    }

    //lineandshaperenderer.setBaseItemLabelsVisible(true)
    //lineandshaperenderer.setBaseItemLabelGenerator(new VectorClockDisseminationCategoryItemLabelGenerator(nodeIds, timeRepresentations, vectorClocks))
    val symbolaxis: SymbolAxis = new SymbolAxis("Nodes", nodes.toArray(List("").toArray))
    categoryplot.setRangeAxis(symbolaxis)
    return jfreechart
  }


}