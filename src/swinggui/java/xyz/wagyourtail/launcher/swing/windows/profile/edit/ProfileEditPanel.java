/*
 * Created by JFormDesigner on Fri Dec 24 10:04:15 MST 2021
 */

package xyz.wagyourtail.launcher.swing.windows.profile.edit;

import java.awt.event.*;

import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author unknown
 */
public class ProfileEditPanel extends JPanel {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton apply;
    private JLabel label1;
    private JTextField name;
    private JLabel label2;
    private JTextField jvmArgs;
    private JLabel label3;
    private JComboBox javaSelect;
    private JButton findJava;
    private JLabel label4;
    private JButton mcVersion;
    private JButton reset;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private LauncherBase launcher;

    public ProfileEditPanel(LauncherBase launcher) {
        this.launcher = launcher;
        initComponents();
    }

    private void apply(ActionEvent e) {
        // TODO add your code here
    }

    private void reset(ActionEvent e) {
        // TODO add your code here
    }

    private void findJava(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        apply = new JButton();
        label1 = new JLabel();
        name = new JTextField();
        label2 = new JLabel();
        jvmArgs = new JTextField();
        label3 = new JLabel();
        javaSelect = new JComboBox();
        findJava = new JButton();
        label4 = new JLabel();
        mcVersion = new JButton();
        reset = new JButton();

        //======== this ========

        //---- apply ----
        apply.setText(bundle.getString("ProfileEditPanel.apply.text"));
        apply.addActionListener(e -> apply(e));

        //---- label1 ----
        label1.setText(bundle.getString("ProfileEditPanel.label1.text"));

        //---- label2 ----
        label2.setText(bundle.getString("ProfileEditPanel.label2.text"));

        //---- label3 ----
        label3.setText(bundle.getString("ProfileEditPanel.label3.text"));

        //---- findJava ----
        findJava.setText(bundle.getString("ProfileEditPanel.findJava.text"));
        findJava.addActionListener(e -> findJava(e));

        //---- label4 ----
        label4.setText(bundle.getString("ProfileEditPanel.label4.text"));

        //---- reset ----
        reset.setText(bundle.getString("ProfileEditPanel.reset.text"));
        reset.addActionListener(e -> reset(e));

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(label1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(label4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(label2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(label3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(20, 20, 20)
                            .addGroup(layout.createParallelGroup()
                                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addComponent(javaSelect, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(findJava, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))
                                .addComponent(jvmArgs)
                                .addComponent(name)
                                .addComponent(mcVersion, GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGap(0, 366, Short.MAX_VALUE)
                            .addComponent(reset)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(apply)))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(name, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(label1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label4, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                        .addComponent(mcVersion))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(jvmArgs, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(label2, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label3, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                        .addComponent(findJava)
                        .addComponent(javaSelect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(apply)
                        .addComponent(reset))
                    .addContainerGap())
        );
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
}
