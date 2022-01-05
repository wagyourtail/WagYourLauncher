package xyz.wagyourtail.launcher.nogui;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.KeystorePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleKeystorePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleMainScreen;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class LauncherNoGui extends LauncherBase {

    public LauncherNoGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
    }

    @Override
    public void error(String message) {
        getLogger().error(message);
    }

    @Override
    public void error(String message, Throwable t) {
        getLogger().error(message);
        t.printStackTrace();
    }

    @Override
    protected void init() throws IOException {
        try {
            this.mainWindow = new ConsoleMainScreen(this);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void refreshLaunchedProfiles() {

    }

    @Override
    public char[] promptKeystorePasswordAndWait(boolean isNew) {
        KeystorePasswordScreen sc = new ConsoleKeystorePasswordScreen(this, mainWindow);
        char[][] pw = new char[1][];
        Semaphore sem = new Semaphore(0);
        sc.then(r -> {
            pw[0] = r;
            sem.release();
        });
        try {
            sem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pw[0];
    }


}
