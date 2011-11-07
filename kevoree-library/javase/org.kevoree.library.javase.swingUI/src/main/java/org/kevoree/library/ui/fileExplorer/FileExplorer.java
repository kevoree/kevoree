package org.kevoree.library.ui.fileExplorer;

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
public class FileExplorer extends AbstractComponentType {

    private JFrame frame = null;
    private FileExplorerPanel fileExplorer = null;

    @Start
    public void start() throws IOException {
        frame = new JFrame("Kevoree File Explorer - " + this.getName());
        if(this.getDictionary().get("basedir") != null){
            fileExplorer = new FileExplorerPanel(this.getDictionary().get("basedir").toString());
        } else {
            fileExplorer = new FileExplorerPanel("notfoundFileRoot");
        }
        frame.add(fileExplorer,BorderLayout.CENTER);
        frame.setVisible(true);
        frame.pack();
    }

    @Stop
    public void stop() {
        fileExplorer.kill();
        frame.dispose();
        frame = null;
    }

    @Update
    public void update() {

    }


    private class FileExplorerPanel extends JPanel implements Runnable{

        private SimpleFileManager simpleFileManager = null;
        private JTree tree = new JTree();

        public void kill(){
            simpleFileManager.stopMonitoring();
        }

        public FileExplorerPanel(String basefile) throws IOException {
            File root = new File(basefile);
            if (!root.exists()) {
                JFileChooser filechooser = new JFileChooser();
                filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                filechooser.setDialogTitle("Select base directory for Kevoree File Explorer ");
                int returnVal = filechooser.showOpenDialog(null);
                if (filechooser.getSelectedFile() != null && returnVal == JFileChooser.APPROVE_OPTION) {
                    root = filechooser.getSelectedFile();
                }
            }

            simpleFileManager = new SimpleFileManager(root, new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return true;
                }
            });
            this.setLayout(new BorderLayout());
            add(new JScrollPane(tree), BorderLayout.CENTER);
            tree.setModel(new DefaultTreeModel(simpleFileManager.getDirectoryTree()));
            simpleFileManager.startMonitoring();
            simpleFileManager.getFileMonitor().addClient(this);
            final File finalRoot = root;
            tree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent p1) {

                  TreePath path = p1.getNewLeadSelectionPath();
                  String file = path.getLastPathComponent().toString();
                  while (path.getParentPath() != null) {
                    path = path.getParentPath();
                    file = path.getLastPathComponent() + "/" + file;
                  }

                  File fileF = new File(finalRoot + "/" + file.substring(file.indexOf("/")));
                  if (fileF.isFile()) {
                    //val content = Source.fromFile(fileF.getAbsolutePath(), "utf-8").getLines().mkString("\n");
                    //editor.loadText(content, fileF)
                    //frame.setTitle("ThingML Editor : " + p1.getNewLeadSelectionPath().getLastPathComponent.toString);
                  }

                }
              }) ;

        }

        @Override
        public void run() {
            simpleFileManager.refresh();
            tree.setModel(new DefaultTreeModel(simpleFileManager.getDirectoryTree()));
        }
    }


}
