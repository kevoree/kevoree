package org.kevoree.library.javase.webserver.latexEditor.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 21/11/11
 * Time: 21:20
 * To change this template use File | Settings | File Templates.
 */
public class latexEditorFileExplorer extends SimplePanel implements AceEditorCallback {

    private Tree tree = null;
    private HashMap<String, TreeItem> map;
    private HashMap<TreeItem, String> mapInv;
    private Boolean[] currentModified = {false};

    public String getQualifiedName(TreeItem item) {
        if (item.getParentItem() != null) {
            return getQualifiedName(item.getParentItem()) + "/" + mapInv.get(item);
        } else {
            return mapInv.get(item);
        }
    }

    private TreeItem previousSelected = null;

    public void initAutoSaveLoop(final latexEditorFileExplorer selfExplorer) {
        Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                if (currentModified[0] == true) {
                    latexEditorRPC.callForSave(selfExplorer);
                    currentModified[0] = false;
                }
                return true;
            }
        }, 3000);


    }


    public latexEditorFileExplorer() {
        tree = new Tree();
        map = new HashMap<String, TreeItem>();
        mapInv = new HashMap<TreeItem, String>();
        previousSelected = null;
        compileRoot = null;
        add(tree);
        reloadFromServer();
        initAutoSaveLoop(this);
        AceEditorWrapper.addOnChangeHandler(this);
        tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
            @Override
            public void onSelection(SelectionEvent<TreeItem> treeItemSelectionEvent) {
                if (previousSelected != null) {
                    if (previousSelected.equals(compileRoot)) {
                        previousSelected.setHTML("<span class=\"label warning\">" + mapInv.get(previousSelected) + "</span>");
                    } else {
                        previousSelected.setHTML(mapInv.get(previousSelected));
                    }
                }
                if (treeItemSelectionEvent.getSelectedItem().equals(compileRoot)) {
                    treeItemSelectionEvent.getSelectedItem().setHTML("<span class=\"label success\">" + mapInv.get(treeItemSelectionEvent.getSelectedItem()) + "</span>");
                } else {
                    treeItemSelectionEvent.getSelectedItem().setHTML("<span class=\"label notice\">" + mapInv.get(treeItemSelectionEvent.getSelectedItem()) + "</span>");
                }
                String qname = getQualifiedName(treeItemSelectionEvent.getSelectedItem());
                if (!qname.startsWith("/")) {
                    displayFile("/" + qname);
                } else {
                    displayFile(qname);
                }
                previousSelected = treeItemSelectionEvent.getSelectedItem();
            }
        });
    }

    private String selectedFilePath = "";

    public String getSelectedFilePath() {
        return selectedFilePath;
    }

    private String selectedCompileRootFilePath = "";

    private TreeItem compileRoot = null;

    public String getSelectedCompileRootFilePath() {
        return selectedCompileRootFilePath;
    }

    public void setSelectAsDefault() {

        if (compileRoot != null) {
            compileRoot.setHTML(mapInv.get(compileRoot));
        }

        String qname = getQualifiedName(tree.getSelectedItem());
        if (!qname.startsWith("/")) {
            selectedCompileRootFilePath = "/" + qname;
        } else {
            selectedCompileRootFilePath = qname;
        }

        tree.getSelectedItem().setHTML("<span class=\"label success\">" + mapInv.get(tree.getSelectedItem()) + "</span>");
        compileRoot = tree.getSelectedItem();
    }

    public void reloadFromServer() {

        tree.clear();
        map.clear();
        mapInv.clear();

        latexEditorService.App.getInstance().getFlatFiles(new AsyncCallback<Set<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error listing files from server");
            }

            @Override
            public void onSuccess(Set<String> flatFiles) {
                for (String flatFile : flatFiles) {
                    if (flatFile.contains("/")) {
                        String path = flatFile.substring(0, flatFile.lastIndexOf("/"));
                        if (path.equals("")) {
                            if (flatFile.lastIndexOf("/") + 1 < flatFile.length()) {
                                if (!selectedCompileRootFilePath.equals(flatFile.substring(flatFile.lastIndexOf("/") + 1))) {
                                    mapInv.put(tree.addItem(flatFile.substring(flatFile.lastIndexOf("/") + 1)), flatFile.substring(flatFile.lastIndexOf("/") + 1));
                                } else {
                                    mapInv.put(tree.addItem("<span class=\"label warning\">" + flatFile.substring(flatFile.lastIndexOf("/") + 1) + "</span>"), flatFile.substring(flatFile.lastIndexOf("/") + 1));
                                }
                            }
                        } else {
                            TreeItem treeItem = null;
                            if (map.containsKey(path)) {
                                treeItem = map.get(path);
                            } else {
                                treeItem = new TreeItem(path);
                                map.put(path, treeItem);
                                mapInv.put(treeItem, path);
                                tree.addItem(treeItem);
                            }
                            mapInv.put(treeItem.addItem(flatFile.substring(flatFile.lastIndexOf("/") + 1)), flatFile.substring(flatFile.lastIndexOf("/") + 1));
                        }
                    } else {
                        if (!selectedCompileRootFilePath.equals(flatFile)) {
                            mapInv.put(tree.addItem(flatFile), flatFile);
                        } else {
                            mapInv.put(tree.addItem("<span class=\"label warning\">" + flatFile + "</span>"), flatFile);
                        }
                    }
                }
            }
        });
    }


    public void displayFile(final String path) {

        latexEditorService.App.getInstance().getFileContent(path, true, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error while connecting to server to get file content");
            }

            @Override
            public void onSuccess(String result) {
                selectedFilePath = path;
                AceEditorWrapper.setText(result);
                currentModified[0] = false;
            }
        });
    }

    @Override
    public void invokeAceCallback(JavaScriptObject obj) {
        if (currentModified[0] == false) {
            //TODO ACQUIRE LOCK
            currentModified[0] = true;
        } else {
            //WAITING FOR AUTO SAVE LOOP TO DO THE JOB
        }


    }
}
