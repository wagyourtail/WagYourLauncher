/*
 * Created by JFormDesigner on Mon Dec 20 08:58:38 MST 2021
 */

package xyz.wagyourtail.launcher.swing.screen.login;

import java.awt.event.*;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.launcher.gui.screen.login.UsernamePasswordScreen;
import xyz.wagyourtail.launcher.swing.component.logging.ProgressPanel;
import xyz.wagyourtail.launcher.swing.screen.BaseSwingScreen;
import xyz.wagyourtail.launcher.swing.screen.main.GuiMainWindow;
import xyz.wagyourtail.notlog4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author unknown
 */
public class GuiLogin extends BaseSwingScreen implements AddAccountScreen {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    public JPanel contentPanel;
    private JPanel panel1;
    private JPanel buttonBar;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
    public GuiLogin(LauncherBase launcher, MainScreen mainWindow) {
        super(launcher, mainWindow);
        initComponents();
        step1();
    }

    private void cancel(ActionEvent e) {
        this.setVisible(false);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        buttonBar = new JPanel();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Login");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

                //======== panel1 ========
                {
                    panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
                }
                contentPanel.add(panel1);
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- cancelButton ----
                cancelButton.setText(bundle.getString("GuiLogin.cancelButton.text"));
                cancelButton.addActionListener(e -> cancel(e));
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
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

    public void step1() {
        panel1.add(new SelectType(this, launcher, getProviders()));
        pack();
    }

    public void step2(String selection) {
        panel1.remove(0);
        panel1.add(new ProgressPanel());
        this.setMinimumSize(new Dimension(600, 600));
        pack();
        CompletableFuture.runAsync(() -> {
            runLogin(selection);
            launcher.refreshAccounts();
            this.close();
        });
    }

    @Override
    public Logger getLogger() {
        return ((ProgressPanel) panel1.getComponent(0)).getLogger();
    }

    @Override
    public void setProgress(int progress) {
        ((ProgressPanel) panel1.getComponent(0)).progressBar.setValue(progress);
    }

    @Override
    public UsernamePasswordScreen getUsernamePassword() {
        return new GuiUsernamePassword(getLauncher(), getMainWindow());
    }

}
