package org.kevoree.library.javase.historyviewer;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* Created with IntelliJ IDEA.
* User: dvojtise
* Date: 09/11/12
* Time: 11:29
* To change this template use File | Settings | File Templates.
*/
class ModelHistoryTextLogFrame extends JPanel {

    private JTextPane screenTP;
    private JButton clearBtn;

    public ModelHistoryTextLogFrame() {
        setPreferredSize(new Dimension(ModelHistoryViewer.FRAME_WIDTH, ModelHistoryViewer.FRAME_HEIGHT));
        setLayout(new BorderLayout());
        clearBtn = new JButton("Clear");
        clearBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {


            }
        });

        screenTP = new JTextPane();
        screenTP.setFocusable(false);
        screenTP.setEditable(false);
        StyledDocument doc = screenTP.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);
        Style system = doc.addStyle("system", def);
        StyleConstants.setForeground(system, Color.GRAY);

        Style incoming = doc.addStyle("modelevent", def);
        StyleConstants.setForeground(incoming, Color.BLUE);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(clearBtn, BorderLayout.EAST);

        add(new JScrollPane(screenTP), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void appendSystem(String text) {
        try {
            StyledDocument doc = screenTP.getStyledDocument();
            doc.insertString(doc.getLength(), formatForPrint(text), doc.getStyle("system"));
        } catch (BadLocationException ex) {
//                ex.printStackTrace();
            ModelHistoryViewer.logger.error("Error while trying to append system message in the " + this.getName(), ex);
        }
    }

    public void appendModelEvent(String text) {
        try {
            StyledDocument doc = screenTP.getStyledDocument();
            doc.insertString(doc.getLength(), formatForPrint(text), doc.getStyle("modelevent"));
            screenTP.setCaretPosition(doc.getLength());
        } catch (BadLocationException ex) {
//                ex.printStackTrace();
            ModelHistoryViewer.logger.error("Error while trying to append incoming message in the " + this.getName(), ex);
            //getLoggerLocal().error(ex.getClass().getSimpleName() + " occured while trying to append text in the terminal.", ex);
        }
    }

    public void appendOutgoing(String text) {
        try {
            StyledDocument doc = screenTP.getStyledDocument();
            doc.insertString(doc.getLength(), ">" + formatForPrint(text), doc.getStyle("outgoing"));
        } catch (BadLocationException ex) {
//                ex.printStackTrace();
            ModelHistoryViewer.logger.error("Error while trying to append local message in the " + this.getName(), ex);
            //getLoggerLocal().error(ex.getClass().getSimpleName() + " occured while trying to append text in the terminal.", ex);
        }
    }

    private String formatForPrint(String text) {
        return (text.endsWith("\n") ? text : text + "\n");
    }
}
