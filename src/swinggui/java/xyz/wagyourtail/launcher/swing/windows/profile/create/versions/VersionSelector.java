package xyz.wagyourtail.launcher.swing.windows.profile.create.versions;

import javax.swing.*;

public abstract class VersionSelector extends JPanel {
    protected JFrame parent;
    protected boolean initialized = false;

    public VersionSelector(JFrame parent) {
        this.parent = parent;
    }

    public abstract void init();

    public abstract String getSelected();

    public boolean isInitialized() {
        return initialized;
    }
}
