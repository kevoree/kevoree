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

package org.kevoree.framework

import JacksonSerializer._
import scala.reflect.BeanProperty
import scala.runtime.BoxedUnit
import org.codehaus.jackson.annotate._


//case class TOTO @JsonCreator()( @JsonProperty("name") name: String  )

object JacksonTester {





  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {

//    var r = TOTO("DEDE")
//var rjson = r.toJSON
//println(rjson)


    println("Hello, world!")

    var t = new PJO
    var json = t.toJSON

    println(json)

    var result = json.toString.fromJSON(classOf[PJO])

    println(result)

  }


  def toto : Unit = {
    println("echo")
  }

  class PJO{
    @BeanProperty
    var dedeString : String = "def"

    @BeanProperty
    var msg : Msg = new MyMsg

  }


  class MyMsg extends Msg {
    @BeanProperty
    var dede = "def"
  }

  @JsonTypeInfo(use=org.codehaus.jackson.annotate.JsonTypeInfo.Id.CLASS, include=org.codehaus.jackson.annotate.JsonTypeInfo.As.PROPERTY, property="class")
  class Msg {
    @BeanProperty
    var mydede = "defdef"
  }

}
