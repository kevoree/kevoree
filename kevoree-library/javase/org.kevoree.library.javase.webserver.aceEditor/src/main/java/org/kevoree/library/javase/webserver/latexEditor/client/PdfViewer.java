package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.canvas.client.Canvas;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 23/11/11
 * Time: 23:06
 * To change this template use File | Settings | File Templates.
 */
public class PdfViewer {

    public static native void display(String pdfpath,Canvas canvas) /*-{
        'use strict';
        $wnd.PDFJS.getPdf(pdfpath, function getPdfHelloWorld(data) {

            $wnd.alert('rraa')

          var pdf = new $wnd.PDFJS.PDFDoc(data);

            $wnd.alert('kjflsdkj')

          var page = pdf.getPage(1);
          var scale = 1;
          var context = canvas.getContext('2d');
          canvas.height = page.height * scale;
          canvas.width = page.width * scale;
          page.startRendering(context);
        });
    }-*/;

}
