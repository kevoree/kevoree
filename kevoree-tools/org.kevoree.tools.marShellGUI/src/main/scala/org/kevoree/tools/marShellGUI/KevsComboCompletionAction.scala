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

import java.awt.event.ActionEvent
import javax.swing.text.JTextComponent
import jsyntaxpane.actions.ComboCompletionAction
import scala.collection.JavaConversions._
import org.kevoree.tools.marShell.lexer.KevsLexical
import jsyntaxpane.{Token, TokenType, SyntaxDocument}

class KevsComboCompletionAction extends ComboCompletionAction {

  this.setItems(new java.util.ArrayList[String]())

  @Override
  override def actionPerformed(target: JTextComponent, sdoc: SyntaxDocument, dot: Int, e: ActionEvent) = {
    refreshList(target, sdoc, dot)


    super.actionPerformed(target, sdoc, dot, e)

  }

  def refreshList(target: JTextComponent, sdoc: SyntaxDocument, dot: Int) = {
    val token = sdoc.getTokenAt(dot);
    var tokens: String = ""
    val tokenPrevious = sdoc.getTokenAt(dot - 1);
    var tokenPreviousS: String = ""
    if (token != null) {
      tokens = token.getString(sdoc);
    }
    if (tokenPrevious != null) {
      tokenPreviousS = tokenPrevious.getString(sdoc);
    }


    isFirstDelimiter(sdoc,dot)
    println(getFirstDelimiter(sdoc,dot).get.getString(sdoc) )
    println(getFirstKeyword(sdoc,dot).get.getString(sdoc) )

    
    tokenPreviousS match {
      case _ if(tokenPreviousS == ":" || tokens == ":") => pushTypeId
      case _ if(tokenPreviousS == "@" || tokens == "@") => pushNodeId
      case _ => pushKeyword
      
    }
    


  }

  def isFirstDelimiter(sdoc: SyntaxDocument,dot: Int) : Boolean ={
    sdoc.getTokens(0,dot).toList.reverse.foreach(t => println(t))
    true
          //sdoc.getTokens(0,dot).toList.reverse.find(tok=>tok.`type` == TokenType.DELIMITER)
  }

  def getFirstDelimiter(sdoc: SyntaxDocument,dot: Int) : Option[Token]={
          sdoc.getTokens(0,dot).toList.reverse.find(tok=>tok.`type` == TokenType.DELIMITER)
  }
  def getFirstKeyword(sdoc: SyntaxDocument,dot: Int) : Option[Token]={
          sdoc.getTokens(0,dot).toList.reverse.find(tok=>tok.`type` == TokenType.KEYWORD || tok.`type` == TokenType.KEYWORD2)
  }


  def pushNodeId = {
    KevsModelHandlers.get(1) match {
      case Some(model) => {
          val items = new java.util.ArrayList[String]()
          model.getNodes.foreach {
            node =>
            items.add(node.getName)
          }
          this.setItems(items)
        }
      case None =>
    }
  }
  
  def pushTypeId = {
    KevsModelHandlers.get(1) match {
      case Some(model) => {
          val items = new java.util.ArrayList[String]()
          model.getTypeDefinitions.foreach {
            tdef =>
            items.add(tdef.getName)
          }
          this.setItems(items)
        }
      case None =>
    }
  }

  def pushKeyword = {
    var lex = new KevsLexical
    this.setItems(lex.reserved.toList)
  }


}
