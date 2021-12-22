/*
 * Created by JFormDesigner on Mon Dec 20 08:58:38 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.login;

import java.awt.event.*;
import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.gui.LauncherGui;
import xyz.wagyourtail.launcher.gui.component.logging.LoggingTextArea;
import xyz.wagyourtail.launcher.gui.component.logging.ProgressPanel;

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
public class GuiLogin extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    public JPanel contentPanel;
    private JPanel panel1;
    private JPanel buttonBar;
    private JButton cancelButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private final LauncherGui launcher;

    public GuiLogin(LauncherGui launcher) {
        this.launcher = launcher;
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
        panel1.add(new SelectType(this, launcher));
        pack();
    }

    public void step2(String selection) {
        panel1.remove(0);
        panel1.add(new ProgressPanel());
        this.setMinimumSize(new Dimension(600, 600));
        pack();
        CompletableFuture.runAsync(() -> {
            try {
                launcher.auth.authProviders.get(selection).withLogger(this.getLogger(), ((ProgressPanel) this.panel1.getComponent(0)).progressBar);
                if (launcher.mainWindow != null) {
                    launcher.mainWindow.populateAccounts();
                    this.dispose();
                    LoggingTextArea.removeLogger(this.getLogger());
                    launcher.login = null;
                }
            } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException | InvalidKeySpecException | InterruptedException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

    public Logger getLogger() {
        return ((ProgressPanel) panel1.getComponent(0)).getLogger();
    }
}
