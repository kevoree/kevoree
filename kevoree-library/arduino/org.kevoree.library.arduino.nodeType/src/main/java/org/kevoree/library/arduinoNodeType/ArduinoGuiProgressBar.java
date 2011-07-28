package org.kevoree.library.arduinoNodeType;

import java.awt.*;
import javax.swing.*;

public class ArduinoGuiProgressBar extends JPanel {

    private JProgressBar progressBar;
    private JTextArea taskOutput;

    public ArduinoGuiProgressBar() {
        super(new BorderLayout());

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(10, 35);
        taskOutput.setMargin(new Insets(5, 5, 5, 5));
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        //panel.add(cancelButton);
        panel.add(progressBar);

        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public void beginTask(String taskName, Integer progress) {
        taskOutput.append(taskName+"...");
        progressBar.setValue(progress);
    }

    public void endTask() {
        taskOutput.append("OK\n");
    }
    public void failTask() {
        taskOutput.append("Failed !\n");
    }


/*
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ProgressBarDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new ArduinoGuiProgressBar();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }  */
}