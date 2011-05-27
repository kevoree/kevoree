package org.kevoree.experiment.trace.gui.alg

import org.jfree.data.category.{DefaultCategoryDataset, CategoryDataset}
import org.jfree.chart.{ChartUtilities, ChartFactory, JFreeChart}
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import java.awt.Color
import util.Random
import scala.collection.JavaConversions._
import java.lang.Long
import collection.immutable.List
import collection.immutable.List._
import java.util._
import org.jfree.chart.axis.{NumberAxis, SymbolAxis}
import org.jfree.data.xy.{XYSeriesCollection, XYSeries, XYDataset}
import org.jfree.chart.plot.{XYPlot, CategoryPlot, PlotOrientation}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer

//import org.kevoree.experiment.trace.gui.VectorClockDisseminationCategoryItemLabelGenerator

class VectorClockSingleDisseminationChartScala (ltrace: LinkedTrace) {

  var nodes: java.util.List[String] = new java.util.ArrayList[String]
  var gidCateg = new java.lang.Integer(0)

  // {idCateg, (nodeId, timeStamp)}
  var categoryRepresentations = new HashMap[String, List[(String, Long)]]()
  // {timeStamp}
  var timeRepresentations: Set[Long] = new TreeSet[Long]

  private def buildCategory (beginningOfTime: java.lang.Long, trace: LinkedTrace) {
    if (!nodes.contains(ltrace.trace.getClientId)) {
      nodes.add(ltrace.trace.getClientId)
    }
    //println("newCateg=" + gidCateg + "," + trace.trace.getClientId)


    /*val timeStampMilli: Long = ((trace.trace.getTimestamp.longValue() - beginningOfTime.longValue()) /
      1000000)
    timeRepresentations.find(t => t == timeStampMilli) match {
      case Some(set) => // NOOP
      case None => timeRepresentations.add(timeStampMilli)
    }
    var values: List[(String, Long)] = categoryRepresentations.get(gidCateg + "")
    if (values == null) {
      values = List[(String, Long)]()
    }
    values = values ++ List((trace.trace.getClientId, timeStampMilli))
    categoryRepresentations.put(gidCateg + "", values)*/

    //var i: java.lang.Integer = 0
    trace.sucessors.foreach {
      successor =>
      //println(currentCategory + " => " + successor.trace.getClientId)
      //if (i.intValue() > 0) {
        gidCateg = gidCateg.intValue() + 1
        //}
        //i = i.intValue() + 1

        if (!nodes.contains(successor.trace.getClientId)) {
          nodes.add(successor.trace.getClientId)
        }

        var timeStampMilli: Long = ((trace.trace.getTimestamp.longValue() - beginningOfTime.longValue()) /
          1000000)
        println("timeStampMilli = " + timeStampMilli)
        timeRepresentations.find(t => t == timeStampMilli) match {
          case Some(set) => // NOOP
          case None => timeRepresentations.add(timeStampMilli)
        }
        var values: List[(String, Long)] = categoryRepresentations.get(gidCateg + "")
        if (values == null) {
          values = List[(String, Long)]()
        }
        values = values ++ List((trace.trace.getClientId, timeStampMilli))
        categoryRepresentations.put(gidCateg + "", values)


        timeStampMilli = ((successor.trace.getTimestamp.longValue() - beginningOfTime.longValue()) /
          1000000)
        timeRepresentations.find(t => t == timeStampMilli) match {
          case Some(set) => // NOOP
          case None => timeRepresentations.add(timeStampMilli)
        }
        values = categoryRepresentations.get(gidCateg + "")
        if (values == null) {
          values = List[(String, Long)]()
        }
        values.find(tuple => tuple._1 == successor.trace.getClientId) match {
          case Some(v) => // NO OP
          case None => values = values ++ List((successor.trace.getClientId, timeStampMilli))
        }
        categoryRepresentations.put(gidCateg + "", values)

        if (successor.sucessors.size > 0) {
          buildCategory(beginningOfTime, successor)
        }
    }
  }

