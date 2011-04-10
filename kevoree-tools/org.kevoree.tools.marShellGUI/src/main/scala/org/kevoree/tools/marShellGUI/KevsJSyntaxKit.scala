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

import jsyntaxpane.DefaultSyntaxKit
import jsyntaxpane.util.Configuration

class KevsJSyntaxKit extends DefaultSyntaxKit(new KevsJSyntaxLexerWrapper()) {

  override def getContentType = "text/kevs"

  var config = new java.util.Properties
  config.setProperty("RightMarginColumn", "80")
  config.setProperty("RightMarginColor", "0xdddddd")

  config.setProperty("Action.indent.WordRegex", "\\w+|\\/(\\*)+")
  config.setProperty("Action.combo-completion", "org.kevoree.tools.marShellGUI.KevsComboCompletionAction, control SPACE")
  config.setProperty("Action.combo-completion.MenuText", "Completions")
  config.setProperty("Action.double-quotes", "jsyntaxpane.actions.PairAction, typed \"")

  //config.setProperty("LineNumbers.CurrentBack","0x333300")




  this.setConfig(config)



}
