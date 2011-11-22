package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import edu.ycp.cs.dh.acegwt.client.ace.AceEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/11/11
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public class latexEditorFileExplorer extends SimplePanel {

    private Tree tree = null;
    private HashMap<String, TreeItem> map;
    private AceEditor editor = null;

    public String getQualifiedName(TreeItem item) {
        if (item.getParentItem() != null) {
            return getQualifiedName(item.getParentItem()) + "/" + item.getText();
        } else {
            return item.getText();
        }
    }

    public latexEditorFileExplorer(AceEditor _editor) {
        editor = _editor;
        tree = new Tree();
        map = new HashMap<String, TreeItem>();
        add(tree);
        reloadFromServer();

        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> treeItemSelectionEvent) {
                displayFile(getQualifiedName(treeItemSelectionEvent.getSelectedItem()));
            }
        });
    }

    public void reloadFromServer() {

        tree.clear();
        map.clear();

        final List<String> flatFiles = new ArrayList<String>();
        String url = GWT.getModuleBaseURL() + "flatfiles";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error while connecting to server");
                }

                public void onResponseReceived(Request request, Response response) {
                    String[] files = response.getText().split(";");
                    for (int i = 0; i < files.length; i++) {
                        flatFiles.add(files[i]);
                    }

                    for (String flatFile : flatFiles) {

                        if (flatFile.contains("/")) {
                            String path = flatFile.substring(0, flatFile.lastIndexOf("/"));
                            TreeItem treeItem = null;
                            if (map.containsKey(path)) {
                                treeItem = map.get(path);
                            } else {
                                treeItem = new TreeItem(path);
                                map.put(path, treeItem);
                                tree.addItem(treeItem);
                            }
                            treeItem.addItem(flatFile.substring(flatFile.lastIndexOf("/") + 1));
                        } else {
                            tree.addItem(flatFile);
                        }
                    }

                }
            });

        } catch (Exception e) {
            Window.alert("Error while connecting to server");
        }

    }


    public void displayFile(String path) {

        String url = GWT.getModuleBaseURL() + "flatfile?file=" + path;
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error while connecting to server");
                }

                public void onResponseReceived(Request request, Response response) {
                    editor.setText(response.getText());
                }
            });

        } catch (Exception e) {
            Window.alert("Error while connecting to server");
        }
    }

}
