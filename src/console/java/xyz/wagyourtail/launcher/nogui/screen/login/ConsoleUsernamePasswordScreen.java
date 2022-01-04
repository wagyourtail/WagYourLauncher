package xyz.wagyourtail.launcher.nogui.screen.login;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.login.UsernamePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;

import java.util.function.BiConsumer;

public class ConsoleUsernamePasswordScreen extends ConsoleScreen implements UsernamePasswordScreen {
    public ConsoleUsernamePasswordScreen(LauncherBase launcher, MainScreen mainScreen) {
        super(launcher, mainScreen, "Username/Password");
        init();
    }

    @Override
    public void init() throws InterruptedException {

    }

    @Override
    public void then(BiConsumer<String, char[]> r) {

    }

}
