package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 24/11/11
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 */
public class latexEditorRPC {

	public static void callForSave (latexEditorFileExplorer explorer) {
		latexEditorService.App.getInstance().saveFile(explorer.getSelectedFilePath(), AceEditorWrapper.getText(), new AsyncCallback<Boolean>() {
			@Override
			public void onFailure (Throwable caught) {
				Window.alert("Error while saving file");
			}

			@Override
			public void onSuccess (Boolean result) {
				//NOOP
			}
		});
	}

	public static void callForCompile (final latexEditorFileExplorer explorer) {
		final JavaScriptObject window = newWindow("", null, null);
		latexEditorService.App.getInstance().saveFile(explorer.getSelectedFilePath(), AceEditorWrapper.getText(), new AsyncCallback<Boolean>() {
			@Override
			public void onFailure (Throwable caught) {
				Window.alert("Error while saving file");
			}

			@Override
			public void onSuccess (Boolean result) {
				final String selectedPath = explorer.getSelectedCompileRootFilePath();
				if (selectedPath.equals("") || selectedPath.equals(null)) {
					Window.alert("Please select root file");
					return;
				}
				try {
					latexEditorService.App.getInstance().compile(selectedPath, new AsyncCallback<String>() {
						@Override
						public void onFailure (Throwable caught) {
							Window.alert("Error while connecting to server");
						}

						@Override
						public void onSuccess (final String compileUUID) {
							final boolean[] compileresult = {false};
							final int[] nbtry = {0};
							Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
								@Override
								public boolean execute () {
									if (nbtry[0] > 10) {
										return false;
									}
									if (compileresult[0]) {
										return false;
									}
									latexEditorService.App.getInstance().compileresult(compileUUID, new AsyncCallback<String[]>() {
										@Override
										public void onFailure (Throwable caught) {
											nbtry[0] = nbtry[0] + 1;
										}

										@Override
										public void onSuccess (String[] result) {
											if (result.length != 2) {
												nbtry[0] = nbtry[0] + 1;
											} else {
												if (result[0].trim().equals("true")) {
													String pdfpath = GWT.getModuleBaseURL() + "rawfile?file=" + selectedPath.replace(".tex", ".pdf");
													setWindowTarget(window, pdfpath);
													compileresult[0] = true;
												} else {
													String pdfpath = GWT.getModuleBaseURL() + "rawfile?file=" + selectedPath.replace(".tex", ".log");
													setWindowTarget(window, pdfpath);
													compileresult[0] = true;
												}
											}
										}
									});
									return true;
								}
							}, 1500);
						}
					});
				} catch (Exception e) {
					Window.alert(e.getMessage());
				}
			}
		});


	}


	public static native JavaScriptObject newWindow (String url, String
			name, String features)/*-{
        var window = $wnd.open(url, name, features);
        return window;
    }-*/;

	public static native void setWindowTarget (JavaScriptObject window,
			String target)/*-{
        window.location = target;
    }-*/;


}
