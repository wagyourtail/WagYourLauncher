/*
 * Created by JFormDesigner on Mon Dec 20 08:28:42 MST 2021
 */

package xyz.wagyourtail.launcher.swing.component.logging;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * @author unknown
 */
public class ProgressPanel extends JPanel {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTextPane textPane1;
    private JPanel panel1;
    public JProgressBar progressBar;
    private JLabel label1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public ProgressPanel() {
        initComponents();
    }

    private void progressBarStateChanged(ChangeEvent e) {
        label1.setText(Integer.toString(progressBar.getValue()) + "/" + 100);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        scrollPane1 = new JScrollPane();
        textPane1 = new LoggingTextArea(scrollPane1);
        panel1 = new JPanel();
        progressBar = new JProgressBar();
        label1 = new JLabel();

        //======== this ========
        setLayout(new BorderLayout(5, 5));

        //======== scrollPane1 ========
        {

            //---- textPane1 ----
            textPane1.setEditable(false);
            textPane1.setContentType("text/html");
            textPane1.setAutoscrolls(false);
            scrollPane1.setViewportView(textPane1);
        }
        add(scrollPane1, BorderLayout.CENTER);

        //======== panel1 ========
        {
            panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

            //---- progressBar ----
            progressBar.addChangeListener(e -> progressBarStateChanged(e));
            panel1.add(progressBar);

            //---- label1 ----
            label1.setText(bundle.getString("ProgressPanel.label1.text"));
            panel1.add(label1);
        }
        add(panel1, BorderLayout.SOUTH);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public LoggingTextArea getLogger() {
        return (LoggingTextArea) textPane1;
    }
}
