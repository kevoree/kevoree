package org.kevoree.library.javase.webserver.codemirrorEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class HtmlEditor implements EntryPoint{

	public void onModuleLoad() {
		
		Button b = new Button();
		b.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				TextAreaElement elem = TextAreaElement.as(RootPanel.get().get("code").getElement());
				Window.alert(elem.getInnerHTML());				
			}
		});
	}

}
