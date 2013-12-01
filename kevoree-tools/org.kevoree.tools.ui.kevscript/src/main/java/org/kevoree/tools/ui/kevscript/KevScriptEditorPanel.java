package org.kevoree.tools.ui.kevscript;

import jsyntaxpane.components.Markers;
import jsyntaxpane.syntaxkits.KevScriptSyntaxKit;
import org.kevoree.api.ModelService;
import org.kevoree.kevscript.Parser;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: duke
 * Date: 30/11/2013
 * Time: 13:43
 */
public class KevScriptEditorPanel extends JPanel implements Runnable {

    private ModelService modelService;

    public KevScriptEditorPanel(ModelService ms) {
        modelService = ms;
        init();
    }

    public KevScriptEditorPanel() {
        init();
    }

    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private JEditorPane codeEditor = null;
    private Boolean isChanged = true;

    private void init() {
        this.setLayout(new BorderLayout());
        jsyntaxpane.DefaultSyntaxKit.initKit();
        jsyntaxpane.DefaultSyntaxKit.registerContentType("text/kevs", KevScriptSyntaxKit.class.getName());
        codeEditor = new JEditorPane();
        codeEditor.setDoubleBuffered(true);
        JScrollPane scrPane = new JScrollPane(codeEditor);
        codeEditor.setContentType("text/kevs; charset=UTF-8");
        codeEditor.setBackground(Color.DARK_GRAY);
        codeEditor.setText("");
        add(scrPane, BorderLayout.CENTER);
        scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
        codeEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isChanged = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isChanged = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isChanged = true;
            }
        });

    }

    public String getContent() {
        return codeEditor.getText();
    }

    public void setContent(String c) {
        codeEditor.setText(c);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private Parser parser = new Parser();


    @Override
    public void run() {
        try {
            Markers.removeMarkers(codeEditor);
            if (!codeEditor.getText().trim().isEmpty()) {
                ParseResult result = parser.parse(new InputBuffer(codeEditor.getText().toCharArray()));
                if (result.getError() != null) {
                    Markers.SimpleMarker marker = new Markers.SimpleMarker(Color.RED, result.getError().getNT());
                    Markers.markText(codeEditor, result.getError().getPosition(), result.getError().getPosition() + 1, marker);
                }
                isChanged = false;
            }
        } catch (Exception e) {

        }

    }
}
