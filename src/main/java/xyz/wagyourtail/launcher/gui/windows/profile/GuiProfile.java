/*
 * Created by JFormDesigner on Mon Dec 20 09:00:58 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.profile;

import java.awt.event.*;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.gui.LauncherGui;
import xyz.wagyourtail.launcher.gui.component.logging.LoggingTextArea;
import xyz.wagyourtail.launcher.gui.windows.main.GuiMainWindow;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author unknown
 */
public class GuiProfile extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JTabbedPane tabs;
    private JPanel panel1;
    private JScrollPane scrollPane1;
    private JTextPane currentLogs;
    private JPanel buttonBar;
    private JButton launchBtn;
    private JButton launchOfflineBtn;
    private JButton closeButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private Profile profile;
    private LauncherGui launcher;
    public GuiProfile(LauncherGui launcher, Profile p) {
        this.launcher = launcher;
        this.profile = p;
        initComponents();
    }

    private void launchBtn(ActionEvent e) {
        launch(false);
    }

    private void launchOfflineBtn(ActionEvent e) {
        launch(true);
    }

    private void close(ActionEvent e) {
        this.setVisible(false);
    }

    private void initComponents() {
        this.setMinimumSize(new Dimension(800, 600));
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        tabs = new JTabbedPane();
        panel1 = new JPanel();
        scrollPane1 = new JScrollPane();
        currentLogs = new LoggingTextArea(scrollPane1);
        buttonBar = new JPanel();
        launchBtn = new JButton();
        launchOfflineBtn = new JButton();
        closeButton = new JButton();

        //======== this ========
        setTitle("Profile");
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

                //======== tabs ========
                {
                    tabs.setTabPlacement(SwingConstants.LEFT);

                    //======== panel1 ========
                    {
                        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));

                        //======== scrollPane1 ========
                        {

                            //---- currentLogs ----
                            currentLogs.setContentType("text/html");
                            currentLogs.setEditable(false);
                            currentLogs.setAutoscrolls(false);
                            scrollPane1.setViewportView(currentLogs);
                        }
                        panel1.add(scrollPane1);
                    }
                    tabs.addTab(bundle.getString("GuiProfile.panel1.tab.title"), panel1);
                }
                contentPanel.add(tabs);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};

                //---- launchBtn ----
                launchBtn.setText(bundle.getString("GuiProfile.launchBtn.text"));
                launchBtn.addActionListener(e -> launchBtn(e));
                buttonBar.add(launchBtn, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- launchOfflineBtn ----
                launchOfflineBtn.setText(bundle.getString("GuiProfile.launchOfflineBtn.text"));
                launchOfflineBtn.addActionListener(e -> launchOfflineBtn(e));
                buttonBar.add(launchOfflineBtn, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- closeButton ----
                closeButton.setText(bundle.getString("GuiProfile.closeButton.text"));
                closeButton.addActionListener(e -> close(e));
                buttonBar.add(closeButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public Logger getLogger() {
        return ((LoggingTextArea) currentLogs);
    }

    public void launch(boolean offline) {
        if (launcher.profiles.getRunningProfiles().contains(profile)) {
            if (offline) {
                launcher.profiles.forceKillRunning(profile);
            } else {
                launcher.profiles.killRunning(profile);
            }
        } else {
            try {
                launcher.launch(profile, launcher.mainWindow.getCurrentAccount().name(), offline);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public void updateLaunched() {
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        if (launcher.profiles.getRunningProfiles().contains(profile)) {
            launchBtn.setText(bundle.getString("GuiProfile.killBtn.text"));
            launchOfflineBtn.setText(bundle.getString("GuiProfile.forceKillBtn.text"));
            launchOfflineBtn.setEnabled(false);
        } else {
            launchBtn.setText(bundle.getString("GuiProfile.launchBtn.text"));
            launchOfflineBtn.setText(bundle.getString("GuiProfile.launchOfflineBtn.text"));
            launchOfflineBtn.setEnabled(true);
        }
    }

    public GuiProfile setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }
}
