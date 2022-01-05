package xyz.wagyourtail.launcher.swing;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.swing.screen.keystore.GuiKeystorePassword;
import xyz.wagyourtail.launcher.swing.screen.main.GuiMainWindow;
import xyz.wagyourtail.util.OSUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

public class SwingWindowManager extends LauncherBase {

    static {
        if (OSUtils.getOSId().equals("linux")) {
            try {
                // set to gtk even if not on gnome
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        }
    }

    public SwingWindowManager(Path minecraftPath) throws IOException {
        super(minecraftPath);
    }

    @Override
    public char[] promptKeystorePasswordAndWait(boolean isNew) throws InterruptedException {
        GuiKeystorePassword guiKeystorePassword = new GuiKeystorePassword(this, mainWindow, isNew);
        guiKeystorePassword.setVisible(true);
        Semaphore sem = new Semaphore(0);
        char[][] pw = new char[1][];
        guiKeystorePassword.then(r -> {
            pw[0] = r;
            sem.release();
        });
        sem.acquire();
        return pw[0];
    }

    @Override
    public void error(String message) {
        JOptionPane.showMessageDialog((Component) mainWindow, message, "Error", JOptionPane.ERROR_MESSAGE);
        getLogger().error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        JOptionPane.showMessageDialog((Component) mainWindow, message + ":\n\n" + t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        getLogger().error(message);
    }

    @Override
    public void init() throws IOException {
        this.mainWindow = new GuiMainWindow(this);
        ((GuiMainWindow) this.mainWindow).initComponents();
        ((GuiMainWindow) this.mainWindow).setVisible(true);
    }

    @Override
    public void refreshProfiles() {
        try {
            ((GuiMainWindow) this.mainWindow).populateProfiles();
        } catch (IOException e) {
            error("Failed to refresh profiles", e);
        }
    }

    @Override
    public void refreshAccounts() {
        try {
            ((GuiMainWindow) this.mainWindow).populateAccounts();
        } catch (IOException e) {
            error("Failed to refresh accounts", e);
        }
    }

}
