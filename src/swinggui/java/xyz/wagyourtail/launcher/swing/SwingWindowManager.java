package xyz.wagyourtail.launcher.swing;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.profile.ProfileScreen;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.launcher.swing.windows.profile.GuiProfile;
import xyz.wagyourtail.util.OSUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;

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
        return new char[0];
    }

    @Override
    public void error(String message) {

    }

    @Override
    public void error(String message, Throwable t) {

    }

    @Override
    protected void init() throws IOException {
        openMainWindow();

        // auto kill when windows closed
        new Thread(() -> {
            while (true) {
                Thread.yield();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (mainWindow != null && mainWindow.isVisible()) {
                    continue;
                }
                if (login != null && login.isVisible()) {
                    continue;
                }
                if (newProfile != null && newProfile.isVisible()) {
                    continue;
                }
                if (keystorePassword != null && keystorePassword.isVisible()) {
                    continue;
                }
                boolean flag = true;
                for (ProfileScreen guiProfile : guiProfiles.values()) {
                    if (guiProfile.isVisible()) {
                        flag = false;
                        break;
                    }
                }
                //else
                if (flag) {
                    if (mainWindow != null) {
                        mainWindow.dispose();
                    }
                    if (login != null) {
                        login.dispose();
                    }
                    if (newProfile != null) {
                        newProfile.dispose();
                    }
                    if (keystorePassword != null) {
                        keystorePassword.dispose();
                    }
                    for (ProfileScreen guiProfile : guiProfiles.values()) {
                        guiProfile.dispose();
                    }
                    System.exit(0);
                }
            }
        }).start();
    }

}
