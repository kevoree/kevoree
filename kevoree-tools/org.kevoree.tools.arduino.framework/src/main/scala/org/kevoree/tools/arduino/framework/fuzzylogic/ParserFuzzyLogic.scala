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

import fuzzy.ast.{FuzzyPredicate, FuzzyRule, FuzzyRules}
import gen.utils.ArduinoException
import util.parsing.combinator.syntactical.StandardTokenParsers
import collection.JavaConversions._

/**
 * Created by jed
 * User: jedartois@gmail.com
 * Date: 13/01/12
 * Time: 10:10
 */

class ParserFuzzyLogic extends StandardTokenParsers{

  lexical.delimiters ++= List(";")
  lexical.reserved +=("IF", "THEN","IS","AND")

  def fuzzyDomain  = ident ^^ {
    case s => s
  }

  def fuzzyTerm  =ident ^^ {
    case s => s
  }

  // FUZZYDOMAIN IS FUZZY TERM
  def  parserArduinoFuzzyPredicate: Parser[FuzzyPredicate]   = fuzzyDomain ~ "IS" ~ fuzzyTerm ^^  {
    case     d  ~ _ ~ t =>  new FuzzyPredicate(d,t)
  }

  // FUZZY RULE
  def arduinoFuzzyRule: Parser[FuzzyRule] =
    "IF" ~ rep1sep (parserArduinoFuzzyPredicate, "AND") ~ "THEN" ~ rep1sep (parserArduinoFuzzyPredicate, "AND")  ^^  {
      case   _ ~ antecedent  ~ _ ~ outcome =>    new FuzzyRule(antecedent,outcome)
    }


  //  global
  def requestParse: Parser[FuzzyRules] =
    rep1sep(arduinoFuzzyRule , ";") ^^ {
      case rules: List[FuzzyRule]=> new  FuzzyRules(rules)
    }

  def   parseRules(chaine : String) :FuzzyRules = {

    requestParse(new lexical.Scanner(chaine)) match {
      case Success(_rules, _) => _rules
      case Failure(msg,_) => throw new ArduinoException("Bad syntax: "+msg)
      case Error(msg, _) =>  throw new ArduinoException("Bad syntax: "+msg)
    }


  }
}