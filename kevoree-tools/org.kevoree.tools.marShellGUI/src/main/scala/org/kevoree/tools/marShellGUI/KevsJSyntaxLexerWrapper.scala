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

package org.kevoree.tools.marShellGUI

import javax.swing.text.Segment
import jsyntaxpane.components.Markers
import org.kevoree.tools.marShell.lexer.KevsLexical
import org.kevoree.tools.marShell.parser.KevsParser
import org.slf4j.LoggerFactory
import jsyntaxpane.{TokenTypes, Token => JTOK, TokenType}


class KevsJSyntaxLexerWrapper extends KevsLexical with jsyntaxpane.Lexer {

  var logger = LoggerFactory.getLogger(this.getClass)

  override def parse(sgmnt:Segment , i : Int, list: java.util.List[JTOK] ) = {

    list.clear()
    var tokens = new Scanner(sgmnt.toString())

    while(!tokens.atEnd){
      var newtype = getType(tokens.first)
      var newtok = new JTOK(newtype,tokens.first.asInstanceOf[KevsToken].getOffset.intValue,tokens.first.toString().length()  );
      list.add(newtok)
      tokens = tokens.rest
    }
  }

  def getType(tok: Token) : TokenType = {
    tok match {
      case k : Keyword =>TokenTypes.KEYWORD
      case i : Identifier => TokenTypes.IDENTIFIER
      case d : Delimiter => TokenTypes.DELIMITER
      case c : Comment => TokenTypes.COMMENT
      case c : MLComment => TokenTypes.COMMENT
      case e : KError => TokenTypes.ERROR
      case slit : StringLit => TokenTypes.STRING
      case i : KIncomplet => TokenTypes.ERROR

      case _ => logger.info(tok.getClass.getName);TokenTypes.DEFAULT
    }
  }

}
