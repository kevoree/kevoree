package org.kevoree.library.sky.api.property

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/07/12
 * Time: 10:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object PropertyConversionHelper {

  @throws(classOf[NumberFormatException])
  def getRAM (ram: String): Long = {
    if (ram.toLowerCase.endsWith("gb")) {
      Integer.parseInt(ram.substring(0, ram.length - 2)) * 1024 * 1024 * 1024
    } else if (ram.toLowerCase.endsWith("mb")) {
      Integer.parseInt(ram.substring(0, ram.length - 2)) * 1024 * 1024
    } else if (ram.toLowerCase.endsWith("kb")) {
      Integer.parseInt(ram.substring(0, ram.length - 2)) * 1024 * 1024
    } else {
      Integer.parseInt(ram)
    }
  }

  @throws(classOf[NumberFormatException])
  def getCPUFrequency (cpu: String): Long = {
    if (cpu.toLowerCase.endsWith("ghz")) {
      Integer.parseInt(cpu.substring(0, cpu.length - 3)) * 1024 * 1024 * 1024
    } else if (cpu.toLowerCase.endsWith("mhz")) {
      Integer.parseInt(cpu.substring(0, cpu.length - 3)) * 1024 * 1024
    } else if (cpu.toLowerCase.endsWith("khz")) {
      Integer.parseInt(cpu.substring(0, cpu.length - 3)) * 1024 * 1024
    } else {
      Integer.parseInt(cpu)
    }
  }

  @throws(classOf[NumberFormatException])
  def getDataSize (dataSize: String): Long = {
    if (dataSize.toLowerCase.endsWith("gb")) {
      Integer.parseInt(dataSize.substring(0, dataSize.length - 2)) * 1024 * 1024 * 1024
    } else if (dataSize.toLowerCase.endsWith("mb")) {
      Integer.parseInt(dataSize.substring(0, dataSize.length - 2)) * 1024 * 1024
    } else if (dataSize.toLowerCase.endsWith("kb")) {
      Integer.parseInt(dataSize.substring(0, dataSize.length - 2)) * 1024 * 1024
    } else {
      Integer.parseInt(dataSize)
    }
  }

}
