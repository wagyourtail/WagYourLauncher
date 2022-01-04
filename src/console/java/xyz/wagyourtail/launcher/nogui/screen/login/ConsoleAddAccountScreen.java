package xyz.wagyourtail.launcher.nogui.screen.login;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.launcher.gui.screen.login.UsernamePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;
import xyz.wagyourtail.notlog4j.Logger;

public class ConsoleAddAccountScreen extends ConsoleScreen implements AddAccountScreen {
    protected ConsoleAddAccountScreen(LauncherBase launcher, MainScreen mainScreen) {
        super(launcher, mainScreen, "Add Account");
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public void setProgress(int progress) {

    }

    @Override
    public UsernamePasswordScreen getUsernamePassword() {
        return null;
    }

}
