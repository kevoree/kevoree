package org.kevoree.library.javase.webserver.markdown2htmlwar.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.Widget;

	 class MainWindows extends Composite {

	private static MainWindowsUiBinder uiBinder = GWT
			.create(MainWindowsUiBinder.class);
	@UiField RichTextArea richTextArea;
	@UiField HTML html;

	private final MarkDown2HtmlServiceAsync serv = GWT.create(MarkDown2HtmlService.class);

	
	interface MainWindowsUiBinder extends UiBinder<Widget, MainWindows> {
	}

	public MainWindows() {
		initWidget(uiBinder.createAndBindUi(this));
		
		
	}




	 	@UiHandler("richTextArea")
	 	void onRichTextAreaKeyPress(KeyPressEvent event) {
	 		serv.markdown2html(richTextArea.getText() + event.getCharCode(), new AsyncCallback<String>() {
				
				@Override
				public void onSuccess(String arg0) {
					html.setHTML(arg0);	
				}
				
				@Override
				public void onFailure(Throwable arg0) {
					Window.alert("toto");
					
				}
			});
	 		
	 	}
}
