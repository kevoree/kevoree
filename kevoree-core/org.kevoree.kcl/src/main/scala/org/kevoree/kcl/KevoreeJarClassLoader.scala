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
/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kevoree.kcl


import org.xeustechnologies.jcl.JarClassLoader
import java.net.URL
import java.lang.{Class, String}
import ref.WeakReference
import org.slf4j.LoggerFactory
import java.io._
import java.util.{HashMap, ArrayList, Collections, Enumeration}
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 18:57
 */

class KevoreeJarClassLoader extends JarClassLoader {

  private val nativeMap = new HashMap[String, String]();

  def addNativeMapping(name: String, url: String) {
    nativeMap.put(name, url)
  }

  override def findLibrary(p1: String): String = {
    if (nativeMap.containsKey(p1)) {
      nativeMap.get(p1)
    } else {
      super.findLibrary(p1)
    }
  }

  def getLoadedURLs: java.util.List[URL] = {
    import scala.collection.JavaConversions._
    classpathResources.asInstanceOf[KevoreeLazyJarResources].getLoadedURLs
  }

  def getLinkedLoadedURLs(): java.util.List[URL] = {
    val resultURL = new ArrayList[URL]()
    val alreadyPassed = new ArrayList[ClassLoader]()
    internal_getAllLoadedURLs(resultURL, alreadyPassed)
    resultURL
  }


  def internal_getAllLoadedURLs(res: java.util.List[URL], cls: java.util.List[ClassLoader]): Unit = {
    import scala.collection.JavaConversions._
    //var res: java.util.List[URL] = new java.util.ArrayList[URL]()
    cls.add(this)
    res.addAll(classpathResources.asInstanceOf[KevoreeLazyJarResources].getLoadedURLs)
    subClassLoaders.foreach(l => {
      if (l.isInstanceOf[KevoreeJarClassLoader] && !cls.contains(l)) {
        l.asInstanceOf[KevoreeJarClassLoader].internal_getAllLoadedURLs(res, cls)
      }
    }
    )
    /*
        subWeakClassLoaders.foreach(l => {
          if (l.get.get.isInstanceOf[KevoreeJarClassLoader] && !cls.contains(l.get.get)) {
            l.get.get.asInstanceOf[KevoreeJarClassLoader].getAllLoadedURLs(res, cls)
          }
        })
    */
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
    // CHECK USED
    subClassLoaders = subClassLoaders.filter(scl => scl != c)
    subWeakClassLoaders = subWeakClassLoaders.filter(scl => scl.get.isDefined && scl.get.get != c)
  }

  classpathResources = new KevoreeLazyJarResources
  classpathResources.asInstanceOf[KevoreeLazyJarResources].setParentKCL(this)

  def setLazyLoad(lazyload: Boolean) {
    classpathResources.asInstanceOf[KevoreeLazyJarResources].setLazyLoad(lazyload)
  }

  protected var subClassLoaders = List[ClassLoader]()

