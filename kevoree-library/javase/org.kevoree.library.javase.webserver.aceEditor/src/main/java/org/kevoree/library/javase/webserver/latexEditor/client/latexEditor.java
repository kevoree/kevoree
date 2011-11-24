package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorCallback;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorMode;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditorTheme;

/**
 * Entry point classes define <code>onModuleLoad()</code>
 */
public class latexEditor implements EntryPoint {


    private latexEditorFileExplorer fileExplorer = null;


    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        fileExplorer = new latexEditorFileExplorer();
        VerticalPanel leftBar = new VerticalPanel();
        ScrollPanel scrollLeft = new ScrollPanel(leftBar);
        Button btCompile = new Button();
        btCompile.setText("Compile");
        btCompile.setStyleName("btn");
        btCompile.addStyleName("primary");

        Button btSave = new Button();
        btSave.setText("Save");
        btSave.setStyleName("btn");
        btSave.addStyleName("primary");

        HorizontalPanel bts = new HorizontalPanel();
        bts.add(btCompile);
        bts.add(btSave);
        leftBar.add(bts);
        leftBar.add(fileExplorer);
        RootPanel.get("files").add(scrollLeft);


    }

}