  /*private def buildGraphCategory (defaultCategoryDataset: DefaultCategoryDataset) {
    categoryRepresentations.foreach {
      tuple: (String, List[(String, Long)]) =>
        println("categ => " + tuple._1 + " : ")
        timeRepresentations.foreach {
          time: java.lang.Long =>
            tuple._2.foreach {
              tupleValue: (String, Long) =>
                if (time == tupleValue._2) {
                  defaultCategoryDataset.addValue(nodes.indexOf(tupleValue._1), tuple._1, tupleValue._2)
                  println("\t" + tupleValue._1 + "(" + tupleValue._2 + ")")
                } /*else if (time.longValue() < tupleValue._2.longValue()) {
                  defaultCategoryDataset.addValue(null, tuple._1, time)
                }*/
            }
        }
    }
    }*/


  /*categoryRepresentations.foreach {
        tuple: (String, List[(String, Long)]) =>
          println("categ => " + tuple._1 + " : ")
          tuple._2.foreach {
            var currentTime: Long = 0
            tupleValue: (String, Long) =>
              println("\t" + tupleValue._1)
              //timeRepresentations.filter(t => t.longValue() > currentTime.longValue()).foreach {
              timeRepresentations.foreach {
                time: java.lang.Long =>
                  if (time == tupleValue._2) {
                    defaultCategoryDataset.addValue(nodes.indexOf(tupleValue._1), tuple._1, tupleValue._2)
                    currentTime = time
                  } else if (time.longValue() < tupleValue._2.longValue()) {
                    defaultCategoryDataset.addValue(null, tuple._1, time)
                  }

                /*timeRepresentations.filter(t => t == tupleValue._2) match {
                  case Some(v) => defaultCategoryDataset.addValue(nodes.indexOf(tupleValue._1), tuple._1, tupleValue._2)
                  case None => defaultCategoryDataset.addValue(null, tuple._1, tupleValue._2)
                }*/


              }
            /*if (vectorClockUpdates.get(nodeIds.get(i)).contains(oldTimeRepresentation)) {
              defaultcategorydataset.addValue(i, nodeIds.get(i), time)
            }
            else {
              defaultcategorydataset.addValue(null, nodeIds.get(i), time)
            }*/

          }
      }*/


  private def buildPlot (beginningOfTime: Long, ltrace: LinkedTrace, defaultCategoryDataset: DefaultCategoryDataset,
    previousTimeStamps: List[java.lang.Long]) {

    val timeStampMilli = ((ltrace.trace.getTimestamp.longValue() - beginningOfTime.longValue()) / 1000000)
    // calendar.setTimeInMillis((ltrace.trace.getTimestamp - beginningOfTime) / 1000000)
    // val timeRepresentation = "" /*+ calendar.get(Calendar.MINUTE)*/ + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND)
    if (!nodes.contains(ltrace.trace.getClientId)) {
      nodes.add(ltrace.trace.getClientId)
    }

    println("newCateg=" + gidCateg + "," + ltrace.trace.getClientId)
    defaultCategoryDataset
      .addValue(nodes.indexOf(ltrace.trace.getClientId), gidCateg, new java.lang.Long(timeStampMilli))

    //Call successors , increment idCateg not for first but for all others
    var i: java.lang.Integer = new java.lang.Integer(0)
    //val newTimeStampL: List[java.lang.Long] = previousTimeStamps ++ List[java.lang.Long](timeStampMilli)
    ltrace.sucessors.foreach {
      successor =>
        if (i.intValue() > 0) {
          gidCateg = gidCateg.intValue() + 1
          //INSERT IT SELF IN THIS CATEGORY
          defaultCategoryDataset
            .addValue(nodes.indexOf(ltrace.trace.getClientId), gidCateg, new java.lang.Long(timeStampMilli))
          //INIT DEF VALUE
          previousTimeStamps.foreach {
            time =>
              defaultCategoryDataset.addValue(null, gidCateg, time)
          }

        }

        val newTimeStampL: List[java.lang.Long] = previousTimeStamps ++ List[java.lang.Long](timeStampMilli)
        buildPlot(beginningOfTime, successor, defaultCategoryDataset, newTimeStampL)
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

  /*private def buildCateg (): CategoryDataset = {
    val defaultcategorydataset = new DefaultCategoryDataset
    buildPlot(ltrace.trace.getTimestamp, ltrace, defaultcategorydataset, List())
    defaultcategorydataset
  }*/

  /*private def buildCateg (): CategoryDataset = {
    val defaultcategorydataset = new DefaultCategoryDataset
    buildCategory(ltrace.trace.getTimestamp, ltrace);
    buildGraphCategory(defaultcategorydataset)
    //buildPlot(ltrace.trace.getTimestamp, ltrace, defaultcategorydataset, List())
    defaultcategorydataset
  }*/


  /*def buildChart (): JFreeChart = {
    val jfreechart: JFreeChart = ChartFactory
      .createLineChart("VectorClock updates", "Times", "nodes", buildCateg(), PlotOrientation.VERTICAL, false, true,
                        false)
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
    jfreechart
  }*/

  private def buildDataset() : XYDataset = {
    val xyseriescollection: XYSeriesCollection = new XYSeriesCollection
    buildCategory(ltrace.trace.getTimestamp, ltrace)
    populateCollection(xyseriescollection)
    xyseriescollection
  }

  private def populateCollection(dataset : XYSeriesCollection) {
    categoryRepresentations.foreach {
      tuple: (String, List[(String, Long)]) =>
        val xyseries: XYSeries = new XYSeries(tuple._1)
        println("categ => " + tuple._1 + " : ")
        /*timeRepresentations.foreach {
          time: java.lang.Long =>*/
            tuple._2.foreach {
              tupleValue: (String, Long) =>
                //if (time == tupleValue._2) {
                  xyseries.add(tupleValue._2, nodes.indexOf(tupleValue._1))
                  println("\t" + tupleValue._1 + "(" + tupleValue._2 + ")")
                //}
                /*else if (time.longValue() < tupleValue._2.longValue()) {
                  defaultCategoryDataset.addValue(null, tuple._1, time)
                }*/
            }
        dataset.addSeries(xyseries)
        //}
    }
  }

  def buildChart (): JFreeChart = {
    val jfreechart: JFreeChart = ChartFactory
      .createXYLineChart("VectorClock updates", "Times", "nodes", buildDataset(), PlotOrientation.VERTICAL, false, true,
                        false)
    /*val categoryplot: CategoryPlot = jfreechart.getPlot.asInstanceOf[CategoryPlot]
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
    categoryplot.setRangeAxis(symbolaxis)*/
    val xyplot: XYPlot = jfreechart.getPlot.asInstanceOf[XYPlot]
    val xylineandshaperenderer: XYLineAndShapeRenderer = xyplot.getRenderer.asInstanceOf[XYLineAndShapeRenderer]
    xylineandshaperenderer.setBaseShapesVisible(true)

    for (i <- 0 until gidCateg.intValue() + 1) {
      val rand = new Random
      val color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
      xyplot.getRenderer.setSeriesPaint(i, color)
    }

    val symbolaxis: SymbolAxis = new SymbolAxis("Nodes", nodes.toArray(List("").toArray))
    xyplot.setRangeAxis(symbolaxis)
    jfreechart
  }
}