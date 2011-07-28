package org.kevoree.annotation.processor.test

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit._
import java.util._
import org.apache.maven.cli.MavenCli
import java.io._

class MandatoryChecksTest {



  @Before
  def initialize() {


  }

  @Test
  def mandatoryChecks() {
    val rootDir: String = "TestProject"
    val workingDir: File = new File(getClass.getClassLoader.getResource(rootDir).getPath)

    val buffer = new StringBuffer();

    val outputStream = new OutputStream {
      def write(p1: Int) {
        buffer.append(p1.toChar)
      }
    }
    val printStream = new PrintStream(outputStream)

    val cli: MavenCli = new MavenCli()

    val args: ArrayList[String] = new ArrayList[String]
    args.add("clean")
    args.add("compile")
    cli.doMain(args.toArray[String](new Array[String](args.size())), workingDir.getAbsolutePath, printStream, printStream);

    val executionTrace = buffer.toString
    System.out.println(buffer.toString)


    assert(executionTrace.toString.contains("error: The className attribute of a Provided ServicePort"))

    assert(executionTrace.toString.contains("error: The className attribute of a Required ServicePort"))


    assert(executionTrace.toString.contains("error: @Stop method is mandatory"))

    assert(executionTrace.toString.contains("error: @Start method is mandatory"))
  }


  @After
  def clean() {

  }

}
