package org.kevoree.library.ui.fileExplorer;

import com.explodingpixels.macwidgets.IAppWidgetFactory;
import com.explodingpixels.macwidgets.plaf.ITunesTableUI;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */
public class FileExplorerPanel extends JPanel {

    public FileExplorerPanel(FileExplorer exp) {
        explorer = exp;
        filesCache = new HashMap<Integer, File>();
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(600, 600));
    }

    private HashMap<Integer, File> filesCache = null;
    private FileExplorer explorer;

    public void refresh(File root) {
        this.removeAll();
        filesCache.clear();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Name");
        model.addColumn("Size");
        if (root != null && root.isDirectory() && !root.getName().startsWith(".")) {
            File[] files = root.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getName().startsWith(".")) {
                    String[] values = {files[i].getName(), files[i].getTotalSpace() + ""};
                    model.addRow(values);
                    filesCache.put(i, files[i]);
                }
            }
        }
        JTable table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setUI(new ITunesTableUI());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        IAppWidgetFactory.makeIAppScrollPane(scrollPane);
        this.add(scrollPane, BorderLayout.CENTER);
        this.repaint();
        this.revalidate();

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                explorer.fileSelected(filesCache.get(listSelectionEvent.getFirstIndex()));
            }
        });


    }

}
