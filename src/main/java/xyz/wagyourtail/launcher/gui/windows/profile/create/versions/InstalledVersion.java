/*
 * Created by JFormDesigner on Tue Dec 21 03:40:01 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.profile.create.versions;

import xyz.wagyourtail.launcher.gui.windows.profile.create.GuiNewProfile;
import xyz.wagyourtail.launcher.minecraft.data.VersionManifest;
import xyz.wagyourtail.launcher.minecraft.version.Version;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.beans.JavaBean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ResourceBundle;

/**
 * @author unknown
 */
@JavaBean
public class InstalledVersion extends VersionSelector {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTable vanillaTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public InstalledVersion(GuiNewProfile profile) {
        super(profile);
        initComponents();
    }

    private void filterUpdated(ActionEvent e) {
        try {
            updateVersions();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        scrollPane1 = new JScrollPane();
        vanillaTable = new JTable();

        //======== this ========

        //======== scrollPane1 ========
        {

            //---- vanillaTable ----
            vanillaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            vanillaTable.setRowMargin(5);
            scrollPane1.setViewportView(vanillaTable);
        }

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(12, 12, 12)
                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 727, Short.MAX_VALUE)
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .addContainerGap())
        );
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    @Override
    public void init() {
        try {
            updateVersions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateVersions() throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        String name = bundle.getString("VanillaVersion.vanillaTable.version");
        String released = bundle.getString("VanillaVersion.vanillaTable.released");
        String snapshot = bundle.getString("VanillaVersion.vanillaTable.type");
        DefaultTableModel model = new DefaultTableModel(new String[]{name, released, snapshot}, 0);
        long latestReleaseTime = VersionManifest.getLatestRelease().releaseTime();
        Path versionPath = parent.launcher.minecraftPath.resolve("versions").toAbsolutePath();
        if (!Files.exists(versionPath)) {
            parent.launcher.getLogger().warn("Versions folder does not exist! " + versionPath);
            return;
        }
        for (Path p : Files.list(versionPath).toList()) {
            try {
                Version version = Version.resolve(parent.launcher, p.getFileName().toString());
                model.addRow(new Object[] {
                    version.id(),
                    Instant.ofEpochMilli(version.releaseTime()).toString().substring(0, 10),
                    version.type()
                });
            } catch (IOException e) {
                parent.launcher.getLogger().error("Failed to resolve version " + p.getFileName().toString());
            }
        }
        vanillaTable.setModel(model);
    }

    @Override
    public void create() {
        Object version = vanillaTable.getModel().getValueAt(vanillaTable.getSelectedRow(), 0);
        if (version != null) {
            createVanillaProfile(version.toString());
            parent.launcher.newProfile = null;
            parent.dispose();
        }
    }

}
