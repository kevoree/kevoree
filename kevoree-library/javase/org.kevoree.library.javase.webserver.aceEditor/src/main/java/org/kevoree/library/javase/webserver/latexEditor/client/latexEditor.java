package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

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

        Button btCompile = new Button();
        btCompile.setText("PDF");
        btCompile.setStyleName("btn");
        btCompile.addStyleName("primary");
        btCompile.addStyleName("small");
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
        btSave.addStyleName("small");
        btSave.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                latexEditorRPC.callForSave(fileExplorer);
            }
        });

        Button btDefault = new Button();
        btDefault.setText("Root");
        btDefault.setStyleName("btn");
        btDefault.addStyleName("primary");
        btDefault.addStyleName("small");
        btDefault.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fileExplorer.setSelectAsDefault();

                //latexEditorRPC.callForSave(fileExplorer);
            }
        });

/*
        Button tooglePDF = new Button();
        tooglePDF.setText("PDF");
        tooglePDF.setStyleName("btn");
        tooglePDF.addStyleName("primary");
        final Frame pdfframe = new Frame("");
        final RootPanel pdfroot = RootPanel.get("pdfview");
        pdfroot.add(pdfframe);
        pdfroot.setVisible(false);

        tooglePDF.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                if (pdfroot.isVisible()) {
                    pdfroot.setVisible(false);
                } else {
                    pdfroot.setVisible(true);
                    pdfframe.setUrl("https://docs.google.com/gview?url=http://www.google.com/google-d-s/docsQuickstartGuide.pdf&chrome=true");
                    pdfframe.setSize("400px","100%");
                }
            }
        });
*/
        HorizontalPanel bts = new HorizontalPanel();
        bts.setBorderWidth(0);
        bts.add(btCompile);
        bts.add(btDefault);
        bts.add(btSave);

        leftBar.add(bts);

        ScrollPanelWrapper scrollP = new ScrollPanelWrapper(fileExplorer);


        leftBar.add(scrollP);


        RootPanel.get("files").add(leftBar);
    }

}
