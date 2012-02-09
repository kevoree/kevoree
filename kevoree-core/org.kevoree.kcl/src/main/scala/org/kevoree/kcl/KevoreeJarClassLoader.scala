package org.kevoree.kcl

/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.xeustechnologies.jcl.JarClassLoader
import java.net.URL
import java.lang.{Class, String}
import ref.WeakReference
import org.slf4j.LoggerFactory
import java.io._
import java.util.{ArrayList, Collections, Enumeration}

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 18:57
 */

class KevoreeJarClassLoader extends JarClassLoader {

  def getLoadedURLs: java.util.List[URL] = {
    import scala.collection.JavaConversions._
    classpathResources.asInstanceOf[KevoreeLazyJarResources].getLoadedURLs
  }

  var specialloaders: List[KevoreeResourcesLoader] = List()

  def addSpecialLoaders(l: KevoreeResourcesLoader) {
    specialloaders = specialloaders ++ List(l)
  }

  def getSpecialLoaders = specialloaders

  private var locked = false

  def lockLinks() {
    locked = true
  }

  private val logger = LoggerFactory.getLogger(classOf[KevoreeJarClassLoader]);

  def cleanupLinks(c: ClassLoader) {
    // CHEKC USED
    subClassLoaders = subClassLoaders.filter(scl => scl != c)
    subWeakClassLoaders = subWeakClassLoaders.filter(scl => scl != c)
  }

  classpathResources = new KevoreeLazyJarResources
  classpathResources.asInstanceOf[KevoreeLazyJarResources].setParentKCL(this)

  def setLazyLoad(lazyload: Boolean) {
    classpathResources.asInstanceOf[KevoreeLazyJarResources].setLazyLoad(lazyload)
  }

  protected var subClassLoaders = List[ClassLoader]()

  def addSubClassLoader(cl: ClassLoader) {
    if (!locked) {
      if (!subClassLoaders.exists(scl => scl == cl)) {
        if (!subWeakClassLoaders.exists(scl => scl.get.isDefined && scl.get.get == cl)) {
          subClassLoaders = subClassLoaders ++ List(cl)
        }
      }
    }
  }

  protected var subWeakClassLoaders = List[WeakReference[ClassLoader]]()

  def addWeakClassLoader(wcl: ClassLoader) {
    if (!locked) {
      if (!subClassLoaders.exists(scl => scl == wcl)) {
        if (!subWeakClassLoaders.exists(scl => scl.get.isDefined && scl.get.get == wcl)) {
          subWeakClassLoaders = subWeakClassLoaders ++ List(new WeakReference[ClassLoader](wcl))
        }
      }
    }
  }

  protected def callSuperConcreteLoader(className: String, resolveIt: Boolean): Class[_] = {
    super[JarClassLoader].loadClass(className, resolveIt)
  }


  override def loadClass(className: String, resolveIt: Boolean): Class[_] = {

    this.synchronized {
      try {
        return callSuperConcreteLoader(className, resolveIt)
      } catch {
        case nf: ClassNotFoundException =>
      }
      if (resolveIt) {
        subClassLoaders.foreach {
          subCL =>
            try {
              return subCL.loadClass(className)
            } catch {
              case nf: ClassNotFoundException =>
            }
        }
        subWeakClassLoaders.foreach {
          subCL =>
            try {
              subCL.get.map {
                m =>
                  if (m.isInstanceOf[KevoreeJarClassLoader]) {
                    return m.asInstanceOf[KevoreeJarClassLoader].loadClass(className, false)
                  }
              }
            } catch {
              case nf: ClassNotFoundException =>
            }
        }
      }

      throw new ClassNotFoundException(className)
    }


  }

  override def loadClass(className: String): Class[_] = {
    // println("loadClass->" + className)
    loadClass(className, true)
  }

  override def getResourceAsStream(name: String): InputStream = {
    logger.debug("Get Ressource : " + name)
    var res: Array[Byte] = null
    if (name.endsWith(".class")) {
      res = this.classpathResources.getResource(name)
    } else {
      val url = this.classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceURL(name)
      if (url != null) {
        res = this.classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceContent(url)
      }
    }
    if (res != null) {
      new ByteArrayInputStream(res)
    } else {
      null
    }
  }

  override def getResource(s: String): URL = {
    internal_getResource(s)
  }

  def internal_getResource(s: String): URL = {
    if (classpathResources.asInstanceOf[KevoreeLazyJarResources].containResource(s)) {
      if (classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceURL(s).toString.startsWith("file:kclstream:")) {
        val cleanName = if (s.contains("/")) {
          s.substring(s.lastIndexOf("/") + 1)
        } else {
          s
        }
        val tFile = File.createTempFile("dummy_kcl_temp", cleanName)
        tFile.deleteOnExit()
        val tWriter = new FileOutputStream(tFile)
        tWriter.write(classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceContent(classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceURL(s)))
        tWriter.close()
        new URL("file:///" + tFile.getAbsolutePath)
      } else {
        classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceURL(s)
      }
    } else {
      logger.debug("getResource not found null=>" + s + " in " + classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar)
      null
    }
  }

  def unload() {
    import scala.collection.JavaConversions._
    (this.getLoadedClasses.keySet().toList ++ List()).foreach {
      lc =>
        unloadClass(lc)
    }
  }

  override def findResource(s: java.lang.String): java.net.URL = {
    internal_getResource(s)
  }

  override def findResources(p1: String): Enumeration[URL] = {
    if (classpathResources.asInstanceOf[KevoreeLazyJarResources].containResource(p1)) {
      val urls = classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceURLS(p1)
      val resolvedUrl = new ArrayList[URL]
      import scala.collection.JavaConversions._
      urls.foreach {
        u =>
          if (u.toString.startsWith("file:kclstream:")) {
            val cleanName = if (p1.contains("/")) {
              p1.substring(p1.lastIndexOf("/") + 1)
            } else {
              p1
            }
            val tFile = File.createTempFile("dummy_kcl_temp", cleanName)
            println(cleanName + "->" + tFile.getAbsolutePath)
            tFile.deleteOnExit()
            val tWriter = new FileOutputStream(tFile)
            tWriter.write(classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceContent(u))
            tWriter.close()
            resolvedUrl.add(new URL("file:///" + tFile.getAbsolutePath))
          } else {
            resolvedUrl.add(u)
          }
      }
      Collections.enumeration(resolvedUrl);
    } else {
      Collections.enumeration(new java.util.ArrayList[URL]())
    }
  }


  def printDump() {
    logger.debug("KCL : " + classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar)
    subClassLoaders.foreach {
      s =>
        logger.debug("    l->" + s.asInstanceOf[KevoreeJarClassLoader].classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar + "_" + s.hashCode())
    }

    subWeakClassLoaders.foreach {
      s =>
        if (s.get.isDefined) {
          logger.debug("    w~>" + s.get.get.asInstanceOf[KevoreeJarClassLoader].classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar + "_" + s.get.get.hashCode())
        }
    }
  }

}