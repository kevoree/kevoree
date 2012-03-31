package org.kevoree.library.javase.webserver.codemirrorEditor.client;

/**
 * Created by IntelliJ IDEA. User: duke Date: 24/11/11 Time: 11:28 To change
 * this template use File | Settings | File Templates.
 */
public class CodeMirrorEditorWrapper {

	public static native String getText() /*-{
		return $wnd.codeMirrorEditor.getValue();
	}-*/;

	public static native void setText(String text) /*-{
		$wnd.codeMirrorEditor.setValue(text);
	}-*/;

	public static native void addOnChangeHandler(MirrorEditorCallback callback)
	/*-{ $wnd.codeMirrorEditor.on("change", function (e) {
	  callback.@org.kevoree.library.javase.webserver.codemirrorEditor.client.MirrorEditorCallback::invokeMirrorCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(e);
	  }); }-*/;
	 

	// private static native void nativeReplaceSelection(JavaScriptObject cm,
	// String text) /*-{
	// cm.replaceSelection(text);
	// }-*/;
	//
	//
	// private static native void nativeFocus(JavaScriptObject cm) /*-{
	// cm.focus();
	// }-*/;
	//
	// private static native void nativeUndo(JavaScriptObject cm) /*-{
	// cm.undo();
	// }-*/;
	//
	//
	// private static native void nativeRedo(JavaScriptObject cm) /*-{
	// cm.redo();
	// }-*/;
	//
	//
	// private static native void nativeRefresh(JavaScriptObject cm) /*-{
	// cm.refresh();
	// }-*/;
	//
	// private static native void nativeSetMarker(JavaScriptObject cm, int line,
	// String id, String text, String style) /*-{
	// if (style == null) {
	// if (id == null) {
	// cm.setMarkerData(line, {text: text });
	// } else {
	// cm.setMarkerData(line, {text: text, id: id});
	// }
	// } else {
	// if (id == null) {
	// cm.setMarkerData(line, {text: text, style: style});
	// } else {
	// cm.setMarkerData(line, {text: text, id: id, style: style});
	// }
	//
	// }
	// }-*/;
	//
	//
	// private static native void nativeClearMarker(JavaScriptObject cm, int
	// line) /*-{
	// cm.clearMarker(line);
	// }-*/;
	//
	// private static native JavaScriptObject nativeMarkText(JavaScriptObject
	// cm, LinePosition from, LinePosition to, String style) /*-{
	// var fromLine =
	// from.@org.gwtoolbox.codeditor.codemirror.client.LinePosition::line()();
	// var fromCh =
	// from.@org.gwtoolbox.codeditor.codemirror.client.LinePosition::column()();
	// var jsFrom = { line: fromLine, ch: fromCh };
	//
	// var toLine =
	// to.@org.gwtoolbox.codeditor.codemirror.client.LinePosition::line()();
	// var toCh =
	// to.@org.gwtoolbox.codeditor.codemirror.client.LinePosition::column()();
	// var jsTo = { line: toLine, ch: toCh };
	//
	// return cm.markText(jsFrom, jsTo, style);
	// }-*/;
	//
	// private static native void nativeSetLineStyle(JavaScriptObject cm, int
	// line, String style) /*-{
	// cm.setLineClass(line, style);
	// }-*/;
	//
	// private static native int nativeLineCount(JavaScriptObject cm) /*-{
	// return cm.lineCount();
	// }-*/;
	//
	// private static native void nativeSetCursor(JavaScriptObject cm, int line,
	// int column) /*-{
	// return cm.setCursor(line, column);
	// }-*/;

}
