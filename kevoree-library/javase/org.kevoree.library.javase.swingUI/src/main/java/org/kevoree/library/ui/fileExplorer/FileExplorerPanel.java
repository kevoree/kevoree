package org.kevoree.library.ui.fileExplorer;

import com.explodingpixels.macwidgets.plaf.ITunesTableUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 07/11/11
 * Time: 23:18
 * To change this template use File | Settings | File Templates.
 */
public class FileExplorerPanel extends JPanel {

    public FileExplorerPanel() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(600,600));
    }

    public void refresh(File root) {
        this.removeAll();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Name");
        model.addColumn("Size");
        if (root != null && root.isDirectory()) {
            File[] files = root.listFiles();
            for (int i = 0; i < files.length; i++) {
                String[] values = {files[i].getName(), files[i].getTotalSpace() + ""};
                model.addRow(values);
            }
        }
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setUI(new ITunesTableUI());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.add(scrollPane, BorderLayout.CENTER);
        this.repaint();
        this.revalidate();
    }

}
