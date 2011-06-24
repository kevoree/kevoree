package org.kevoree.experiment.smartbuilding.benchmark

import org.kevoree.library.arduinoNodeType.{ArduinoGuiProgressBar, ArduinoNode}
import org.wayoda.ang.project.TargetDirectoryService
import org.kevoree.{ContainerRoot, KevoreeFactory}
import org.kevoree.kompare.KevoreeKompareBean
import collection.mutable.HashMap
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.{FileOutputStream, File}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * User: ffouquet
 * Date: 23/06/11
 * Time: 10:26
 */

abstract class AbstractExperiment {

  var kompare: KevoreeKompareBean = new KevoreeKompareBean

  def initNode(knodeName: String, model: ContainerRoot) = {
    val baseTime = System.currentTimeMillis()
    val node: ArduinoNode = new ArduinoNode
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
  }

  def init()

  def runExperiment(_twa: TwoWayActors)

  private val values: HashMap[Int, HashMap[String, Int]] = new HashMap[Int, HashMap[String, Int]]

  def addToRaw(index: Int, typeID: String, value: Int) {
    val valueMap = values.get(index).getOrElse {
      val inMap = new HashMap[String, Int];
      values.put(index, inMap);
      inMap
    }
    valueMap.put(typeID, value)
    values.put(index, valueMap)
  }

  def saveRawDump() {

    var headers: List[String] = List()

    val wb = new HSSFWorkbook
    val sheet = wb.createSheet(this.getClass.getName)
    values.foreach {
      v =>
        val row = sheet.createRow(v._1 + 1)
        v._2.foreach {
          v2 =>
            if (!headers.contains(v2._1)) {
              headers = headers ++ List(v2._1)
            }
            val cell = row.createCell(1 + headers.indexOf(v2._1))
            cell.setCellValue(v2._2)
            cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC)
          //val namedCell = wb.createName();
          //namedCell.setNameName(v2._1);
          //namedCell.setRefersToFormula(new CellReference(cell).formatAsString())
        }
        val cell = row.createCell(0)
        cell.setCellValue(v._1)
        cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC)

    }
    //ADD HEADER
    val headerRow = sheet.createRow(0)
    headers.foreach {
      header =>
        val cell = headerRow.createCell(1 + headers.indexOf(header))
        cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING)
        cell.setCellValue(header)
    }
    val cell = headerRow.createCell(0)
    cell.setCellType(org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING)
    cell.setCellValue("STEP")

    val fo = new FileOutputStream(this.getClass.getName + ".xls")
    wb.write(fo)
    fo.close()

  }

  def saveRowImage() {
    val size = SmartSensorsGUI.getPanel.getSize()
    val bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
    val g2 = bi.createGraphics();
    SmartSensorsGUI.getPanel.paint(g2);
    ImageIO.write(bi, "PNG", new File(this.getClass.getName+".png"))
  }


}