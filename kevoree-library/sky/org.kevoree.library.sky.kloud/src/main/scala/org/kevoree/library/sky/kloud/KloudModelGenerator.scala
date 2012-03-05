package org.kevoree.library.sky.kloud

import org.kevoree.framework.KevoreeXmiHelper
import org.kevoree.tools.marShell.KevsEngine
import org.kevoree.KevoreeFactory
import org.kevoree.tools.aether.framework.AetherUtil
import java.io.{FileInputStream, BufferedReader, FileReader, File}
import java.util.jar.{JarEntry, JarFile}

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 16/02/12
 * Time: 17:24
 *
 * @author Erwan Daubert
 * @version 1.0
 */

object KloudModelGenerator extends App {

  if (args.length == 2) {

    var script = ""
    var outputModel = ""

    args.find(arg => arg.startsWith("script=")) match {
      case None => println("You need to provide the path of a Kevoree script file!")
      case Some(argScript) => {
        script = argScript.substring("script=".size, argScript.size)
        args.find(arg => arg.startsWith("output=")) match {
          case None => println("You need to give where the output model will be save!")
          case Some(argOuputModel) => {
            outputModel = argOuputModel.substring("output=".size, argOuputModel.size)
          }
        }
      }
    }


    val file: File = AetherUtil
      .resolveMavenArtifact("org.kevoree.library.model.sky", "org.kevoree.library.model", KevoreeFactory.getVersion, List("http://maven.kevoree.org/release", "http://maven.kevoree.org/snapshots"))



    val jar = new JarFile(file)
    val entry: JarEntry = jar.getJarEntry("KEV-INF/lib.kev")

    val model = KevoreeXmiHelper.loadStream(jar.getInputStream(entry))
    jar.close()
    if (model != null) {
      val modelOption = KevsEngine.executeScript(loadScript(new File(script)), model)
      if (modelOption.isDefined) {
        KevoreeXmiHelper.save(outputModel, modelOption.get)
      } else {
        println("Unable to apply the script from " + script)
      }
    }
  } else {
    println("We need at least one parameter which define the file that contains the kloud kevovree script configuration")
  }


  private def loadScript (file: File): String = {
    val fileReader = new BufferedReader(new FileReader(file))
    val scriptBuilder = new StringBuilder
    var line = fileReader.readLine()

    while (line != null) {
      scriptBuilder append line + "\n"
      line = fileReader.readLine()
    }

    fileReader.close()
    println("try to apply the following script:\n" + scriptBuilder.toString())
    scriptBuilder.toString()
  }
}