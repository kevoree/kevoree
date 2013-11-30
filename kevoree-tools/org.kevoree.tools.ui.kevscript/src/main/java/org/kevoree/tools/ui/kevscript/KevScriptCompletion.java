package org.kevoree.tools.ui.kevscript;

import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.actions.ComboCompletionAction;
import jsyntaxpane.actions.gui.ComboCompletionDialog;

import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 30/11/2013
 * Time: 14:33
 */
public class KevScriptCompletion extends ComboCompletionAction {

    ComboCompletionDialog ldlg = null;

    private List<String> cmd = Arrays.asList("add INSTANCENAME : TYPENAME\n", "move NODE.COMPONENT TARGETNODE\n");

    @Override
    public void actionPerformed(JTextComponent target, SyntaxDocument sdoc, int dot, ActionEvent e) {
        if (ldlg == null) {
            ldlg = new ComboCompletionDialog(target);
        }

        Token token = sdoc.getTokenAt(dot);
        if (token == null) {
            Iterator<Token> it = sdoc.getTokens(0, dot);
            while (it.hasNext()) {
                token = it.next();
            }

        }
        if (token != null) {
            if (token.getString(sdoc).endsWith(":")) {
                //fill type
            } else {
                ldlg.displayFor("", cmd);
            }
        } else {
            ldlg.displayFor("", cmd);
        }
    }
}
