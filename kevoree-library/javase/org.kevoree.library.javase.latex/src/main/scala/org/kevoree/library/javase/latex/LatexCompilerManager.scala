package org.kevoree.library.javase.latex

import actors.DaemonActor
import java.lang.String

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/11/11
 * Time: 13:18
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class LatexCompilerManager extends DaemonActor with LatexCompilerInterface {

  start()

  def compile (file: String, folder: String): String = {
    (this !? COMPILE(file, folder)).asInstanceOf[String]
  }

  def isAvailable: Boolean = {
    (this !? AVAILABILITY()).asInstanceOf[Boolean]
  }

  def clean (folder: String) {
    this ! CLEAN(folder)
  }

  def stop () {
    this ! STOP()
  }

  def act () {
    loop {
      react {
        case STOP() => this.exit()
        case CLEAN(folder) => cleanInternals(folder)
        case AVAILABILITY() => reply(isAvailableInternals)
        case COMPILE(file, folder) => reply(compileInternals(file, folder))

      }
    }
  }

  private def isAvailableInternals: Boolean = {
    if (isWindows) {
      false
    } else if (isUnix || isMac) {
      val latexCompiler = new LinuxLatexCompiler
      latexCompiler.isAvailable
    } else {
      throw new Exception("Unknown Operating System. Unable to check if latex is available")
    }
  }

  private def cleanInternals (folder: String) {
    if (isWindows) {
      false
    } else if (isUnix || isMac) {
      val latexCompiler = new LinuxLatexCompiler
      latexCompiler.clean(folder)
    } else {
      throw new Exception("Unknown Operating System. Unable to check if latex is available")
    }
  }

  private def compileInternals (file: String, folder: String): String = {
    if (isWindows) {
      ""
    } else if (isUnix || isMac) {
      val latexCompiler = new LinuxLatexCompiler
      if (!latexCompiler.isAvailable) {
        throw new
            Exception("Unable to find required software (pdflatex and/or bibtex). Please check the configuration of your system")
      } else {
        latexCompiler.compile(file, folder)
      }
    } else {
      throw new Exception("Unknown Operating System. Unable to check if latex is available")
    }
  }

  def isWindows: Boolean = {
    val os: String = System.getProperty("os.name").toLowerCase
    (os.contains("win"))
  }

  def isMac: Boolean = {
    val os: String = System.getProperty("os.name").toLowerCase
    (os.contains("mac"))
  }

  def isUnix: Boolean = {
    val os: String = System.getProperty("os.name").toLowerCase
    (os.contains("nix") || os.contains("nux"))
  }
}