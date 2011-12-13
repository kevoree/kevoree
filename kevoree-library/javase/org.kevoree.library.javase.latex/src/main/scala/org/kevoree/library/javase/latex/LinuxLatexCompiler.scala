package org.kevoree.library.javase.latex

import org.slf4j.LoggerFactory
import actors.{TIMEOUT, Actor}
import util.matching.Regex
import java.io._
import java.lang.ProcessBuilder
import scala.collection.JavaConversions._

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 18/11/11
 * Time: 10:00
 *
 * @author Erwan Daubert
 * @version 1.0
 */

class LinuxLatexCompiler extends LatexCompilerInterface {

  private val logger = LoggerFactory.getLogger(this.getClass.getName)
  private val resultActor = new ResultManagementActor()

  private val errorLatexRegex = new Regex(".*LaTeX Error:.*")
  private val errorBibtexRegex = new Regex("I was expecting.*")
  private val warningLatexRegex = new Regex(".*LaTeX Warning:.*")
  private val warningBibtexRegex = new Regex("Warning.*")
  //  private val latexAvailabilityRegex = new Regex("pdflatex: /.*")
  //  private val bibtexAvailabilityRegex = new Regex("bibtex: /.*")
  //  private val latexAvailabilityErrorRegex = new Regex("pdflatex:")
  //  private val bibtexAvailabilityErrorRegex = new Regex("bibtex:")


  private val latexAvailabilityRegex = new Regex("/.*")
  private val bibtexAvailabilityRegex = new Regex("/.*")
  private val AvailabilityErrorRegex1 = new Regex("which: no")

  private var absolutePdfLatex = "pdflatex"
  private var absoluteBibtex = "bibtex"

  def isAvailable: Boolean = {
    val builder = new ProcessBuilder()

    builder.command("which", "pdflatex")
    var p = builder.start()
    resultActor.starting()
    new Thread(new
        ProcessStreamManager(p.getInputStream, Array(latexAvailabilityRegex),
                              Array(AvailabilityErrorRegex1)))
      .start()
    val isAvailable1 = resultActor.waitingForPath(2000)

    builder.command("which", "bibtex")
    p = builder.start()
    resultActor.starting()
    new Thread(new
        ProcessStreamManager(p.getInputStream, Array(bibtexAvailabilityRegex),
                              Array(AvailabilityErrorRegex1)))
      .start()
    val isAvailable2 = resultActor.waitingForPath(2000)
    if (isAvailable1._1 && isAvailable2._1) {
      absolutePdfLatex = isAvailable1._2.trim()
      absoluteBibtex = isAvailable2._2.trim()
      true
    } else {
      false
    }

  }

  def clean (folder: String) {

    val builder = new ProcessBuilder()
    builder.directory(new File(folder))

    builder.command("sh", "-c", "rm -f *.aux")

    val p1 = builder.start()
   // builder.command("sh", "-c", "rm -f *.log")
   // val p2 = builder.start()
    //    builder.command("bash", "-c", "rm -f *.pdf")
    //    val p3 = builder.start()
    //    builder.command("bash", "-c", "rm -f *.dvi")
    //    val p4 = builder.start()
    builder.command("sh", "-c", "rm -f *.toc")
    val p5 = builder.start()
    builder.command("sh", "-c", "rm -f *.bbl")
    val p6 = builder.start()
    builder.command("sh", "-c", "rm -f *.blg")
    val p7 = builder.start()
    builder.command("sh", "-c", "rm -f *.out")
    val p8 = builder.start()

    p1.waitFor()
    //p2.waitFor()
    //    p3.waitFor()
    //    p4.waitFor()
    p5.waitFor()
    p6.waitFor()
    p7.waitFor()
    p8.waitFor()
  }

