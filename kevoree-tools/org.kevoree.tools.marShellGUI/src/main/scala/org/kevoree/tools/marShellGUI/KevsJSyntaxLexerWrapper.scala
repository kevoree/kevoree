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

package org.kevoree.tools.marShellGUI

import javax.swing.text.Segment
import org.kevoree.tools.marShell.lexer.KevsLexical
import jsyntaxpane.{TokenTypes, Token => JTOK, TokenType}
import org.kevoree.log.Log

class KevsJSyntaxLexerWrapper extends KevsLexical with jsyntaxpane.Lexer {

  override def parse(sgmnt:Segment , i : Int, list: java.util.List[JTOK] ) = {

    list.clear()
    var tokens = new Scanner(sgmnt.toString())

    while(!tokens.atEnd){
      val newtype = getType(tokens.first)
      val newtok = new JTOK(newtype,tokens.first.asInstanceOf[KevsToken].getOffset.intValue,tokens.first.toString().length()  );
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

      case _ => Log.info(tok.getClass.getName);TokenTypes.DEFAULT
    }
  }

}
