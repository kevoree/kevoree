package org.kevoree.platform.osgi.standalone.gui;

import com.explodingpixels.macwidgets.plaf.HudLabelUI;
import org.kevoree.ContainerNode;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: duke
 * Date: 06/03/12
 * Time: 15:46
 */
public class KevoreeGuiHeader extends JPanel {

    private JLabel componentNumbers = new JLabel();

    public KevoreeGuiHeader() {
        URL urlIcon = getClass().getClassLoader().getResource("kevoree-logo-full.png");
        ImageIcon topIIcon = new ImageIcon(urlIcon);
        JLabel topImage = new JLabel(topIIcon);
        topImage.setOpaque(true);
        topImage.setBackground(new Color(63, 128, 187));
        this.setBackground(new Color(63, 128, 187));

        this.setLayout(new BorderLayout());
        this.add(topImage, BorderLayout.CENTER);

        componentNumbers = new JLabel();
        componentNumbers.setUI(new HudLabelUI());
        componentNumbers.setOpaque(false);

        JPanel layout = new JPanel();
        layout.setOpaque(false);
        layout.setLayout(new BoxLayout(layout,BoxLayout.PAGE_AXIS));
        layout.add(componentNumbers);

        this.add(layout, BorderLayout.EAST);
    }

    public void updateInfo(ContainerNode node) {
        componentNumbers.setText(node.getName()+"@host "+node.getComponentsForJ().size()+" components");
        componentNumbers.repaint();
        componentNumbers.revalidate();
        System.out.println("Hello serving " + node.getComponentsForJ().size());
    }

}
