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
package org.kevoree.tools.arduino.framework.fuzzylogic

import eca.ast._
import gen.Constants
import gen.utils.ArduinoException
import scala.util.parsing.combinator.syntactical._
import scala.collection.JavaConversions._

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 13/01/12
 * Time: 10:08
 */

class ParserECA  extends StandardTokenParsers{

  lexical.delimiters ++= List(";")
  lexical.reserved +=("IF", "THAN","IS", "AND","OR","DO","CHECK","EVERY","SINCE","AFTER","MILLISECONDES","MINUTES","SECONDES","HEURES","EQUALS","HIGHER","LESS")

  def value  =numericLit ^^ {
    case s => s
  }

  def parseOperator: Parser[Constants.OPERATORS] =
    ("EVERY" | "AFTER") ^^ {
      case "EVERY" => Constants.OPERATORS.EVERY
      case "AFTER" => Constants.OPERATORS.AFTER
    }


  def parseUnit: Parser[Constants.UNITS] =
    ("MILLISECONDES" | "SECONDES"| "MINUTES" | "HEURES" ) ^^ {
      case "MILLISECONDES" => Constants.UNITS.MILLISECONDES
      case "SECONDES" => Constants.UNITS.SECONDES
      case "MINUTES" => Constants.UNITS.MINUTES
      case "HEURES" => Constants.UNITS.HEURES
    }


  def parseSign: Parser[Constants.SIGNS] =
    ("HIGHER" | "LESS"| "EQUALS" | "CHANGE" ) ^^ {
      case "HIGHER" => Constants.SIGNS.HIGHER
      case "LESS" => Constants.SIGNS.LESS
      case "EQUALS" => Constants.SIGNS.EQUALS
      case "CHANGE" => Constants.SIGNS.CHANGE
    }


  def inputValue  = ident ^^ {
    case s => s
  }

  def  parserArduinoECAEvent: Parser[ECAEventPredicate]   = inputValue ~ "IS" ~ parseSign ~ value ^^  {
    case     inputValue  ~ _ ~ sign ~ v =>  new ECAEventPredicate(inputValue,sign,v.toFloat)
  }


  def action  =ident ^^ {
    case s => s
  }


  def arduinoECARule: Parser[ECARule] =
    "IF" ~ rep1sep (parserArduinoECAEvent , "AND") ~ "DO"  ~  action  ^^  {
      case   _ ~ events  ~ _ ~ action =>    new ECARule(events,action)
    }

  // TEMPORA + ECA RULE
  def parserArduinoFuzzyTemporalRule : Parser[ECATemporal] = parserarduinoFuzzyTemporalPredicate ~ arduinoECARule ^^{
    case t  ~ t2 =>   new ECATemporal(t,t2)
  }

  // SINCE 10 MINUTES CHECK
  def parserarduinoFuzzyTemporalPredicate :Parser[ECATemporalPredicate] =  parseOperator  ~ value ~ parseUnit ~ "CHECK"  ^^ {
    case t ~ d ~ u ~ _ =>   new ECATemporalPredicate(t,d.toInt,u)
  }


  //  global
  def requestParse: Parser[ECARules] =
    rep1sep(arduinoECARule | parserArduinoFuzzyTemporalRule, ";") ^^ {
      case rules: List[ECARule]=> new  ECARules(rules)
    }



  def   parseRules(chaine : String) :ECARules = {

    requestParse(new lexical.Scanner(chaine)) match {
      case Success(_rules, _) => _rules
      case Failure(msg,_) => throw new ArduinoException("Bad syntax: "+msg)
      case Error(msg, _) =>  throw new ArduinoException("Bad syntax: "+msg)
    }

  }

}