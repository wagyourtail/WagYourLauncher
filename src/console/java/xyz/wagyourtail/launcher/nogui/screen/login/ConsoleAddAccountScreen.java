package xyz.wagyourtail.launcher.nogui.screen.login;

import xyz.wagyourtail.launcher.LauncherBase;
import xyz.wagyourtail.launcher.gui.screen.MainScreen;
import xyz.wagyourtail.launcher.gui.screen.login.AddAccountScreen;
import xyz.wagyourtail.launcher.gui.screen.login.UsernamePasswordScreen;
import xyz.wagyourtail.launcher.nogui.screen.ConsoleScreen;
import xyz.wagyourtail.notlog4j.Logger;

import java.util.Scanner;

public class ConsoleAddAccountScreen extends ConsoleScreen implements AddAccountScreen {
    public ConsoleAddAccountScreen(LauncherBase launcher, MainScreen mainScreen) {
        super(launcher, mainScreen, "Add Account");
        init();
    }

    @Override
    public void init() {
        String[] providers = getProviders();
        getLogger().info("Available providers:");
        for (int i = 0; i < providers.length; i++) {
            getLogger().info("  " + (i + 1) + ". " + providers[i]);
        }
        Scanner scanner = new Scanner(System.in);
        getLogger().info("Enter provider number:");
        int provider = scanner.nextInt();
        if (provider < 1 || provider > providers.length) {
            getLogger().info("Invalid provider number");
            return;
        }
        runLogin(providers[provider - 1]);
    }

    @Override
    public Logger getLogger() {
        return getLauncher().getLogger();
    }

    @Override
    public void setProgress(int progress) {

    }

    @Override
    public UsernamePasswordScreen getUsernamePassword() {
        try {
            return new ConsoleUsernamePasswordScreen(getLauncher(), getMainWindow());
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

}
