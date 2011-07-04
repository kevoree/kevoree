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
import jsyntaxpane.actions.gui.ComboCompletionDialog
import org.kevoree._
import jsyntaxpane.{TokenTypes, Token, TokenType, SyntaxDocument}

class KevsComboCompletionAction extends ComboCompletionAction {

  this.setItems(new java.util.ArrayList[String]())


  var ldlg: ComboCompletionDialog = null;


  @Override
  override def actionPerformed(target: JTextComponent, sdoc: SyntaxDocument, dot: Int, e: ActionEvent): Unit = {
    refreshList(target, sdoc, dot)
    // super.actionPerformed(target, sdoc, dot, e)
    if (sdoc == null) {
      return Unit;
    }
    val token = sdoc.getTokenAt(dot);
    var abbrev = "";
    if (token != null) {
      token match {
        case _@delim if (token.`type` == TokenTypes.DELIMITER) => //NOOP FOR DELIMITER
        case _ => {
          abbrev = token.getString(sdoc);
          target.select(token.start, token.end());
        }
      }
    }
    if (ldlg == null) {
      ldlg = new ComboCompletionDialog(target);
    }
    ldlg.displayFor(abbrev, getItems);
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


    tokenPreviousS match {


      case _ if (isFirstDelimiter(sdoc, dot, "=>")) => {
        pushChannelId()
      }
      case _ if (isFirstDelimiter(sdoc, dot, ":")) => {
        getFirstKeyword(sdoc, dot) match {
          case Some(e) if (e.getString(sdoc) == "addComponent") => pushFilteredTypeId((tdef: TypeDefinition) =>
            tdef match {
              case e: ComponentType => true
              case _@noType => false
            })
          case Some(e) if (e.getString(sdoc) == "addChannel") => pushFilteredTypeId((tdef: TypeDefinition) =>
            tdef match {
              case e: ChannelType => true
              case _@noType => false
            })
          case Some(e) if (e.getString(sdoc) == "addGroup") => pushFilteredTypeId((tdef: TypeDefinition) =>
            tdef match {
              case e: GroupType => true
              case _@noType => false
            })
          case Some(e) if (e.getString(sdoc) == "addNode") => pushFilteredTypeId((tdef: TypeDefinition) =>
            tdef match {
              case e: NodeType => true
              case _@noType => false
            })
          case _@e => pushTypeId()
        }
      }


      case _ if (isFirstDelimiter(sdoc, dot, ".")) => {
        pushComponentPortRequireId(sdoc, dot)
      }

    //  case _ if (tokenPreviousS == "." || tokens == ".") => pushComponentPortRequireId(sdoc, dot)

      case _ if (tokenPreviousS == "@" || tokens == "@") => pushNodeId()
      case _ if (tokenPreviousS == "bind" && tokens != "bind") => pushComponentId()
      case _ if (tokenPreviousS == "unbind" && tokens != "unbind") => pushComponentId()
      case _ => pushKeyword()

    }


  }


  def isFirstDelimiter(sdoc: SyntaxDocument, dot: Int, delimiterValue: String): Boolean = {
    val firstTok = sdoc.getTokens(0, dot).toList.reverse.get(0)
    if (firstTok == null) {
      return false
    } else {
      firstTok.getString(sdoc) == delimiterValue
    }
  }

  def getFirstIdentifier(sdoc: SyntaxDocument, dot: Int): Option[Token] = {
    sdoc.getTokens(0, dot).toList.reverse.find(tok => tok.`type` == TokenTypes.IDENTIFIER)
  }

  def getFirstDelimiter(sdoc: SyntaxDocument, dot: Int): Option[Token] = {
    sdoc.getTokens(0, dot).toList.reverse.find(tok => tok.`type` == TokenTypes.DELIMITER)
  }

  def getFirstKeyword(sdoc: SyntaxDocument, dot: Int): Option[Token] = {
    sdoc.getTokens(0, dot).toList.reverse.find(tok => tok.`type` == TokenTypes.KEYWORD || tok.`type` == TokenTypes.KEYWORD2)
  }


  def pushComponentPortRequireId(sdoc: SyntaxDocument, dot: Int) {

    println("pushComponentPortRequireId")

    val token = getFirstIdentifier(sdoc, dot)
    KevsModelHandlers.get(1) match {
      case Some(model) => {
        val items = new java.util.ArrayList[String]()
        token match {
          case None =>
          case Some(tokenFound) => {
            model.getNodes.foreach(node => {
              node.getComponents.filter(component => component.getName == tokenFound.getString(sdoc)).foreach(cinstance => {
                cinstance.asInstanceOf[ComponentInstance].getProvided.foreach(port => items.add(port.getPortTypeRef.getName))
                cinstance.asInstanceOf[ComponentInstance].getRequired.foreach(port => items.add(port.getPortTypeRef.getName))
              })
            })
          }
        }

        this.setItems(items)
      }
      case None =>
    }
  }


  def pushChannelId() {
    KevsModelHandlers.get(1) match {
      case Some(model) => {
        val items = new java.util.ArrayList[String]()
        model.getHubs.foreach {
          hub =>println
            items.add(hub.getName)
        }
        this.setItems(items)
      }
      case None =>
    }
  }

  def pushComponentId() {
    KevsModelHandlers.get(1) match {
      case Some(model) => {
        val items = new java.util.ArrayList[String]()
        model.getNodes.foreach {
          node =>
            node.getComponents.foreach {
              component =>
                items.add(component.getName)
            }
        }


        this.setItems(items)
      }
      case None =>
    }
  }


  def pushNodeId() {
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

  def pushFilteredTypeId[C](filter: (TypeDefinition) => Boolean) {
    KevsModelHandlers.get(1) match {
      case Some(model) => {
        val items = new java.util.ArrayList[String]()
        model.getTypeDefinitions.filter(tdef => filter(tdef)).foreach {
          tdef => items.add(tdef.getName)
        }
        this.setItems(items)
      }
      case None =>
    }
  }

  def pushTypeId() {
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

  def pushKeyword() {
    val lex = new KevsLexical
    this.setItems(lex.reserved.toList)
  }


}
