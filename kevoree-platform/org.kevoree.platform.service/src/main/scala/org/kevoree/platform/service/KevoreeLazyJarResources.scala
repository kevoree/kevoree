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
package org.kevoree.platform.service

import java.io._
import java.util.jar.JarInputStream

import java.util.logging.Logger
import org.xeustechnologies.jcl.exception.JclException
import java.net.URL
import org.xeustechnologies.jcl.ClasspathResources
import java.lang.String
;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/01/12
 * Time: 19:13
 */

class KevoreeLazyJarResources extends ClasspathResources {

  protected val jarContentURL = new java.util.HashMap[String, URL]
  private val logger = Logger.getLogger(classOf[KevoreeLazyJarResources].getName);

  def getContentURL(name: String) = jarContentURL.get(name)

  override def loadJar(url: URL) {
    var in: InputStream = null;
    try {
      in = url.openStream();
      loadJar(in, url);
    } catch {
      case e: IOException => throw new JclException(e);
    } finally {
      if (in != null)
        try {
          in.close();
        } catch {
          case e: IOException => throw new JclException(e);
        }
    }
  }

  override def loadJar(jarFile: String) {
    var fis: FileInputStream = null;
    try {
      val f = new File(jarFile)
      fis = new FileInputStream(jarFile);
      loadJar(fis, new URL("file:" + f.getAbsolutePath))
    } catch {
      case e: IOException => throw new JclException(e);
    } finally {
      if (fis != null)
        try {
          fis.close();
        } catch {
          case e: IOException => throw new JclException(e);
        }
    }

  }

  def loadJar(jarStream: InputStream, baseurl: URL) {
    var bis: BufferedInputStream = null;
    var jis: JarInputStream = null;
    try {
      bis = new BufferedInputStream(jarStream);
      jis = new JarInputStream(bis);
      var jarEntry = jis.getNextJarEntry
      while (jarEntry != null) {
        if (!jarEntry.isDirectory) {
          if (jarEntryContents.containsKey(jarEntry.getName)) {
            if (!collisionAllowed) {
              throw new JclException("Class/Resource " + jarEntry.getName() + " already loaded");
            }
          } else {
            val b = new Array[Byte](2048)
            val out = new ByteArrayOutputStream();
            var len = 0;
            while (jis.available() > 0) {
              len = jis.read(b);
              if (len > 0) {
                out.write(b, 0, len);
              }
            }
            out.flush();
            out.close();
            jarEntryContents.put(jarEntry.getName, out.toByteArray)
            jarContentURL.put(jarEntry.getName, new URL("jar:" + baseurl + "!/" + jarEntry.getName))
          }
        }
        jarEntry = jis.getNextJarEntry
      }
    } catch {
      case e: IOException => new JclException(e)
      case e: NullPointerException =>
    } finally {
      if (jis != null)
        try {
          jis.close();
        } catch {
          case _@e => throw new JclException(e);
        }
      if (bis != null)
        try {
          bis.close();
        } catch {
          case _@e => throw new JclException(e);
        }
    }
  }

}