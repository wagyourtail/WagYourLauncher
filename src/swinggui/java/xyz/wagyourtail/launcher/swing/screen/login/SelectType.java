/*
 * Created by JFormDesigner on Mon Dec 20 09:06:13 MST 2021
 */

package xyz.wagyourtail.launcher.swing.screen.login;

import java.awt.event.*;
import xyz.wagyourtail.launcher.LauncherBase;

import java.util.*;
import javax.swing.*;

/**
 * @author unknown
 */
public class SelectType extends JPanel {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JComboBox comboBox1;
    private JButton button1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private final LauncherBase launcher;
    private final GuiLogin parent;

    private final String[] types;

    public SelectType(GuiLogin parent, LauncherBase launcher, String[] types) {
        this.launcher = launcher;
        this.parent = parent;
        this.types = types;
        initComponents();
    }

    private void button1(ActionEvent e) {
        parent.step2(comboBox1.getSelectedItem().toString());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        comboBox1 = new JComboBox();
        button1 = new JButton();

        //======== this ========

        //---- comboBox1 ----
        populateBox(comboBox1);

        //---- button1 ----
        button1.setText(bundle.getString("SelectType.button1.text"));
        button1.addActionListener(e -> button1(e));

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(comboBox1, GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(button1, GroupLayout.PREFERRED_SIZE, 155, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(button1, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                        .addComponent(comboBox1, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                    .addContainerGap())
        );
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private void populateBox(JComboBox box) {
        for (String s : types) {
            box.addItem(s);
        }
    }
}
