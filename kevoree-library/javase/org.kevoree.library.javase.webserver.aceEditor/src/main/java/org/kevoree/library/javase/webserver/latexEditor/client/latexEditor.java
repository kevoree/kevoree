package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.StyleInjector;
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

        AceEditorWrapper.setText("WTF !!! Kevoree inside");

        // create first AceEditor widget
        //editor1 = new AceEditor("editor");
       // fileExplorer = new latexEditorFileExplorer(editor1);
      //  editor1.setWidth("100%");
    //    editor1.setHeight("100%");
      //  buildUI();



      //  AceEditorWrapper wrapper = new AceEditorWrapper();
 //       wrapper.initEditor();

        // start the first editor and set its theme and mode
      /*


        editor1.startEditor(); // must be called before calling setTheme/setMode/etc.
        editor1.setTheme(AceEditorTheme.IDLE_FINGERS);
        editor1.setMode(AceEditorMode.LATEX);
        editor1.setShowPrintMargin(false);
        editor1.setUseWrapMode(true);
        editor1.setUseSoftTabs(false);*/

    //    editor1.setHScrollBarAlwaysVisible(false);


        //editor1.setStylePrimaryName("editor");

        // use cursor position change events to keep a label updated
        // with the current row/col



    }

    private void buildUI() {

//        HorizontalSplitPanel p = new HorizontalSplitPanel();


        VerticalPanel leftBar = new VerticalPanel();
        ScrollPanel scrollLeft = new ScrollPanel(fileExplorer);

/*
        Button btCompile = new Button();
        btCompile.setText("Compile");
        btCompile.setStyleName("btn");
        btCompile.addStyleName("primary");
        leftBar.add(btCompile);
        leftBar.add(scrollLeft);*/


      //  SplitLayoutPanel p = new SplitLayoutPanel();
      //  StyleInjector.inject(".gray.gwt-SplitLayoutPanel .gwt-SplitLayoutPanel-HDragger { background: silver; }");
      //  p.addStyleName("gray");

        //  p.getElement().getStyle().setBackgroundColor("black");
        //p.setLeftWidget(scrollLeft);
      //  p.addWest(leftBar, 200);
        //p.add(editor1);

      //  p.setWidth("100%");
      //  p.setHeight("100%");




       // RootPanel.get("files").add(scrollLeft);
        //p.forceLayout();
    }

}
