package org.kevoree.library.ui.fileExplorer;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 17:13
 * To change this template use File | Settings | File Templates.
 */

@MessageTypes({
        @MessageType(name = "fileurlmsg", elems = {@MsgElem(name = "url", className = String.class)})
})
@Requires({
        @RequiredPort(name = "fileurl", type = PortType.MESSAGE, optional = true, messageType = "fileurlmsg")
})
@DictionaryType({
        @DictionaryAttribute(name = "basedir", optional = true)})
@Library(name = "JavaSE")
@ComponentType
public class DirectoryExplorer extends AbstractComponentType {

    private JFrame frame = null;
    private DirectoryExplorerPanel fileExplorer = null;

    @Start
    public void start() throws IOException {
        frame = new JFrame("Kevoree File Explorer - " + this.getName());
        fileExplorer = new DirectoryExplorerPanel(this);
        if (this.getDictionary().get("basedir") != null) {
            fileExplorer.refresh(this.getDictionary().get("basedir").toString());
        } else {
            fileExplorer.refresh("notfoundFileRoot");
        }
        JScrollPane scrollPane = new JScrollPane(fileExplorer);
        IAppWidgetFactory.makeIAppScrollPane(scrollPane);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
        frame.pack();
    }

    @Stop
    public void stop() {
        frame.dispose();
        frame = null;
    }

    @Update
    public void update() throws IOException {
        stop();
        start();
    }


}
