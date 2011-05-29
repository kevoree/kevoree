package org.kevoree.experiment.trace.gui.alg

import org.kevoree.experiment.trace.TraceMessages
import javax.swing.{WindowConstants, JFrame}
import java.io.{FileOutputStream, BufferedOutputStream, File, InputStream}
import org.jfree.chart.{JFreeChart, ChartPanel}
import com.lowagie.text.pdf.{DefaultFontMapper, PdfTemplate, PdfContentByte, PdfWriter}
import com.lowagie.text.Rectangle
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D

object AppPath extends Application {


  var input: InputStream = this.getClass.getClassLoader.getResourceAsStream("./trace_out")
  var traces: TraceMessages.Traces = TraceMessages.Traces.parseFrom(input)
  var linkedTrace = TracePath.getPathFrom("kspark0", 3, traces)
  linkedTrace match {
    case Some(ltrace) => {
      println(ltrace.toString)
      val frame = new JFrame();
      frame.setSize(400, 400);

      val chart = new VectorClockSingleDisseminationChartScala(ltrace);
      val jchart = chart.buildChart()
      val chartPanel = new ChartPanel(jchart);
      chartPanel.setOpaque(false);
      frame.add(chartPanel);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);

      saveChartToPDF(jchart, "trace_out.pdf", 600, 600)

    }
    case None => println("Not found")
  }


  /**
   * Save chart as PDF file. Requires iText library.
   *
   * @param chart JFreeChart to save.
   * @param fileName Name of file to save chart in.
   * @param width Width of chart graphic.
   * @param height Height of chart graphic.
   * @throws Exception if failed.
   * @see <a href="http://www.lowagie.com/iText">iText</a>
   */
  def saveChartToPDF (chart: JFreeChart, fileName: String, width: java.lang.Integer, height: java.lang.Integer) {
    if (chart != null) {
      var out: BufferedOutputStream = null
      try {
        out = new BufferedOutputStream(new FileOutputStream(fileName))

        //convert chart to PDF with iText:
        val pagesize: Rectangle = new Rectangle(width.intValue(), height.intValue())
        val document: com.lowagie.text.Document = new com.lowagie.text.Document(pagesize, 50, 50, 50, 50)
        try {
          val writer: PdfWriter = PdfWriter.getInstance(document, out)
          document.addAuthor("JFreeChart")
          document.open()

          val cb: PdfContentByte = writer.getDirectContent
          val tp: PdfTemplate = cb.createTemplate(width.intValue(), height.intValue())
          val g2: Graphics2D = tp.createGraphics(width.intValue(), height.intValue(), new DefaultFontMapper())

          val r2D: Rectangle2D = new Rectangle2D.Double(0, 0, width.intValue(), height.intValue())
          chart.draw(g2, r2D, null)
          g2.dispose()
          cb.addTemplate(tp, 0, 0)
        } finally {
          document.close()
        }
      } finally {
        if (out != null) {
          out.close()
        }
      }
    } //else: input values not available
  }
}