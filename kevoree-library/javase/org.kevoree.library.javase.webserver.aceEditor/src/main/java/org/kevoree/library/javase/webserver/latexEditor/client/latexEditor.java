package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.*;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class latexEditor implements EntryPoint {

    private AceEditor editor1;
    private latexEditorFileExplorer fileExplorer = null;


    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // create first AceEditor widget
        editor1 = new AceEditor();
        fileExplorer = new latexEditorFileExplorer(editor1);
        editor1.setWidth("100%");
        editor1.setHeight("100%");
        buildUI();

        // start the first editor and set its theme and mode
        editor1.startEditor(); // must be called before calling setTheme/setMode/etc.
        editor1.setTheme(AceEditorTheme.TWILIGHT);
        editor1.setMode(AceEditorMode.LATEX);
        //editor1.setStylePrimaryName("editor");

        // use cursor position change events to keep a label updated
        // with the current row/col
        editor1.addOnCursorPositionChangeHandler(new AceEditorCallback() {
            @Override
            public void invokeAceCallback(JavaScriptObject obj) {
               // updateEditor1CursorPosition();
            }
        });


    }

    private void buildUI() {

//        HorizontalSplitPanel p = new HorizontalSplitPanel();

        
        VerticalPanel leftBar = new VerticalPanel();
        ScrollPanel scrollLeft = new ScrollPanel(fileExplorer);


        Button btCompile = new Button();
        btCompile.setText("Compile");
        btCompile.setStyleName("btn");
        btCompile.addStyleName("primary");
        leftBar.add(btCompile);
        leftBar.add(scrollLeft);
        
        
        
        SplitLayoutPanel p = new SplitLayoutPanel();

        //p.setLeftWidget(scrollLeft);
        p.addWest(leftBar,200);
        p.add(editor1);

       // p.setSplitPosition("100px");
     //   p.setSplitPosition("20%");
       // VerticalPanel mainPanel = new VerticalPanel();
        p.setWidth("100%");
        p.setHeight("100%");
       // mainPanel.add(editor1);
        RootPanel.get().add(p);
        p.forceLayout();
    }

}
