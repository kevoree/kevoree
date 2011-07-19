package org.kevoree.experiment.trace.gui.alg

import javax.swing.event.{ChangeEvent, ChangeListener}
import org.jfree.chart.{JFreeChart, ChartPanel}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/05/11
 * Time: 11:40
 */

class ScrollableChartPanel(chart : JFreeChart) extends ChartPanel(chart) with ChangeListener {
  def stateChanged (e: ChangeEvent) {

  }
}