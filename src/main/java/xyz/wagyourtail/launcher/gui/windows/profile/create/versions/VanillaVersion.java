/*
 * Created by JFormDesigner on Tue Dec 21 03:40:01 MST 2021
 */

package xyz.wagyourtail.launcher.gui.windows.profile.create.versions;

import java.awt.event.*;
import javax.swing.border.*;
import xyz.wagyourtail.launcher.gui.windows.profile.create.GuiNewProfile;
import xyz.wagyourtail.launcher.minecraft.data.VersionManifest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.beans.JavaBean;
import java.io.IOException;
import java.time.Instant;
import java.util.ResourceBundle;

/**
 * @author unknown
 */
@JavaBean
public class VanillaVersion extends VersionSelector {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel3;
    private JCheckBox snapshotFilter;
    private JCheckBox releaseFilter;
    private JLabel label2;
    private JCheckBox oldSnapshots;
    private JCheckBox betas;
    private JCheckBox alphas;
    private JCheckBox experiments;
    private JButton refresh;
    private JScrollPane scrollPane1;
    private JTable vanillaTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public VanillaVersion(GuiNewProfile profile) {
        super(profile);
        initComponents();
    }

    private void filterUpdated(ActionEvent e) {
        init();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        panel3 = new JPanel();
        snapshotFilter = new JCheckBox();
        releaseFilter = new JCheckBox();
        label2 = new JLabel();
        oldSnapshots = new JCheckBox();
        betas = new JCheckBox();
        alphas = new JCheckBox();
        experiments = new JCheckBox();
        refresh = new JButton();
        scrollPane1 = new JScrollPane();
        vanillaTable = new JTable();

        //======== this ========

        //======== panel3 ========
        {

            //---- snapshotFilter ----
            snapshotFilter.setText(bundle.getString("VanillaVersion.snapshotFilter.text_2"));
            snapshotFilter.addActionListener(e -> filterUpdated(e));

            //---- releaseFilter ----
            releaseFilter.setText(bundle.getString("VanillaVersion.releaseFilter.text_2"));
            releaseFilter.setSelected(true);
            releaseFilter.setHorizontalAlignment(SwingConstants.LEFT);
            releaseFilter.addActionListener(e -> filterUpdated(e));

            //---- label2 ----
            label2.setText(bundle.getString("VanillaVersion.label2.text_2"));
            label2.setHorizontalAlignment(SwingConstants.CENTER);

            //---- oldSnapshots ----
            oldSnapshots.setText(bundle.getString("VanillaVersion.oldSnapshots.text_2"));
            oldSnapshots.addActionListener(e -> filterUpdated(e));

            //---- betas ----
            betas.setText(bundle.getString("VanillaVersion.betas.text_2"));
            betas.addActionListener(e -> filterUpdated(e));

            //---- alphas ----
            alphas.setText(bundle.getString("VanillaVersion.alphas.text_2"));
            alphas.addActionListener(e -> filterUpdated(e));

            //---- experiments ----
            experiments.setText(bundle.getString("VanillaVersion.experiments.text_2"));
            experiments.addActionListener(e -> filterUpdated(e));

            //---- refresh ----
            refresh.setText(bundle.getString("VanillaVersion.refresh.text"));

            GroupLayout panel3Layout = new GroupLayout(panel3);
            panel3.setLayout(panel3Layout);
            panel3Layout.setHorizontalGroup(
                panel3Layout.createParallelGroup()
                    .addComponent(releaseFilter, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(snapshotFilter, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(oldSnapshots, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addComponent(betas, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(alphas, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(experiments, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(label2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(refresh, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
            );
            panel3Layout.setVerticalGroup(
                panel3Layout.createParallelGroup()
                    .addGroup(panel3Layout.createSequentialGroup()
                        .addComponent(label2)
                        .addGap(3, 3, 3)
                        .addComponent(releaseFilter)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(snapshotFilter)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(oldSnapshots)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(betas)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alphas)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(experiments)
                        .addGap(18, 18, 18)
                        .addComponent(refresh)
                        .addContainerGap(54, Short.MAX_VALUE))
            );
        }

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
                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(panel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(12, 12, 12))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(panel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .addContainerGap())
        );
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    @Override
    public void init() {
        try {
            updateVersions();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load versions: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public boolean filterMatches(VersionManifest.Version version, long latestReleaseTime) throws IOException {
        if (releaseFilter.isSelected() && version.type() == VersionManifest.Type.RELEASE) {
            return true;
        }
        if (snapshotFilter.isSelected() && version.type() == VersionManifest.Type.SNAPSHOT) {
            return version.releaseTime() > latestReleaseTime;
        }
        if (oldSnapshots.isSelected() && version.type() == VersionManifest.Type.SNAPSHOT) {
            return version.releaseTime() < latestReleaseTime;
        }
        if (betas.isSelected() && version.type() == VersionManifest.Type.OLD_BETA) {
            return true;
        }
        if (alphas.isSelected() && version.type() == VersionManifest.Type.OLD_ALPHA) {
            return true;
        }
        return experiments.isSelected() && version.type() == VersionManifest.Type.EXPERIMENTAL;
    }

    public void updateVersions() throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        String name = bundle.getString("VanillaVersion.vanillaTable.version");
        String released = bundle.getString("VanillaVersion.vanillaTable.released");
        String snapshot = bundle.getString("VanillaVersion.vanillaTable.type");
        DefaultTableModel model = new DefaultTableModel(new String[]{name, released, snapshot}, 0);
        long latestReleaseTime = VersionManifest.getLatestRelease().releaseTime();
        for (VersionManifest.Version version : VersionManifest.getAllVersions().values()) {
            if (filterMatches(version, latestReleaseTime)) {
                model.addRow(new Object[]{version.id(), Instant.ofEpochMilli(version.releaseTime()).toString().substring(0, 10), version.type().id});
            }
        }
        vanillaTable.setModel(model);
    }

    @Override
    public void create() {
        if (vanillaTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a version", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Object version = vanillaTable.getModel().getValueAt(vanillaTable.getSelectedRow(), 0);
        if (version != null) {
            createVanillaProfile(version.toString());
            parent.launcher.newProfile = null;
            parent.dispose();
        }
    }

}
