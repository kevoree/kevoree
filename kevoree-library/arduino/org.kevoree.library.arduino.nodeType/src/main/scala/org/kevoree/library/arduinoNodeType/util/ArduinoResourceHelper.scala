package org.kevoree.library.arduinoNodeType.util

import java.util.zip.ZipFile
import java.io._
import collection.JavaConversions._
import org.kevoree.api.Bootstraper

object ArduinoResourceHelper {
  private var basePath: String = null;
  
  private var boot : Bootstraper  = null
  def setBs(b : Bootstraper) {
    boot = b 
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

  def is64: Boolean = {
    val os: String = System.getProperty("os.arch").toLowerCase
    (os.contains("64"))
  }

  def copyInputStream (in: InputStream, out: OutputStream) {
    val buffer: Array[Byte] = new Array[Byte](1024)
    var len: Int = 0
    while ((({
      len = in.read(buffer);
      len
    })) >= 0) {
      out.write(buffer, 0, len)
    }
    in.close()
    out.flush()
    out.close()
  }

  private def unzip (filePath: String, outputLocation: String) {
    try {
      val zipFile: ZipFile = new ZipFile(filePath)
      val entries = zipFile.entries()
      while (entries.hasMoreElements) {
        val entry = entries.nextElement
        if (entry.isDirectory) {
          new File(outputLocation + File.separator + entry.getName).mkdir
        }
        else {
          copyInputStream(zipFile.getInputStream(entry), new
              BufferedOutputStream(new FileOutputStream(outputLocation + File.separator + entry.getName)))
          new File(outputLocation + File.separator + entry.getName).setExecutable(true)
        }
      }
      zipFile.close()
    }
    catch {
      case ioe: IOException => {
        ioe.printStackTrace()
      }
    }
  }

  private def copyAvrDude () {
    val f = new File(basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "tools" +
      File.separator + "avrdude.conf")
    if (f.exists()) {
      new File(basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator +
        "tools" + File.separator + "avr" + File.separator + "etc").mkdirs()
      copyInputStream(new FileInputStream(f), new
          FileOutputStream(basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator +
            "tools" + File.separator + "avr" + File.separator + "etc" + File.separator + "avrdude.conf"))
    }
  }

  def getIncludePaths: java.util.List[String] = {
    if (basePath == null) {
      extractArduinoResources
    }
    var paths = List[String]()
    paths = paths ++
      List[String](basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "tools" +
        File.separator + "avr" + File.separator + "include")
    paths = paths ++ List[String](basePath + File.separator + "arduino" + File.separator + "libraries")

    //TODO BETTER CHOICE

   // println(basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "variants" + File.separator +"standard")
    
    paths = paths ++ List[String](basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "arduino" + File.separator + "variants" + File.separator +"standard")

    paths
  }

  def getLibraryLocation: java.util.List[String] = {
    if (basePath == null) {
      extractArduinoResources
    }
    var paths = List[String]()
    paths = paths ++
      List[String](basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "tools" +
        File.separator + "avr" + File.separator + "lib")
    paths
  }

  def getArduinoHome: String = {
    if (basePath == null) {
      extractArduinoResources;
    }

    basePath + File.separator + "arduino";
  }

  def extractArduinoResources {
    // use aether to get the correspond artifact : extra avr-arduino
    var repositories = List[String]()
    repositories = repositories ++ List[String]("http://maven.kevoree.org/release");
    repositories = repositories ++ List[String]("http://maven.kevoree.org/snapshots");
    //		scala.collection.immutable.List<String> list = scala.collection.immutable.List.apply(repositories);
    var artifactId = "org.kevoree.extra.avr-arduino"
    if (isMac) {
      artifactId = artifactId + ".osx"
    } else if (isWindows) {
     // if (is64) {
        artifactId = artifactId + ".win64"
     // } else {
     //   artifactId = artifactId + ".win"
     // }
    } else if (isUnix) {
      if (is64) {
        artifactId = artifactId + ".nix64"
      } else {
        artifactId = artifactId + ".nix"
      }
    }
    val arteFile = boot.resolveArtifact(artifactId, "org.kevoree.extra", "1.0", repositories);
    // unzip it on /tmp
    val f = File.createTempFile("arduino_resources", "")
    f.delete()
    f.mkdirs()
    f.deleteOnExit()
    unzip(arteFile.getAbsolutePath, f.getAbsolutePath)
    basePath = f.getAbsolutePath

    copyAvrDude()
  }

  def getBinaryLocation: java.util.List[String] = {
    if (basePath == null) {
      extractArduinoResources
    }
    // TODO test if basePath is defined

    var paths = List[String]()
    paths = paths ++
      List[String](basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "tools" +
        File.separator + "bin")
    paths = paths ++
      List[String](basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "tools" +
        File.separator + "avr" + File.separator + "bin")
    paths = paths ++
      List[String](basePath + File.separator + "arduino" + File.separator + "hardware" + File.separator + "tools" +
        File.separator + "libexec" + File.separator + "gcc" + File.separator + "avr" +
        File.separator + "4.3.2")
    paths
  }
}
