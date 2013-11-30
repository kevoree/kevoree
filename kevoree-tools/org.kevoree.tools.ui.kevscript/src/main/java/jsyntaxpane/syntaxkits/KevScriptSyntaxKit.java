/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License 
 *       at http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 */
package jsyntaxpane.syntaxkits;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.lexers.KevScriptLexer;

import java.util.Properties;

/**
 * @author Fouquet Francois
 */
public class KevScriptSyntaxKit extends DefaultSyntaxKit {

    public KevScriptSyntaxKit() {
        super(new KevScriptLexer());
        Properties config = new java.util.Properties();
        config.setProperty("Action.indent.WordRegex", "\\w+|\\/(\\*)+");
        config.setProperty("Action.combo-completion", "org.kevoree.tools.ui.kevscript.KevScriptCompletion, control SPACE");
        config.setProperty("Action.combo-completion.MenuText", "Completions");
        config.setProperty("Action.double-quotes", "jsyntaxpane.actions.PairAction, typed \"");
        config.setProperty("Style.IDENTIFIER", "0xFFCE89, 1");
        config.setProperty("Style.DELIMITER", "0xFFFFFF, 1");
        config.setProperty("Style.KEYWORD", "0xFC6C1D, 1");
        config.setProperty("Style.KEYWORD2", "0xFC6C1D, 3");
        config.setProperty("Style.OPERATOR", "0xFFFFFF,1");
        config.setProperty("CaretColor", "0xFFFFFF");
        config.setProperty("TokenMarker.Color", "0x6D788F");
        config.setProperty("PairMarker.Color", "0x6D788F");
        config.setProperty("LineNumbers.Background", "0x646464");
        config.setProperty("LineNumbers.Foreground", "0xFFFFFF");
        config.setProperty("LineNumbers.CurrentBack", "0xF4A460");
        setConfig(config);
    }
}
