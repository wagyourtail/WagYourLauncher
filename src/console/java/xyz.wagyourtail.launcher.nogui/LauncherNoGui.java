package xyz.wagyourtail.launcher.nogui;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.KeystorePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleKeystorePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleMainScreen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

public class LauncherNoGui extends LauncherBase {

    public LauncherNoGui(Path minecraftPath) throws IOException {
        super(minecraftPath);
        this.mainWindow = new ConsoleMainScreen(this);
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
    public void init() throws IOException {
        try {
            ((ConsoleMainScreen) mainWindow).init();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void refreshProfiles() {

    }

    @Override
    public void refreshAccounts() {

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
