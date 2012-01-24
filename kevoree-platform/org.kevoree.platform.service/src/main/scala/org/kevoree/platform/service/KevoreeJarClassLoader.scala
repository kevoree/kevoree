package org.kevoree.platform.service

import org.xeustechnologies.jcl.JarClassLoader
import java.io.{ByteArrayInputStream, InputStream}
import java.lang.String
import java.net.URL

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */

class KevoreeJarClassLoader extends JarClassLoader {

  classpathResources =  new KevoreeLazyJarResources

  override def getResourceAsStream(name  : String) : InputStream = {
    val res = this.classpathResources.getResource(name)
    if(res != null){
      new ByteArrayInputStream(res)
    } else {
      null
    }
  }

  override def getResource(p1: String): URL = {
    classpathResources.asInstanceOf[KevoreeLazyJarResources].getContentURL(p1)
  }
}