  def compile (file: String, folder: String): String = {
    var f = file
    if (file.endsWith(".tex")) {
      f = f.substring(0, file.length() - ".tex".length())
    }

    clean(folder)

    val builderPdfLatex = new ProcessBuilder()
    builderPdfLatex.directory(new File(folder))

    builderPdfLatex.command(absolutePdfLatex, "-halt-on-error", "-file-line-error", "-output-directory", folder, f)

    val builderBibtex = new ProcessBuilder()
    builderBibtex.directory(new File(folder))

    builderBibtex.command(absoluteBibtex, f)

    var p = builderPdfLatex.start()
    resultActor.starting()
    new Thread(new
        ProcessStreamManager(p.getInputStream, Array(warningLatexRegex), Array(errorLatexRegex)))
      .start()
    val isAvailable1 = resultActor.waitingFor(10000)

    p = builderBibtex.start()
    resultActor.starting()
    new Thread(new
        ProcessStreamManager(p.getInputStream, Array(warningBibtexRegex), Array(errorBibtexRegex)))
      .start()
    val isAvailable2 = resultActor.waitingFor(10000)

    p = builderPdfLatex.start()
    resultActor.starting()
    new Thread(new
        ProcessStreamManager(p.getInputStream, Array(warningLatexRegex), Array(errorLatexRegex)))
      .start()
    val isAvailable3 = resultActor.waitingFor(10000)

    p = builderPdfLatex.start()
    resultActor.starting()
    new Thread(new
        ProcessStreamManager(p.getInputStream, Array(warningLatexRegex), Array(errorLatexRegex)))
      .start()
    val isAvailable4 = resultActor.waitingFor(10000)

    clean(folder)

    if (isAvailable1._1 && isAvailable2._1 && isAvailable3._1 && isAvailable4._1) {
      isAvailable2._2 + "\n" + isAvailable4._2 + "\nBuild success!"
    } else {
      isAvailable2._2 + "\n" + isAvailable4._2 + "\nBuild failed!"
    }
  }

  class ProcessStreamManager (inputStream: InputStream, outputRegexes: Array[Regex], errorRegexes: Array[Regex])
    extends Runnable {

    override def run () {
      val outputBuilder = new StringBuilder
      var errorBuilder = false
      try {
        val reader = new BufferedReader(new InputStreamReader(inputStream))
        var line = reader.readLine()
        while (line != null) {
          
          outputRegexes.find(regex => line match {
            case regex() => true
            case _ => false
          }) match {
            case Some(regex) => outputBuilder.append(line + "\n")
            case None =>
          }
          errorRegexes.find(regex => line match {
            case regex() => true
            case _ => false
          }) match {
            case Some(regex) => errorBuilder = true; outputBuilder.append(line + "\n")
            case None =>
          }
          line = reader.readLine()
        }
      } catch {
        case _@e => {
        }
      }
      if (errorBuilder) {
        resultActor.error(outputBuilder.toString())
      } else {
        resultActor.output(outputBuilder.toString())
        
      }
    }
  }

  class ResultManagementActor () extends Actor {

    case class STOP ()

    case class WAITINGFOR (timeout: Int)

    case class WAITINGFORPATH (timeout: Int)

    case class STARTING ()

    sealed abstract case class Result ()

    case class OUTPUT (data: String) extends Result

    case class ERROR (data: String) extends Result

    start()

    def stop () {
      this ! STOP()
    }

    def starting () {
      this ! STARTING()
    }

    def waitingFor (timeout: Int): (Boolean, String) = {
      (this !? WAITINGFOR(timeout)).asInstanceOf[(Boolean, String)]
    }

    def waitingForPath (timeout: Int): (Boolean, String) = {
      (this !? WAITINGFORPATH(timeout)).asInstanceOf[(Boolean, String)]
    }


    def output (data: String) {
      this ! OUTPUT(data)
    }

    def error (data: String) {
      this ! ERROR(data)
    }

    var firstSender = null

    def act () {
      loop {
        react {
          case STOP() => this.exit()
          case ERROR(data) =>
          case OUTPUT(data) =>
          case STARTING() => {
            var firstSender = this.sender
            react {
              case STOP() => this.exit()
              case WAITINGFOR(timeout) => {
                firstSender = this.sender
                reactWithin(timeout) {
                  case STOP() => this.exit()
                  case OUTPUT(data) => firstSender !(true, data)
                  case TIMEOUT => firstSender !(false, "Timeout exceeds. Maybe the compilation is not able to finished")
                  case ERROR(data) => firstSender !(false, data)
                }
              }
              case WAITINGFORPATH(timeout) => {
                firstSender = this.sender
                reactWithin(timeout) {
                  case STOP() => this.exit()
                  case OUTPUT(data) => firstSender !(true, data)
                  case TIMEOUT => firstSender !(false, "Timeout exceeds. Maybe the compilation is not able to finished")
                  case ERROR(data) => firstSender !(false, data)
                }
              }
              case OUTPUT(data) => {
                react {
                  case STOP() => this.exit()
                  case WAITINGFOR(timeout) => firstSender !(true, data)
                  case WAITINGFORPATH(timeout) => firstSender !(true, data)
                }
              }
              case ERROR(data) => {
                react {
                  case STOP() => this.exit()
                  case WAITINGFOR(timeout) => firstSender !(false, data)
                  case WAITINGFORPATH(timeout) => firstSender !(true, data)
                }
              }
            }
          }
        }
      }
    }
  }

}