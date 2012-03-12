package org.kevoree.library.frascatiNodeTypes

import java.net.URL
import org.kevoree.kcl.KevoreeJarClassLoader

/**
 * Created with IntelliJ IDEA.
 * User: barais
 * Date: 12/03/12
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */

class FrascatiClassLoaderWrapper(kcl : KevoreeJarClassLoader) extends org.ow2.frascati.util.FrascatiClassLoader {

  override def loadClass(p1: String): Class[_] = {
    kcl.loadClass(p1)
  }

  override def getResourceAsStream(p1: String) = {
    kcl.getResourceAsStream(p1)
  }

  override def getResources(p1: String): java.util.Enumeration[URL] = {
    kcl.getResources(p1)
  }

  override def getResource(p1: String): URL = {
    kcl.getResource(p1)
  }

  override def getURLs(): Array[java.net.URL] = {
    val urls = kcl.getLinkedLoadedURLs()
    urls.toArray(new Array[java.net.URL](urls.size))
  }

  override def addURL(p1: URL) {
    val kcl1 = new KevoreeJarClassLoader()
    kcl1.add(p1)
    kcl.addSubClassLoader(kcl1)
  }

}
