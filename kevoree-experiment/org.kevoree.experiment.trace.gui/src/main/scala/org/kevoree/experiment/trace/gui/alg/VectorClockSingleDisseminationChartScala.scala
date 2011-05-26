package org.kevoree.experiment.trace.gui.alg

import org.jfree.data.category.{DefaultCategoryDataset, CategoryDataset}
import java.util.Calendar
import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import org.jfree.chart.{ChartUtilities, ChartFactory, JFreeChart}
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import java.awt.Color
import util.Random

//import org.kevoree.experiment.trace.gui.VectorClockDisseminationCategoryItemLabelGenerator

import org.jfree.chart.axis.SymbolAxis


class VectorClockSingleDisseminationChartScala(ltrace: LinkedTrace) {

  var nodes: java.util.List[String] = new java.util.ArrayList[String]
  var gidCateg = new java.lang.Integer(0)

  private def buildPlot(beginningOfTime: Long, ltrace: LinkedTrace, defaultCategoryDataset: DefaultCategoryDataset , previousTimeStamps: List[java.lang.Long]) {

    val timeStampMilli = ((ltrace.trace.getTimestamp - beginningOfTime) / 1000000)
    // calendar.setTimeInMillis((ltrace.trace.getTimestamp - beginningOfTime) / 1000000)
    // val timeRepresentation = "" /*+ calendar.get(Calendar.MINUTE)*/ + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND)
    if (!nodes.contains(ltrace.trace.getClientId)) {
      nodes.add(ltrace.trace.getClientId)
    }

    println("newCateg=" + gidCateg + "," + ltrace.trace.getClientId)
    defaultCategoryDataset.addValue(nodes.indexOf(ltrace.trace.getClientId), gidCateg, new java.lang.Long(timeStampMilli))

    //Call successors , increment idCateg not for first but for all others
    var i: java.lang.Integer = new java.lang.Integer(0)
    //val newTimeStampL: List[java.lang.Long] = previousTimeStamps ++ List[java.lang.Long](timeStampMilli)
    ltrace.sucessors.foreach {
      successor =>
        if (i.intValue() > 0) {
          gidCateg = gidCateg.intValue() + 1
          //INSERT IT SELF IN THIS CATEGORY
          defaultCategoryDataset.addValue(nodes.indexOf(ltrace.trace.getClientId), gidCateg, new java.lang.Long(timeStampMilli))
          //INIT DEF VALUE
          previousTimeStamps.foreach {
            time =>
              defaultCategoryDataset.addValue(null, gidCateg, time)
          }

        }

        val newTimeStampL: List[java.lang.Long] = previousTimeStamps ++ List[java.lang.Long](timeStampMilli)
        buildPlot(beginningOfTime, successor, defaultCategoryDataset,newTimeStampL)
        // if (i != 0) {
        //INIT NULL VALUE
        /*
          previousTimeStamps.foreach {
            time =>
              defaultCategoryDataset.addValue(null, new java.lang.Integer(i.intValue), time)
          } */

        //}
        i = new java.lang.Integer(i.intValue + 1)
    }
  }

  private def buildCateg(): CategoryDataset = {
    val defaultcategorydataset = new DefaultCategoryDataset
    buildPlot(ltrace.trace.getTimestamp, ltrace, defaultcategorydataset,List())
    defaultcategorydataset
  }


  def buildChart(): JFreeChart = {
    val jfreechart: JFreeChart = ChartFactory.createLineChart("VectorClock updates", "Time", "nodes", buildCateg, PlotOrientation.VERTICAL, false, true, false)
    val categoryplot: CategoryPlot = jfreechart.getPlot.asInstanceOf[CategoryPlot]
    ChartUtilities.applyCurrentTheme(jfreechart)
    val lineandshaperenderer: LineAndShapeRenderer = categoryplot.getRenderer.asInstanceOf[LineAndShapeRenderer]

    import scala.collection.JavaConversions._
    for (i <- 0 until gidCateg.intValue() + 1) {
      lineandshaperenderer.setSeriesShapesVisible(i, true)
      lineandshaperenderer.setSeriesLinesVisible(i, true)

      val rand = new Random
      val color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());

      categoryplot.getRenderer.setSeriesPaint(i, color)
    }

    val symbolaxis: SymbolAxis = new SymbolAxis("Nodes", nodes.toArray(List("").toArray))
    categoryplot.setRangeAxis(symbolaxis)
    return jfreechart
  }


}