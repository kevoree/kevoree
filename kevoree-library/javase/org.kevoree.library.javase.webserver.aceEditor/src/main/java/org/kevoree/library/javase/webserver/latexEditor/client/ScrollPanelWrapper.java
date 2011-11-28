package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 27/11/11
 * Time: 22:16
 * To change this template use File | Settings | File Templates.
 */
class ScrollPanelWrapper extends SimplePanel {

   private ScrollPanel innerPanel;

   public ScrollPanelWrapper(Widget child) {
      innerPanel = new ScrollPanel(child);
      setWidget(innerPanel);
   }

   public void adjust() {
      innerPanel.setPixelSize(getOffsetWidth(), getOffsetHeight());
   }
}