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
package org.kevoree.tools.annotation.scalaprocessor

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 23/04/12
 * Time: 20:17
 */

import tools.nsc._
import java.io.File
import tools.nsc.reporters.Reporter
import java.net.URLClassLoader

class KevoreeScalaParser {

  val settings = new Settings(s => {
    sys.error("errors report: " + s)
  })
  settings.sourcepath.tryToSet(List("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.annotation.scalaprocessor/src/test/resources/kproj/src/main/scala"))
  settings.outdir.tryToSet(List("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.annotation.scalaprocessor/src/test/resources/kproj/target"))
  settings.classpath.tryToSet(List(currentClassPath.map(_.getAbsolutePath).mkString(File.pathSeparator)))

  //settings.


  def currentClassPath = {
    // this tries to detect the classpath, if it doesn't work
    // for you, please email me or open an issue explaining your
    // usecase.
    def cp(cl: ClassLoader): Set[File] = cl match {
      case ucl: URLClassLoader => ucl.getURLs.map(u => new File(u.getFile)).toSet ++ cp(ucl.getParent)
      case _: ClassLoader => Set()
      case null => Set()
    }
    cp(Thread.currentThread.getContextClassLoader) ++ System.getProperty("java.class.path").split(File.pathSeparator).map(p => new File(p)).toSet
  }



  private val g = new Global(settings)//, new CompilationReporter)
  private lazy val run = new g.Run

  def compile(){


    val phase = run.phaseNamed("typer")
    val cps = new KevoreeAnnotationProcessor(g)
    cps.KevoreeAnnotationProcessorComponent.newPhase(phase)
    run.compile(List("/Users/duke/Documents/dev/dukeboard/kevoree/kevoree-tools/org.kevoree.tools.annotation.scalaprocessor/src/test/resources/kproj/src/main/scala/KevScalaCT.scala"))
  }


}
