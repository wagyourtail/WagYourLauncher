/*
 * Created by JFormDesigner on Mon Dec 20 05:10:54 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.main;

import xyz.wagyourtail.launcher.gui.LauncherGui;
import xyz.wagyourtail.launcher.gui.windows.login.GuiLogin;
import xyz.wagyourtail.launcher.gui.windows.profile.create.GuiNewProfile;
import xyz.wagyourtail.launcher.minecraft.auth.common.GetProfile;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author wagyourtail
 */
public class GuiMainWindow extends JFrame {
    private final LauncherGui launcher;

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel3;
    private JPanel panel1;
    private JButton newProfile;
    private JPanel panel4;
    private JButton newAccount;
    private JComboBox accounts;
    private JPanel panel5;
    private JScrollPane scrollPane1;
    private JTree profileTree;
    private JPanel panel2;
    private JButton launch;
    private JButton launchoffline;
    private JButton profileView;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public GuiMainWindow(LauncherGui launcher) throws IOException {
        this.launcher = launcher;
        this.setVisible(true);
    }

    private void profileView(ActionEvent e) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) profileTree.getLastSelectedPathComponent());
        Profile profile = (Profile) node.getUserObject();
        if (profile == null) {
            return;
        }
        launcher.getGuiProfile(profile).setVisible(true);
    }

    private void newProfile(ActionEvent e) {
        if (launcher.newProfile == null) {
            launcher.newProfile = new GuiNewProfile(launcher);
        }
        launcher.newProfile.setVisible(true);
    }

    public void profileTreeValueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) profileTree.getLastSelectedPathComponent());
        if (node == null || !(node.getUserObject() instanceof Profile profile)) {
            profileTree.clearSelection();
            return;
        }
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        launcher.getLogger().trace("Selected profile: " + profile.name());
        if (launcher.profiles.getRunningProfiles().contains(profile)) {
            launch.setText(bundle.getString("GuiMainWindow.kill.text"));
            launchoffline.setText(bundle.getString("GuiMainWindow.forcekill.text"));
        } else {
            launch.setText(bundle.getString("GuiMainWindow.launch.text"));
            launchoffline.setText(bundle.getString("GuiMainWindow.launchoffline.text"));
        }

    }

    public void initComponents() throws IOException {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        panel3 = new JPanel();
        panel1 = new JPanel();
        newProfile = new JButton();
        panel4 = new JPanel();
        newAccount = new JButton();
        accounts = new JComboBox();
        panel5 = new JPanel();
        scrollPane1 = new JScrollPane();
        profileTree = new JTree();
        panel2 = new JPanel();
        launch = new JButton();
        launchoffline = new JButton();
        profileView = new JButton();

        //======== this ========
        setTitle("WagYourLauncher v1.0");
        var contentPane = getContentPane();

        //======== panel3 ========
        {
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));

            //======== panel1 ========
            {
                panel1.setLayout(new FlowLayout(FlowLayout.LEFT));

                //---- newProfile ----
                newProfile.setText(bundle.getString("GuiMainWindow.newProfile.text"));
                newProfile.addActionListener(e -> newProfile(e));
                panel1.add(newProfile);
            }
            panel3.add(panel1);

            //======== panel4 ========
            {
                panel4.setLayout(new FlowLayout(FlowLayout.RIGHT));

                //---- newAccount ----
                newAccount.setText(bundle.getString("GuiMainWindow.newaccount.text"));
                newAccount.addActionListener(e -> newAccount(e));
                panel4.add(newAccount);

                //---- accounts ----
                populateAccounts();
                panel4.add(accounts);
            }
            panel3.add(panel4);
        }

        //======== panel5 ========
        {

            //======== scrollPane1 ========
            {

                //---- profileTree ----
                profileTree.addTreeSelectionListener(e -> profileTreeValueChanged(e));
                populateProfiles();
                scrollPane1.setViewportView(profileTree);
            }

            //======== panel2 ========
            {
                panel2.setLayout(new GridBagLayout());
                ((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
                ((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
                ((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
                ((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

                //---- launch ----
                launch.setText(bundle.getString("GuiMainWindow.launch.text"));
                launch.addActionListener(e -> launch(e));
                panel2.add(launch, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- launchoffline ----
                launchoffline.setText(bundle.getString("GuiMainWindow.launchoffline.text"));
                launchoffline.addActionListener(e -> launchoffline(e));
                panel2.add(launchoffline, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

                //---- profileView ----
                profileView.setText(bundle.getString("GuiMainWindow.profileView.text"));
                profileView.addActionListener(e -> profileView(e));
                panel2.add(profileView, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }

            GroupLayout panel5Layout = new GroupLayout(panel5);
            panel5.setLayout(panel5Layout);
            panel5Layout.setHorizontalGroup(
                panel5Layout.createParallelGroup()
                    .addGroup(panel5Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
            );
            panel5Layout.setVerticalGroup(
                panel5Layout.createParallelGroup()
                    .addGroup(GroupLayout.Alignment.TRAILING, panel5Layout.createSequentialGroup()
                        .addGroup(panel5Layout.createParallelGroup()
                            .addComponent(panel2, GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE))
                        .addContainerGap())
            );
        }

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addComponent(panel3, GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                        .addComponent(panel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(panel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void populateProfiles() throws IOException {
        TreeModel model = getProfileTreeModel();
        profileTree.setModel(model);
        DefaultMutableTreeNode root = ((DefaultMutableTreeNode) model.getRoot());
        for (int i = 0; i < root.getChildCount(); i++) {
            profileTree.expandPath(new TreePath(root).pathByAddingChild(root.getChildAt(i)));
        }
    }

    public TreeModel getProfileTreeModel() throws IOException {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Profiles");

        DefaultMutableTreeNode vanilla = new DefaultMutableTreeNode("Main Directory");
        DefaultMutableTreeNode wyl = new DefaultMutableTreeNode("WagYourLauncher");
        DefaultMutableTreeNode other = new DefaultMutableTreeNode("Other");

        for (Map.Entry<String, Profile> profile : launcher.profiles.getAllProfiles().entrySet()) {
            if (profile.getValue().gameDir().toAbsolutePath().equals(launcher.minecraftPath.toAbsolutePath())) {
                vanilla.add(new DefaultMutableTreeNode(profile.getValue()));
            } else if (profile.getValue().gameDir().toAbsolutePath().normalize().startsWith(launcher.minecraftPath.resolve("profiles").toAbsolutePath().normalize())) {
                wyl.add(new DefaultMutableTreeNode(profile.getValue()));
            } else {
                launcher.getLogger().info("Profile gamedir: " + profile.getValue().gameDir().toAbsolutePath().normalize());
                launcher.getLogger().info("Minecraft path: " + launcher.minecraftPath.toAbsolutePath().normalize());
                other.add(new DefaultMutableTreeNode(profile.getValue()));
            }
        }

        if (vanilla.getChildCount() > 0) {
            root.add(vanilla);
        }
        if (wyl.getChildCount() > 0) {
            root.add(wyl);
        }
        if (other.getChildCount() > 0) {
            root.add(other);
        }
        return new DefaultTreeModel(root);
    }

    public void populateAccounts() throws IOException {
        DefaultComboBoxModel<AccountLabel> model = new DefaultComboBoxModel<>();
        accounts.removeAllItems();
        for (String account : launcher.auth.getRegisteredUsers().keySet()) {
            GetProfile.MCProfile profile;
            try {
                profile = launcher.auth.getProfile(launcher.getLogger(), account, true);
                if (profile == null) {
                    continue;
                }
            } catch (IOException | InterruptedException | UnrecoverableEntryException | CertificateException | KeyStoreException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new IOException(e);
            }
            BufferedImage image = null;
            try {
                image = ImageIO.read(profile.skin_url());
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedImage head = null;
            if (image != null) {
                // trim to face
                head = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        head.setRGB(x, y, image.getRGB(x + 8, y + 8));
                    }
                }
            }
            model.addElement(new AccountLabel(profile, head.getScaledInstance(20, 20, Image.SCALE_FAST)));
        }
        accounts.setModel(model);
        accounts.setRenderer(new AccoutLabelRenderer());
    }

    private void launch(ActionEvent e) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) profileTree.getLastSelectedPathComponent());
        if (node == null) {
            return;
        }
        Profile profile = (Profile) node.getUserObject();
        if (profile == null) {
            return;
        }
        if (!launcher.profiles.getRunningProfiles().contains(profile)) {
            launch(profile, false);
        } else {
            launcher.profiles.killRunning(profile);
        }
    }

    private void launchoffline(ActionEvent e) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) profileTree.getLastSelectedPathComponent());
        if (node == null) {
            return;
        }
        Profile profile = (Profile) node.getUserObject();
        if (profile == null) {
            return;
        }
        if (!launcher.profiles.getRunningProfiles().contains(profile)) {
            launch(profile, true);
        } else {
            launcher.profiles.forceKillRunning(profile);
        }
    }

    private void launch(Profile profile, boolean offline) {
        launcher.getGuiProfile(profile).launch(offline);
    }

    private void newAccount(ActionEvent e) {
        if (launcher.login != null) {
            launcher.login.setVisible(true);
            return;
        }
        launcher.login = new GuiLogin(launcher);
        launcher.login.setVisible(true);
    }

    public GetProfile.MCProfile getCurrentAccount() {
        return ((GuiMainWindow.AccountLabel) accounts.getSelectedItem()).profile;
    }

    public record AccountLabel(GetProfile.MCProfile profile, Image image) {
        @Override
        public String toString() {
            return profile.name();
        }

    }

    public static class AccoutLabelRenderer implements ListCellRenderer<AccountLabel> {

        @Override
        public Component getListCellRendererComponent(JList<? extends AccountLabel> list, AccountLabel value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return new JLabel("");
            }
            return new JLabel(value.toString(), new ImageIcon(value.image), JLabel.LEFT);
        }

    }
}
