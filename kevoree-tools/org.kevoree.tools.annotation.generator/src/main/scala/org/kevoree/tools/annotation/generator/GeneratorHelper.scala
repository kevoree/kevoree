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
package org.kevoree.tools.annotation.generator

/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 01/03/13
* (c) 2013 University of Luxembourg – Interdisciplinary Centre for Security Reliability and Trust (SnT)
* All rights reserved
*/
object GeneratorHelper {

  def protectReservedWord(w:String) : String = {
    w match {
      case "object" => "`object`"
      case "package" => "`package`"
      case "def" => "`def`"
      case _ => w
    }
  }


  def protectedType(t:String) : String = {
    t match {
      case "java.lang.String" => "String"
      case "void" => "Unit"
      case _ => t.replace("[","<")
        .replace("]",">")
        .replace("Array<Byte>","ByteArray")
        .replace("java.lang.","")
        .replace("java.util.","")
    }

  }



}
