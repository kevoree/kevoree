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
import jsyntaxpane.{Token => JTOK }
import jsyntaxpane.TokenType;import org.kevoree.tools.marShell.lexer.KevsLexical
import org.kevoree.tools.marShell.parser.KevsParser
import org.slf4j.LoggerFactory


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




    /*
     list.clear()
     List<IKToken> tokens = loader.lex(sgmnt.toString());
     for (IKToken tok : tokens) {
     TokenType type = getType(tok);
     Token t = new Token(type,tok.getOffset(),tok.toString().length()  );
     System.out.println("Tok,"+tok.toString());

     // Markers.markToken(, t, null)

     list.add(t);
     }

     try{
     parser.parseSynch(sgmnt.toString());
     } catch(Exception e) {
     e.printStackTrace();
     }
     //loader.
     */
    // System.out.println("Must call parser "+i);

  }

  def getType(tok: Token) : TokenType = {
    tok match {
      case k : Keyword =>TokenType.KEYWORD
      case i : Identifier => TokenType.IDENTIFIER
      case d : Delimiter => TokenType.DELIMITER
      case c : Comment => TokenType.COMMENT
      case c : MLComment => TokenType.COMMENT
      case e : KError => TokenType.ERROR
      case slit : StringLit => TokenType.STRING
      case i : KIncomplet => TokenType.ERROR

      case _ => logger.info(tok.getClass.getName);TokenType.DEFAULT
    }
  }

}
