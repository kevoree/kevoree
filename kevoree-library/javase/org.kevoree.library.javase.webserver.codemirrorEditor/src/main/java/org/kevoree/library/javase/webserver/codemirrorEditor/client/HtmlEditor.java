package org.kevoree.library.javase.webserver.codemirrorEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;

public class HtmlEditor implements EntryPoint,MirrorEditorCallback{

	public void onModuleLoad() {
		
		/*Button b = new Button("ok");
		b.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				Window.alert(CodeMirrorEditorWrapper.getText());
				
			}
		});
		
		RootPanel.get().add(b);
		*/
		CodeMirrorEditorWrapper.addOnChangeHandler(this);
	}

	@Override
	public void invokeMirrorCallback(JavaScriptObject obj) {
		Window.alert(CodeMirrorEditorWrapper.getText());
		
	}

}