  def getSubClassLoaders() = subClassLoaders

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
    val result = internal_loadClass(className, resolveIt)
    if (result == null) {
      throw new ClassNotFoundException(className)
    }
    result
  }

  private val scoreMap = new ConcurrentHashMap[Int,Int]

  def getScore(kcl : ClassLoader):Int={
    if(scoreMap.containsKey(kcl.hashCode())){
      scoreMap.get(kcl.hashCode())
    } else {
      0
    }
  }
  def incScore(kcl : ClassLoader):Int={
    scoreMap.put(kcl.hashCode(),getScore(kcl)+1)
  }

  def internal_loadClass(className: String, resolveIt: Boolean): Class[_] = {
    var result: Class[_] = null
    result = callSuperConcreteLoader(className, resolveIt)
    if (result != null) {
      return result
    }
    if (resolveIt) {
      /*
      println("Score =")
      subClassLoaders.sortWith((e1, e2) => (getScore(e1) > getScore(e2))).foreach{
        sub => println(getScore(sub)+"-"+sub.toString)
      }
      println("---")
      */
      subClassLoaders.sortWith((e1, e2) => (getScore(e1) > getScore(e2))).foreach {
        subCL =>
          result = subCL match {
            case kcl: KevoreeJarClassLoader => kcl.internal_loadClass(className, false)
            case _ => {
              try {
                subCL.loadClass(className)
              } catch {
                case nf: ClassNotFoundException => null
              }
            }
          }
          if (result != null) {
            incScore(subCL)
            return result
          }
      }
      subWeakClassLoaders.filter(p => p.get.isDefined).sortWith((e1, e2) => ( getScore(e1.get.get) < getScore(e2.get.get))).foreach {
        subCL =>
          try {
            subCL.get.map {
              m =>
                result = m match {
                  case kcl: KevoreeJarClassLoader => kcl.internal_loadClass(className, false)
                  case _ => {
                    try {
                      m.loadClass(className)
                    } catch {
                      case nf: ClassNotFoundException => null
                    }
                  }
                }
                if (result != null) {
                  incScore(m)
                  return result
                }
            }
          } catch {
            case nf: ClassNotFoundException =>
          }
      }
    }
    result
  }

  override def loadClass(className: String): Class[_] = {
    loadClass(className, true)
  }

  override def getResourceAsStream(name: String): InputStream = {
    var resolved = internal_getResourceAsStream(name)
    if (resolved != null) {
      return resolved
    }
    subClassLoaders.foreach {
      sub =>
        resolved = if (sub.isInstanceOf[KevoreeJarClassLoader]) {
          sub.asInstanceOf[KevoreeJarClassLoader].internal_getResourceAsStream(name)
        } else {
          sub.getResourceAsStream(name)
        }
        if (resolved != null) {
          return resolved
        }
    }
    subWeakClassLoaders.foreach {
      subOpt =>
        if (subOpt.get.isDefined) {
          val sub = subOpt.get.get
          resolved = if (sub.isInstanceOf[KevoreeJarClassLoader]) {
            sub.asInstanceOf[KevoreeJarClassLoader].internal_getResourceAsStream(name)
          } else {
            sub.getResourceAsStream(name)
          }
          if (resolved != null) {
            return resolved
          }
        }
    }
    resolved
  }


  def internal_getResourceAsStream(name: String): InputStream = {
    //logger.debug("Get RessourceAsStream : " + name)
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
      //logger.debug("Not found res " + name + " in " + this)
      null
    }
  }

  override def getResource(s: String): URL = {
    //println("GetResource "+s+" - "+findResource(s))
    findResource(s)
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
        //SIMPLY RETURN URL
        classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceURL(s)
      }
    } else {
      //logger.debug("getResource not found null=>" + s + " in " + classpathResources.asInstanceOf[KevoreeLazyJarResources].getClass)

      //logger.debug("getResource not found null=>" + s + " in " + classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar)
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
    var urlInternal: URL = internal_getResource(s)
    if (urlInternal == null) {
      subClassLoaders.foreach {
        sub =>
          urlInternal = if (sub.isInstanceOf[KevoreeJarClassLoader]) {
            sub.asInstanceOf[KevoreeJarClassLoader].internal_getResource(s)
          } else {
            sub.getResource(s)
          }
          if (urlInternal != null) {
            return urlInternal
          }
      }
      subWeakClassLoaders.foreach {
        subOpt =>

          if (subOpt.get.isDefined) {
            val sub = subOpt.get.get
            urlInternal = if (sub.isInstanceOf[KevoreeJarClassLoader]) {
              sub.asInstanceOf[KevoreeJarClassLoader].internal_getResource(s)
            } else {
              sub.getResource(s)
            }
            if (urlInternal != null) {
              return urlInternal
            }
          }
      }
      null
    } else {
      urlInternal
    }
  }


  def internal_findResources(p1: String): java.util.ArrayList[URL] = {
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
            tFile.deleteOnExit()
            val tWriter = new FileOutputStream(tFile)
            tWriter.write(classpathResources.asInstanceOf[KevoreeLazyJarResources].getResourceContent(u))
            tWriter.close()
            resolvedUrl.add(new URL("file:///" + tFile.getAbsolutePath))
          } else {
            resolvedUrl.add(u)
          }
      }
      resolvedUrl
    } else {
      new java.util.ArrayList[URL]()
    }
  }

  override def findResources(p1: String): Enumeration[URL] = {
    //logger.debug("CallFind Resources for " + p1 + "-")
    val selfRes: ArrayList[URL] = internal_findResources(p1)
    //Then call on all
    subClassLoaders.foreach {
      sub =>
        val subEnum = if (sub.isInstanceOf[KevoreeJarClassLoader]) {
          Collections.enumeration(sub.asInstanceOf[KevoreeJarClassLoader].internal_findResources(p1))
        } else {
          sub.getResources(p1)
        }
        while (subEnum.hasMoreElements) {
          val subElem = subEnum.nextElement();
          if (!selfRes.contains(subElem)) {
            selfRes.add(subElem)
          }
        }
    }
    subWeakClassLoaders.foreach {
      subOpt =>
        if (subOpt.get.isDefined) {
          val sub = subOpt.get.get
          val subEnum = if (sub.isInstanceOf[KevoreeJarClassLoader]) {
            Collections.enumeration(sub.asInstanceOf[KevoreeJarClassLoader].internal_findResources(p1))
          } else {
            sub.getResources(p1)
          }
          while (subEnum.hasMoreElements) {
            val subElem = subEnum.nextElement();
            if (!selfRes.contains(subElem)) {
              selfRes.add(subElem)
            }
          }
        }
    }
    /*
    logger.debug("ResourcesEnumSize={}",selfRes.size())
    if(logger.isDebugEnabled){
      selfRes.toArray.foreach{  u =>
        logger.debug("URL="+u)
      }
    }   */
    Collections.enumeration(selfRes)
  }

  def cleanJarURL(j: String) = {
    if (j.contains(File.separator)) {
      j.substring(j.lastIndexOf(File.separator) + 1)
    } else {
      j
    }
  }

  def getKCLDump: String = {
    val buffer = new StringBuffer
    buffer.append("\tJar=" + cleanJarURL(classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar) + "_" + hashCode() + "\n")
    subClassLoaders.foreach {
      s =>
        buffer.append("\t\tl->" + cleanJarURL(s.asInstanceOf[KevoreeJarClassLoader].classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar) + "_" + s.hashCode() + "\n")
    }
    subWeakClassLoaders.foreach {
      s =>
        if (s.get.isDefined) {
          buffer.append("\t\tw~>" + cleanJarURL(s.get.get.asInstanceOf[KevoreeJarClassLoader].classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar + "_" + s.get.get.hashCode()) + "\n")
        }
    }
    buffer.toString
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

  override def toString: String = cleanJarURL(classpathResources.asInstanceOf[KevoreeLazyJarResources].getLastLoadedJar) + hashCode()
}