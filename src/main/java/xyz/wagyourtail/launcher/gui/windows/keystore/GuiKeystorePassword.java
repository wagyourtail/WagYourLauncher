/*
 * Created by JFormDesigner on Mon Dec 20 07:09:29 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.keystore;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author wagyourtail
 */
public class GuiKeystorePassword extends JFrame {

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel buttonBar;
    private JLabel title;
    private JPasswordField passwordField1;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public final boolean newPassword;
    public final AtomicReference<char[]> password = new AtomicReference<>(null);

    public GuiKeystorePassword(boolean newPassword) {
        this.newPassword = newPassword;
        initComponents();
    }

    private void onOk(ActionEvent e) {
        password.set(passwordField1.getPassword());
        synchronized (this) {
            this.notifyAll();
        }
        dispose();
    }

    private void enterKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            onOk(null);
        }
    }

    private void passwordField1KeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            onOk(null);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        title = new JLabel();
        passwordField1 = new JPasswordField();
        okButton = new JButton();

        //======== this ========
        setTitle("Keystore Login");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                enterKeyPressed(e);
            }
        });
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- title ----
                title.setText(bundle.getString("KeystorePassword.title.text"));
                updateTitle(title);
                buttonBar.add(title, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- passwordField1 ----
                passwordField1.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        passwordField1KeyPressed(e);
                    }
                });
                buttonBar.add(passwordField1, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- okButton ----
                okButton.setText(bundle.getString("KeystorePassword.okButton.text"));
                okButton.addActionListener(e -> onOk(e));
                buttonBar.add(okButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.CENTER);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    private void updateTitle(JLabel title) {
        if (newPassword) {
            ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
            title.setText(bundle.getString("KeystorePassword.title.textNew"));
        }
    }
}
