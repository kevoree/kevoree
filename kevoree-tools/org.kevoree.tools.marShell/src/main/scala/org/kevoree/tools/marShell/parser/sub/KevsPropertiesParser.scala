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

package org.kevoree.tools.marShell.parser.sub

trait KevsPropertiesParser extends KevsAbstractParser {

  def parseProperties : Parser[java.util.Properties] = "{" ~ repsep(parseProperty,",") ~ "}" ^^{ case _ ~ propsParsed ~ _ =>
      var props = new java.util.Properties
      propsParsed.foreach{prop =>
        props.put(prop._1, prop._2)
      }
      props
  }

  def parseProperty : Parser[Tuple2[String,String]] = ident ~ "=" ~ stringLit ^^{ case id ~ _ ~ content =>
      Tuple2(id,content)
  }


}
