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
package org.kevoree.tools.marShell.parser.sub

import scala.collection.mutable.HashMap
import org.kevoree.tools.marShell.ast._
import scala.util.parsing.combinator.syntactical.TokenParsers
import org.kevoree.tools.marShell.lexer.KevsLexical

/**
 * Common part of all Sub parsers
 */
trait KevsAbstractParser extends TokenParsers {
  type Tokens = org.kevoree.tools.marShell.lexer.KevsTokens
  val lexical = new KevsLexical {
    override def whitespace: Parser[Any] = rep(whitespaceChar | comment)
  }
  import lexical._

  def kevStatement : Parser[List[Statment]]
  //def fExpression : Parser[Expression] = fLiteral
  //def fLiteral : Parser[Expression]
  def componentID : Parser[ComponentInstanceID]

  protected val keywordCache : HashMap[String, Parser[String]] = HashMap.empty
  protected val delimCache : HashMap[String, Parser[String]] = HashMap.empty

  /** A parser which matches a numeric literal */
  def numericLit: Parser[String] =
    elem("number", _.isInstanceOf[NumericLit]) ^^ (_.chars)

  /** A parser which matches a string literal */
  def stringLit: Parser[String] =
    elem("string literal", _.isInstanceOf[StringLit]) ^^ (_.chars)

  /** A parser which matches an identifier */
  def ident: Parser[String] =
    elem("identifier", _.isInstanceOf[Identifier]) ^^ (_.chars)

  //Error handling
  def orFailure[A](a:Parser[A],msg:String) : Parser[A] = ( a | failure(msg) )


  def identOrWildcard = ident | "*"


  //an implicit keyword function that gives a warning when a given word is not in the reserved/delimiters list
  implicit def keyword(chars : String): Parser[String] =
    if(lexical.reserved.contains(chars)) keywordCache.getOrElseUpdate(chars, accept(Keyword(chars)) ^^ (_.chars))
    else if(lexical.delimiters.contains(chars)) delimCache.getOrElseUpdate(chars, accept(Delimiter(chars)) ^^ (_.chars))
    else failure("You are trying to parse \""+chars+"\", but it is neither contained in the delimiters list, nor in the reserved keyword list of your lexical object")


}
