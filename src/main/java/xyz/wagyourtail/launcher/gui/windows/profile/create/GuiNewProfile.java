/*
 * Created by JFormDesigner on Mon Dec 20 15:59:10 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.profile.create;

import java.awt.event.*;
import javax.swing.event.*;
import xyz.wagyourtail.launcher.gui.LauncherGui;
import xyz.wagyourtail.launcher.gui.windows.profile.create.versions.InstalledVersion;
import xyz.wagyourtail.launcher.gui.windows.profile.create.versions.VanillaVersion;
import xyz.wagyourtail.launcher.gui.windows.profile.create.versions.VersionSelector;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author unknown
 */
public class GuiNewProfile extends JFrame {
    public final LauncherGui launcher;

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane1;
    private JPanel vanillaPanel;
    private JPanel installedPanel;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private JPanel panel1;
    private JLabel label1;
    private JTextField nameField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public GuiNewProfile(LauncherGui launcher) {
        this.launcher = launcher;
        initComponents();
    }

    private void tabbedPane1StateChanged(ChangeEvent e) {
        if (!((VersionSelector) tabbedPane1.getSelectedComponent()).isInitialized()) {
            ((VersionSelector) tabbedPane1.getSelectedComponent()).init();
        }
    }

    private void ok(ActionEvent e) {
        ((VersionSelector) tabbedPane1.getSelectedComponent()).create();
    }

    private void cancel(ActionEvent e) {
        this.setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        tabbedPane1 = new JTabbedPane();
        vanillaPanel = new JPanel();
        installedPanel = new JPanel();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        panel1 = new JPanel();
        label1 = new JLabel();
        nameField = new JTextField();

        //======== this ========
        setTitle("Create Profile");
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

                //======== tabbedPane1 ========
                {
                    tabbedPane1.setTabPlacement(SwingConstants.LEFT);
                    tabbedPane1.addChangeListener(e -> tabbedPane1StateChanged(e));

                    //======== vanillaPanel ========
                    {
                        vanillaPanel.setLayout(new GridLayout());
                        vanillaPanel=new VanillaVersion(this);
                    }
                    tabbedPane1.addTab(bundle.getString("GuiNewProfile.panel3.tab.title"), vanillaPanel);

                    //======== installedPanel ========
                    {
                        installedPanel.setLayout(new GridLayout());
                        installedPanel = new InstalledVersion(this);
                    }
                    tabbedPane1.addTab(bundle.getString("GuiNewProfile.panel2.tab.title"), installedPanel);
                }
                contentPanel.add(tabbedPane1);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setText(bundle.getString("GuiNewProfile.okButton.text"));
                okButton.addActionListener(e -> ok(e));
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText(bundle.getString("GuiNewProfile.cancelButton.text"));
                cancelButton.addActionListener(e -> cancel(e));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //======== panel1 ========
            {
                panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

                //---- label1 ----
                label1.setText(bundle.getString("GuiNewProfile.label1.text"));
                panel1.add(label1);
                panel1.add(nameField);
            }
            dialogPane.add(panel1, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents

        tabbedPane1StateChanged(null);
    }

    public String getProfileName() {
        return nameField.getText();
    }
}
