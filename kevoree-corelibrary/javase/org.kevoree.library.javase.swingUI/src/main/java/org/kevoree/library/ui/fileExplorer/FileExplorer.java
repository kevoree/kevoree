package org.kevoree.library.ui.fileExplorer;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.SourceListControlBar;
import org.kevoree.annotation.*;
import org.kevoree.framework.AbstractComponentType;
import org.kevoree.framework.MessagePort;
import org.kevoree.library.ui.layout.KevoreeLayout;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.io.File;
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
public class FileExplorer extends AbstractComponentType {

    //private JFrame frame = null;
    private DirectoryExplorerPanel dirExplorer = null;
    private FileExplorerPanel fileExplorer = null;

    @Start
    public void start() throws IOException {
        //frame = new JFrame("Kevoree File Explorer - " + this.getName());
        dirExplorer = new DirectoryExplorerPanel(this);
        fileExplorer = new FileExplorerPanel(this);
        if (this.getDictionary().get("basedir") != null) {
            dirExplorer.refresh(this.getDictionary().get("basedir").toString());
        } else {
            dirExplorer.refresh("notfoundFileRoot");
        }
        JScrollPane scrollPane = new JScrollPane(dirExplorer);
        scrollPane = MacWidgetFactory.makeSourceListScrollPane(scrollPane);
        IAppWidgetFactory.makeIAppScrollPane(scrollPane);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, scrollPane, fileExplorer);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(1);
        ((BasicSplitPaneUI) splitPane.getUI()).getDivider().setBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(0xa5a5a5)));
        splitPane.setBorder(BorderFactory.createEmptyBorder());


        SourceListControlBar controlBar = new SourceListControlBar();
        dirExplorer.sourceList.installSourceListControlBar(controlBar);
        controlBar.installDraggableWidgetOnSplitPane(splitPane);


        //frame.add(scrollPane, BorderLayout.WEST);
        //frame.add(splitPane, BorderLayout.CENTER);
        //frame.setVisible(true);
       // frame.pack();

        KevoreeLayout.getInstance().displayTab(splitPane,getName());

        splitPane.setDividerLocation(0.3);
    }

    public void directorySelected(File selected) {
        fileExplorer.refresh(selected);
        if (getPortByName("fileurl") != null) {
            getPortByName("fileurl", MessagePort.class).process(selected);
        }
    }

    public void fileSelected(File selected) {
        //frame.setTitle(selected.getName());
        if (getPortByName("fileurl") != null) {
            getPortByName("fileurl", MessagePort.class).process(selected);
        }
    }

    @Stop
    public void stop() {
        KevoreeLayout.getInstance().releaseTab(getName());
        //frame.dispose();
       // frame = null;
    }

    @Update
    public void update() throws IOException {
        stop();
        start();
    }


}
