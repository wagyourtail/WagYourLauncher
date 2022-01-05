/*
 * Created by JFormDesigner on Wed Jan 05 07:03:07 MST 2022
 */

package xyz.wagyourtail.launcher.swing.screen.profile.create.versions.v2;

import de.javagl.treetable.AbstractTreeTableModel;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;
import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.versions.BaseVersionProvider;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout;

/**
 * @author unknown
 */
public class VersionSelector extends JPanel {
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JScrollPane scrollPane1;
    private JTable treeTable;
    private JButton button1;
    private JPanel panel1;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private final LauncherBase launcher;
    private final BaseVersionProvider<?> provider;

    public boolean initialized = false;

    public VersionSelector(LauncherBase launcher, BaseVersionProvider<?> provider) {
        this.launcher = launcher;
        this.provider = provider;
    }

    public synchronized void initComponents() {
        if (initialized) return;
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        ResourceBundle bundle = ResourceBundle.getBundle("lang.lang");
        scrollPane1 = new JScrollPane();
        treeTable = new JTreeTable(new VersionTreeTableModel<>(provider));
        button1 = new JButton();
        panel1 = new JPanel();
        checkBox1 = new JCheckBox();
        checkBox2 = new JCheckBox();

        //======== this ========

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(treeTable);
        }

        //---- button1 ----
        button1.setText(bundle.getString("VersionSelector.button1.text"));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
            ((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
            ((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
            ((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

            //---- checkBox1 ----
            checkBox1.setText(bundle.getString("VersionSelector.checkBox1.text"));
            panel1.add(checkBox1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

            //---- checkBox2 ----
            checkBox2.setText(bundle.getString("VersionSelector.checkBox2.text"));
            panel1.add(checkBox2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
                    .addGap(18, 18, 18)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(button1, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                        .addComponent(panel1, GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(panel1, GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(button1)))
                    .addContainerGap())
        );
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
        initialized = true;
    }

    public class VersionTreeTableModel<T extends BaseVersionProvider.BaseVersionData> extends AbstractTreeTableModel {

        boolean hasIcon = false;

        int columns;

        protected VersionTreeTableModel(BaseVersionProvider<T> root) {
            this((T) new BaseVersionProvider.BaseVersionData() {

                @Override
                public URL getIconUrl() {
                    return null;
                }

                @Override
                public String getId() {
                    return "";
                }

                @Override
                public String[] getTableParts() {
                    return new String[4];
                }

                @Override
                public boolean hasSubProviders() {
                    return true;
                }

                @Override
                public String[] filterMatches() {
                    return root.versionFilters();
                }

                @Override
                public BaseVersionProvider<?> getSubProvider() {
                    return root;
                }

                @Override
                public String provide() throws IOException {
                    try {
                        return root.getLatestStable().provide();
                    } catch (IOException e) {
                        throw new IOException("Failed to provide latest stable version", e);
                    }
                }
            });
        }

        /**
         * Default constructor
         *
         * @param root The root node of the tree
         */
        protected VersionTreeTableModel(T root) {
            super(root);
            try {
                root.getSubProvider().refreshVersions();
            } catch (IOException e) {
                launcher.error("Failed refreshing versions", e);
            }
            hasIcon = root.getSubProvider().hasIcons();
            columns = root.getSubProvider().getTableHeaders().length + (hasIcon ? 1 : 0);
        }

        @Override
        public int getColumnCount() {
            return columns;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0 && hasIcon) {
                return "Icon";
            }
            return ((T)root).getSubProvider().getTableHeaders()[column + (hasIcon ? 1 : 0)];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return TreeTableModel.class;
            }
            return Object.class;
        }

        @Override
        public Object getValueAt(Object node, int column) {
            if (column == 0 && hasIcon) {
                return ((T) node).getIconUrl();
            }
            return ((T) node).getTableParts()[column + (hasIcon ? 1 : 0)];
        }

        @Override
        public Object getChild(Object parent, int index) {
            try {
                return ((T) parent).getSubProvider().getVersions().get(index);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            try {
                if (((T) parent).hasSubProviders()) {
                    return ((T) parent).getSubProvider().getVersions().size();
                } else {
                    return 0;
                }
            } catch (IOException e) {
                launcher.error("Failed to get child count", e);
            }
            return 0;
        }

    }

    public BaseVersionProvider.BaseVersionData getSelected() {
        return (BaseVersionProvider.BaseVersionData) ((JTreeTable) treeTable).getTree().getLastSelectedPathComponent();
    }
}
