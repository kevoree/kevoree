package org.kevoree.library.javase.webserver.codemirrorEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class HtmlEditor implements EntryPoint,MirrorEditorCallback{

	
	SendContentAsync service = GWT.create(SendContent.class);
	public void onModuleLoad() {
		CodeMirrorEditorWrapper.addOnChangeHandler(this);
	}

	@Override
	public void invokeMirrorCallback(JavaScriptObject obj) {
		service.sendHtmlContent(CodeMirrorEditorWrapper.getText(), new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable arg0) {
			}

			@Override
			public void onSuccess(Void arg0) {
			}});
		
		//Window.alert(CodeMirrorEditorWrapper.getText());
		
	}

}
