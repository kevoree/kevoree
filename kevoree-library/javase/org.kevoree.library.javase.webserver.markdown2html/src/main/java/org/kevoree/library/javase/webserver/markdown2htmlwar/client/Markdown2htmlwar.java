package org.kevoree.library.javase.webserver.markdown2htmlwar.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Markdown2htmlwar implements EntryPoint {
	/**
	 * This is the entry point method.
	 */
	
	
	
	public void onModuleLoad() {
		
/*		Button b = new Button("ok");
		final TextBox box = new TextBox();
		
		b.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				serv.markdown2html(box.getText(), new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String arg0) {
						Window.alert(arg0);
					}
					
					@Override
					public void onFailure(Throwable arg0) {
						Window.alert("erreur");
					}
				});
			}
		});
		
		
		RootPanel.get().add(box);*/
		RootPanel.get().add(new MainWindows());
		
		
	}
}
