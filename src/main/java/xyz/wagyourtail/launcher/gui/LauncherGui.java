package xyz.wagyourtail.launcher.gui;

import xyz.wagyourtail.launcher.Launcher;
import xyz.wagyourtail.launcher.Logger;
import xyz.wagyourtail.launcher.gui.windows.keystore.GuiKeystorePassword;
import xyz.wagyourtail.launcher.gui.windows.login.GuiLogin;
import xyz.wagyourtail.launcher.gui.windows.main.GuiMainWindow;
import xyz.wagyourtail.launcher.gui.windows.profile.GuiProfile;
import xyz.wagyourtail.launcher.minecraft.profile.Profile;
import xyz.wagyourtail.util.OSUtils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LauncherGui extends Launcher {

    static {
        if (OSUtils.getOSId().equals("linux")) {
            try {
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

    public GuiMainWindow mainWindow = new GuiMainWindow(this);
    public GuiLogin login;
    public GuiKeystorePassword keystorePassword;
    public final Map<String, GuiProfile> guiProfiles = new HashMap<>();

    public LauncherGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
        init();
    }

    @Override
    public Logger getProfileLogger(Profile userProfile) {
        return getGuiProfile(userProfile).getLogger();
    }

    public GuiProfile getGuiProfile(Profile profile) {
        return guiProfiles.computeIfAbsent(profile.key(), k -> new GuiProfile(this, profile)).setProfile(profile);
    }

    @Override
    public void launch(Profile profile, String username, boolean offline) throws Exception {
        if (!auth.isRegistered(username)) {
            throw new IOException("User " + username + " is not logged in!");
        }
        CompletableFuture.runAsync(() -> {
            try {
                profile.launch(this, username, offline);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public synchronized char[] promptKeystorePasswordAndWait(boolean isNew) {
        if (keystorePassword == null) {
            keystorePassword = new GuiKeystorePassword(isNew);
            keystorePassword.setVisible(true);
        }
        GuiKeystorePassword temp = keystorePassword;
        synchronized (temp) {
            try {
                temp.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return temp.password.get();
    }

    protected void init() throws IOException {
        openMainWindow();
        new Thread(() -> {
            while (true) {
                Thread.yield();
                if (mainWindow != null && mainWindow.isVisible()) {
                    continue;
                }
                if (login != null && login.isVisible()) {
                    continue;
                }
                if (keystorePassword != null && keystorePassword.isVisible()) {
                    continue;
                }
                boolean flag = true;
                for (GuiProfile guiProfile : guiProfiles.values()) {
                    if (guiProfile.isVisible()) {
                        flag = false;
                        break;
                    }
                }
                //else
                if (flag) System.exit(0);
            }
        }).start();
    }

    public void openMainWindow() throws IOException {
        mainWindow.initComponents();
    }
}
