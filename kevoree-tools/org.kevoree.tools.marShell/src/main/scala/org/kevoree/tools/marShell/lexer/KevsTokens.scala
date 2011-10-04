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
package org.kevoree.tools.marShell.lexer

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.token.Tokens
import scala.util.parsing.input.OffsetPosition
import scala.util.parsing.input.Positional


trait KevsTokens extends Tokens with Parsers {

  abstract case class KevsToken extends Token with Positional {
    def getOffset : java.lang.Integer  = this.pos.asInstanceOf[OffsetPosition].offset
    def getLength : java.lang.Integer = this.toString.length
  }

  /** The class of comment tokens */
  case class WHITESPACE extends KevsToken {
    override def chars = ' '.toString
    override def toString = ' '.toString
  }

  /** The class of comment tokens */
  case class Comment(chars: String) extends KevsToken {
    override def toString = "//"+chars
  }

  /** The class of comment tokens */
  case class MLComment(chars: String) extends KevsToken {
    override def toString = "/*"+chars+"*/"
  }
  /** The class of comment tokens */
  case class ERR_MLComment(chars: String) extends KevsToken {
    override def toString = "/* "+chars+" */"
  }
  /** The class of delim tokens */
  case class Delimiter(chars: String) extends KevsToken {
    override def toString = chars
  }

  /** The class of keyword tokens */
  case class Keyword(chars: String) extends KevsToken {
    override def toString = chars
  }

  /** The class of numeric literal tokens */
  case class NumericLit(chars: String) extends KevsToken {
    override def toString = chars
  }

  /** The class of string literal tokens */
  case class StringLit(chars: String) extends KevsToken {
    override def toString = "\""+chars+"\""
  }

  /** The class of identifier tokens */
  case class Identifier(chars: String) extends KevsToken {
    override def toString = chars
  }

  case class KEOF extends KevsToken {
    override def toString = ""
    override def chars = ' '.toString
  }

  case class KIncomplet(chars:String,msg:String) extends KevsToken {
    override def toString = chars
  }

  case class KError(chars: String) extends KevsToken{
    override def toString = chars
    override def getLength = 1
  }


}
