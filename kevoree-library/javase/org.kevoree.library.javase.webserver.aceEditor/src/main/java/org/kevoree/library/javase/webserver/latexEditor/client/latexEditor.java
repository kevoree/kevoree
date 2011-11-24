package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

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
        btCompile.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                latexEditorRPC.callForCompile(fileExplorer);
            }
        });


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
